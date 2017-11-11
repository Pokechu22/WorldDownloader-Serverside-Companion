package wdl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import wdl.range.BlockRangeGroupType;
import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;
import wdl.range.ProtectionRange;
import wdl.range.ChunkRangeGroupType;
import wdl.range.IRangeProducer;
import wdl.range.TransientRangeProducer;
import wdl.request.PermissionRequest;
import wdl.request.RequestManager;

/**
 * Serverside plugin for World Downloader.
 * 
 * Makes use of the plugin channel system to control various features of the
 * mod as needed, and supports chunk overrides and permission requests.
 * 
 * @author Pokechu22
 * 
 * @see <a href="http://wiki.vg/User:Pokechu22/World_downloader">The protocol doc</a>
 */
public class WDLCompanion extends JavaPlugin implements Listener, PluginMessageListener {
	/**
	 * The name of the plugin channel sent by WDL to signify that it is ready to
	 * receive permissions.
	 */
	public static final String INIT_CHANNEL_NAME = "WDL|INIT";
	
	/**
	 * The name of the plugin channel used for WDL control.
	 */
	public static final String CONTROL_CHANNEL_NAME = "WDL|CONTROL";
	
	/**
	 * The name of the plugin channel used by WDL to request new permissions.
	 */
	public static final String REQUEST_CHANNEL_NAME = "WDL|REQUEST";

	/**
	 * Cached entity ranges.
	 * <br/>
	 * Key is worldname, value is the map between entity name string and
	 * track distance.
	 */
	private Map<String, Map<String, Integer>> worldEntityRanges 
			= new HashMap<>();
	
	public PermissionHandler permissionHandler;
	public RequestManager requestManager;
	
	/**
	 * A transient range producer to store ranges from accepted permission
	 * requests.
	 */
	public TransientRangeProducer requestRangeProducer;
	
	/**
	 * Map of all registered {@link IRangeGroupType}s by their IDs.
	 */
	final Map<String, IRangeGroupType<?>> registeredRangeGroupTypes = new HashMap<>();
	/**
	 * Map of all registered {@link IRangeProducer}s by their IDs.
	 */
	private final Map<String, IRangeProducer> rangeProducers = new HashMap<>();
	
	@Override
	public void onLoad() {
		try {
			LoggingHandler.setupLogging(getConfig().getString("wdl.logMode"));
		} catch (Throwable e) {
			getLogger().log(Level.WARNING, 
					"Failed to set up WDL-only logging!", e);
		}
	}
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		this.getServer().getMessenger()
				.registerIncomingPluginChannel(this, INIT_CHANNEL_NAME, this);
		this.getServer().getMessenger()
				.registerIncomingPluginChannel(this, REQUEST_CHANNEL_NAME, this);
		this.getServer().getMessenger()
				.registerOutgoingPluginChannel(this, CONTROL_CHANNEL_NAME);
		this.getServer().getPluginManager().registerEvents(this, this);
		
		this.permissionHandler = new PermissionHandler(this);
		this.requestManager = new RequestManager(this);
		
		try {
			class ConfigBooleanPlotter extends Plotter {
				private final String key;
				public ConfigBooleanPlotter(String key) {
					super(getConfig().getBoolean(key) ? "true" : "false");
					
					this.key = key;
				}
				
				@Override
				public String getColumnName() {
					return getConfig().getBoolean(key) ? "true" : "false";
				}

				@Override
				public int getValue() {
					return 1;
				}
			}

			Metrics metrics = new Metrics(this);

			int saveRadius = getConfig().getInt("wdl.saveRadius", -1);
			String saveRadiusText = (saveRadius >= 0 ? (saveRadius + " chunks")
					: "Server view distance");

			Graph canDoNewThingsGraph = metrics
					.createGraph("canDoNewThings");
			canDoNewThingsGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canDoNewThings"));
			
			Graph globalEnabledGraph = metrics
					.createGraph("canDownloadInGeneral");
			globalEnabledGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canDownloadInGeneral"));

			Graph saveRadiusGraph = metrics.createGraph("saveRadius");
			saveRadiusGraph.addPlotter(new Plotter(saveRadiusText) {
				@Override
				public int getValue() {
					return 1;
				}
				
				@Override
				public String getColumnName() {
					int saveRadius = getConfig().getInt("wdl.saveRadius", -1);
					String saveRadiusText = (saveRadius >= 0 ? (saveRadius + " chunks")
							: "Server view distance");

					return saveRadiusText;
				}
			});

			Graph canCacheChunksGraph = metrics
					.createGraph("canCacheChunks");
			canCacheChunksGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canCacheChunks"));

			Graph canSaveEntitiesGraph = metrics
					.createGraph("canSaveEntities");
			canSaveEntitiesGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canSaveEntities"));

			Graph canSaveTileEntitiesGraph = metrics
					.createGraph("canSaveTileEntities");
			canSaveTileEntitiesGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canSaveTileEntities"));

			Graph canSaveContainersGraph = metrics
					.createGraph("canSaveContainers");
			canSaveContainersGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canSaveContainers"));
			
			Graph sendEntityRangesGraph = metrics
					.createGraph("sendEntityRanges");
			sendEntityRangesGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.sendEntityRanges"));

			metrics.start();
		} catch (IOException e) {
			getLogger().warning("Failed to start PluginMetrics :(");
		}
		
		
		// Let everything else finish and then call events and perform the
		// rangeProducer registration.
		getServer().getScheduler().runTaskLater(this, new Runnable() {
			@Override
			public void run() {
				for (IRangeGroupType<?> type : registeredRangeGroupTypes.values()) {
					try {
						type.dispose();
					} catch (Exception e) {
						getLogger().log(Level.WARNING,
								"Failed to dispose of old IRangeGroupType " + type
										+ ": ", e);
					}
				}
				registeredRangeGroupTypes.clear();
				RangeGroupTypeRegistrationEvent event =
						new RangeGroupTypeRegistrationEvent(WDLCompanion.this);
				getServer().getPluginManager().callEvent(event);
				event.markFinished();
				
				// Warn about incorrect any incorrect config setup.
				// Using the console sender because it supports coloration.
				// We do this now so that the group types have registered.
				ConfigValidation.validateConfig(getConfig(), getServer()
						.getConsoleSender(), WDLCompanion.this);
				
				// OK, now create the range producers.
				createRangeProducers();
				
				// And finally, we can update all players so that they get
				// their ranges.
				updateAllPlayers();
			}
		}, 1);
	}

	@Override
	public void onDisable() {
		this.getServer().getMessenger()
				.unregisterIncomingPluginChannel(this, INIT_CHANNEL_NAME);
		this.getServer().getMessenger()
				.unregisterIncomingPluginChannel(this, REQUEST_CHANNEL_NAME);
		this.getServer().getMessenger()
				.unregisterOutgoingPluginChannel(this, CONTROL_CHANNEL_NAME);
	}
	
	@EventHandler
	public void registerRanges(RangeGroupTypeRegistrationEvent event) {
		event.addRegistration("BlockRange", new BlockRangeGroupType());
		event.addRegistration("ChunkRange", new ChunkRangeGroupType());
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String alias, String[] args) {
		if (command.getName().equals("wdl")) {
			if (args.length <= 1) {
				List<String> base = Arrays.asList("updateme", "reload",
						"update", "updateall", "requests");
				if (args.length == 0) {
					return base;
				}
				
				return tabLimit(base, args[0]);
			}
			if (args.length == 2) {
				if (args[0].equals("update")) {
					List<String> names = new ArrayList<>();
					for (Player player : Bukkit.getOnlinePlayers()) {
						names.add(player.getName());
					}
					return tabLimit(names, args[1]);
				}
				if (args[0].equals("requests")) {
					return tabLimit(Arrays.asList("list", "show",
							"accept", "reject", "revoke"), args[1]);
				}
			}
			if (args.length == 3) {
				if (args[0].equals("requests")) {
					if (args[1].equals("show") || args[1].equals("accept")
							|| args[1].equals("reject")
							|| args[1].equals("revoke")) {
						List<String> names = new ArrayList<>();
						for (Player player : Bukkit.getOnlinePlayers()) {
							names.add(player.getName());
						}
						return tabLimit(names, args[2]);
					}
				}
			}
		}
		return new ArrayList<>();
	}
	
	/**
	 * Tab-limits the given list of strings, returning only values that
	 * start with second parameter.
	 */
	private List<String> tabLimit(List<String> values, String start) {
		List<String> returned = new ArrayList<>();
		
		start = start.toLowerCase();
		for (String s : values) {
			if (s.toLowerCase().startsWith(start)) {
				returned.add(s);
			}
		}
		
		return returned;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (command.getName().equals("wdl")) {
			if (args.length < 1) {
				return false;
			}
			
			if (args[0].equalsIgnoreCase("updateme")) {
				if (args.length != 1) {
					return false;
				}
				
				if (!(sender instanceof Player)) {
					sender.sendMessage("§cYou must be a player.");
					return true;
				}
				Player player = (Player)sender;
				if (!player.getListeningPluginChannels()
						.contains(CONTROL_CHANNEL_NAME)) {
					sender.sendMessage("§cYou don't have WDL installed!");
					sender.sendMessage("§c(not listening on "
							+ CONTROL_CHANNEL_NAME + ")");
					return true;
				}
				
				updatePlayer(player);
				sender.sendMessage("§aUpdated your WDL permissions.");
				
				return true;
			}
			if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("wdl.admin.reloadConfig")) {
					sender.sendMessage("§cYou don't have permission!");
					return true;
				}
				
				if (args.length != 1) {
					return false;
				}
				
				reloadConfig();
				ConfigValidation.validateConfig(getConfig(), sender, this);
				createRangeProducers();
				
				updateAllPlayers();
				
				sender.sendMessage("§aWDL configuration reloaded.");
				return true;
			}
			if (args[0].equalsIgnoreCase("update")) {
				if (!sender.hasPermission("wdl.admin.updatePlayer")) {
					sender.sendMessage("§cYou don't have permission!");
					return true;
				}
				
				if (args.length != 2) {
					return false;
				}
				
				Player player = getServer().getPlayer(args[1]);
				if (player == null) {
					sender.sendMessage("§cThere is no player named " + 
							args[1] + ".");
					return true;
				}
				
				if (!player.getListeningPluginChannels()
						.contains(CONTROL_CHANNEL_NAME)) {
					sender.sendMessage("§c" + player.getDisplayName() + 
							" doesn't have WDL installed!");
					sender.sendMessage("§c(not listening on "
							+ CONTROL_CHANNEL_NAME + ")");
					return true;
				}
				
				updatePlayer(player);
				sender.sendMessage("§aUpdated " + player.getDisplayName() +
						"'s WDL permissions.");
				
				return true;
			}
			if (args[0].equalsIgnoreCase("updateall")) {
				if (!sender.hasPermission("wdl.admin.updatePlayer")) {
					sender.sendMessage("§cYou don't have permission!");
					return true;
				}
				
				if (args.length != 1) {
					return false;
				}
				
				int updatedCount = updateAllPlayers();
				
				sender.sendMessage("§aUpdated the WDL permissions of " + 
						updatedCount + " players.");
				return true;
			}
			
			if (args[0].equals("requests")) {
				if (!sender.hasPermission("wdl.admin.handleRequests")) {
					sender.sendMessage("§cYou don't have permission!");
					return true;
				}
				if (args.length == 1) {
					sender.sendMessage("Usage: ");
					sender.sendMessage("/wdl requests list [page] -- List all requests.");
					sender.sendMessage("/wdl requests show <player> -- Show <player>'s request, if present.");
					sender.sendMessage("/wdl requests accept <player> -- Approve <player>'s request.");
					sender.sendMessage("/wdl requests reject <player> -- Deny <player>'s request.");
					sender.sendMessage("/wdl requests revoke <player> -- Revoke <player>'s request after it has already been accepted.");
					return true;
				}
				// Aliases
				if (args[1].equals("acept") || args[1].equals("approve")
						|| args[1].equals("aprove")) {
					args[1] = "accept";
				}
				if (args[1].equals("deny")) {
					args[1] = "reject";
				}

				if (args[1].equals("list")) {
					int page = 1;
					final int NUM_PER_PAGE = 8;
					
					List<PermissionRequest> requests = requestManager.getRequests();
					int numPages = (int) Math.ceil(requests.size()
							/ (float)NUM_PER_PAGE);
					
					if (args.length > 3) {
						sender.sendMessage("§cUsage: /wdl requests list [page] -- List all requests.");
						return true;
					}
					if (args.length == 3) {
						try {
							page = Integer.parseInt(args[2]);
						} catch (NumberFormatException e) {
							sender.sendMessage("§cInvalid page number: '"
									+ args[2] + "' is not a number!");
							return true;
						}
						if (page <= 0) {
							sender.sendMessage("§cInvalid page number: Must be greater than 0!");
							return true;
						}
					}
					
					if (numPages == 0) {
						sender.sendMessage("There currently are no requests!");
						return true;
					}
					
					if (page > numPages) {
						sender.sendMessage("§cInvalid page number: There are only " + numPages + " pages!");
						return true;
					}
					
					sender.sendMessage("Permission requests (page " + page + " of " + numPages +"): ");
					for (int i = 0; i < NUM_PER_PAGE; i++) {
						int index = NUM_PER_PAGE * (page - 1) + i;
						if (index >= requests.size()) {
							break;
						}
						PermissionRequest request = requests.get(index);
						String description = request.state.prefix
								+ request.toString() + " - ";
						int remainingChars = 65 - description.length();
						if (remainingChars > 0) {
							if (request.requestReason.length() <= remainingChars) {
								description += request.requestReason;
							} else {
								description += request.requestReason.substring(
										0, remainingChars) + "...";
							}
						}

						sender.sendMessage(description);
					}
					
					return true;
				} else if (args[1].equals("show")) {
					if (args.length != 3) {
						sender.sendMessage("Usage: /wdl requests show <player> -- Show <player>'s request, if present.");
						return true;
					}
					PermissionRequest request = requestManager.getPlayerRequest(args[2]);
					if (request == null) {
						sender.sendMessage("§cPlayer '" + args[2] + "' doesn't have a request or doesn't exist.");
						return true;
					}
					sender.sendMessage(args[2] + "'s request ("
							+ request.state.prefix + request.state.name()
							+ "§r):");
					sender.sendMessage("Requesting: ");
					for (Map.Entry<String, String> e : request.requestedPerms.entrySet()) {
						sender.sendMessage(" * " + e.getKey() + " to be " + e.getValue());
					}
					sender.sendMessage("Ranges: ");
					for (ProtectionRange range :  request.rangeRequests) {
						sender.sendMessage(" * " + range);
					}
					sender.sendMessage("Reason: ");
					sender.sendMessage(request.requestReason);
					return true;
				} else if (args[1].equals("accept")) {
					if (args.length != 3) {
						sender.sendMessage("Usage: /wdl requests accept <player> -- Approve <player>'s request");
						return true;
					}
					final PermissionRequest request = requestManager
							.getPlayerRequest(args[2]);
					if (request == null) {
						sender.sendMessage("§cPlayer '" + args[2] + "' doesn't have a request or doesn't exist.");
						return true;
					}
					
					if (request.state != PermissionRequest.State.WAITING) {
						sender.sendMessage("§c" + args[2] + "'s request isn't " +
								"in the right state to be accepted.");
						return true;
					}
					
					Player player = Bukkit.getPlayer(args[2]);
					if (player == null) {
						sender.sendMessage("§cPlayer '" + args[2] + "' isn't online.");
						return true;
					}
					
					//TODO: Add an argument to change this time.
					long durationSeconds = getConfig().getLong("wdl.requestDuration", 3600);
					
					requestManager.acceptRequest(durationSeconds, request);
					sender.sendMessage("§aAccepted " + args[2] + "'s request.");
					
					return true;
				} else if (args[1].equals("reject")) {
					if (args.length != 3) {
						sender.sendMessage("/wdl requests reject <player> -- Deny <player>'s request.");
						
						return true;
					}
					PermissionRequest request = requestManager.getPlayerRequest(args[2]);
					if (request == null) {
						sender.sendMessage("§cPlayer '" + args[2] + "' doesn't have a request or doesn't exist.");
						return true;
					}
					
					if (request.state != PermissionRequest.State.WAITING) {
						sender.sendMessage("§c" + args[2] + "'s request isn't " +
								"in the right state to be rejected.");
						if (request.state == PermissionRequest.State.ACCEPTED) {
							sender.sendMessage("§cUse /wdl requests revoke " +
									"<player> to revoke an already-accepted " +
									"request.");
						}
						return true;
					}
					
					requestManager.rejectRequest(request);
					sender.sendMessage("§aRejected " + args[2] + "'s request.");
					return true;
				} else if (args[1].equals("revoke")) {
					if (args.length != 3) {
						sender.sendMessage("/wdl requests revoke <player> -- Revoke <player>'s request after it has already been accepted.");
						
						return true;
					}
					PermissionRequest request = requestManager
							.getPlayerRequest(args[2]);
					if (request == null) {
						sender.sendMessage("§cPlayer '" + args[2] + "' doesn't have a request or doesn't exist.");
						return true;
					}
					
					if (request.state != PermissionRequest.State.ACCEPTED) {
						sender.sendMessage("§c" + args[2] + "'s request isn't " +
								"in the right state to be revoked.");
						if (request.state == PermissionRequest.State.WAITING) {
							sender.sendMessage("§cUse /wdl requests reject " +
									"<player> to reject a non-accepted " +
									"request.");
						}
						return true;
					}
					
					requestManager.revokeRequest(request);
					sender.sendMessage("§aRevoked " + args[2] + "'s request.");
					
					return true;
				}
				
				sender.sendMessage("§cUnknown requests subcommand '" + args[1]
						+ "'.  Do '/wdl requests' for usage.");
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Update all online players.
	 * 
	 * @return Number of players updated.
	 */
	public int updateAllPlayers() {
		int updatedCount = 0;
		for (Player player : getServer().getOnlinePlayers()) {
			if (player.getListeningPluginChannels().contains(
					CONTROL_CHANNEL_NAME)) {
				updatePlayer(player);
				updatedCount++;
			}
		}
		
		return updatedCount;
	}
	
	/**
	 * Sends a player all of the WDL settings.
	 */
	public void updatePlayer(Player player) {
		byte[][] packets = createWDLPackets(player);
		
		for (byte[] packet : packets) {
			player.sendPluginMessage(this, CONTROL_CHANNEL_NAME,
					packet);
		}
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player,
			byte[] data) {
		if (channel.equals(INIT_CHANNEL_NAME)) {
			getLogger().info("Player " + player.getName() + 
					" has WDL installed.");
			Location loc = player.getLocation();
			getLogger().info(
					"They are located in world " + player.getWorld().getName()
							+ ", at " + loc.getX() + ", " + loc.getY() + ", "
							+ loc.getZ() + ".");
			if (data.length == 0) {
				getLogger().info(
						"They are running an extremely old version of WDL (before 1.8d).");
			} else {
				try {
					String payload = new String(data, "UTF-8");
					if (payload.startsWith("{")) {
						JsonObject obj = new JsonParser().parse(payload).getAsJsonObject();
						if (!obj.has("Version")) {
							throw new Exception("Missing Version");
						}
						if (!obj.has("State")) {
							throw new Exception("Missing State");
						}
						String version = obj.get("Version").getAsString();
						String state = obj.get("State").getAsString();

						getLogger().info("They are running WDL version " + version + ", in state " + state);

						if (obj.has("X-UpdateNote")) {
							String updateNote = obj.get("X-UpdateNote").getAsString();
							if (!updateNote.equals("The plugin message system will be changing shortly.  Please stay tuned.")) {
								getLogger().info("Client-sent update note: " + updateNote);
							} else {
								// We already know of this note
								getLogger().fine("Client-sent update note: " + updateNote);
							}
						}
					} else {
						getLogger().info("They are running an old WDL version, " + payload);
					}
				} catch (UnsupportedEncodingException e) {
					throw new Error(":(", e);
				} catch (Exception e) {
					player.kickPlayer("Malformed WDL|INIT packet: " + e.getMessage());
					getLogger().log(Level.WARNING, "Received a malformed WDL|INIT packet from " + player, e);
				}
			}
			
			updatePlayer(player);
		}
		
		if (channel.equals(REQUEST_CHANNEL_NAME)) {
			PermissionRequest request = WDLPackets.readPermissionRequest(player, data);
			
			// TODO: An event, maybe?
			requestManager.addRequest(request, this);
		}
	}

	/**
	 * Recreate the {@link #rangeProducers} list.
	 */
	private void createRangeProducers() {
		for (IRangeProducer producer : rangeProducers.values()) {
			try {
				producer.getRangeGroup().dispose();
			} catch (Exception e) {
				getLogger().log(Level.WARNING,
						"Failed to dispose of old IRangeProducer " + producer
								+ "'s range group: ", e);
			}
			try {
				producer.dispose();
			} catch (Exception e) {
				getLogger().log(Level.WARNING,
						"Failed to dispose of old IRangeProducer " + producer
								+ ": ", e);
			}
		}
		
		rangeProducers.clear();
		ConfigurationSection overrides = getConfig()
				.getConfigurationSection("wdl.chunkOverrides");
		if (overrides != null) {
			Set<String> keys = overrides.getKeys(false);
			for (String key : keys) {
				ConfigurationSection override = overrides
						.getConfigurationSection(key);
				
				IRangeGroupType<?> type = registeredRangeGroupTypes
						.get(override.getString("type"));
				
				if (type == null) {
					throw new AssertionError("Failed to get the group "
							+ "type for ChunkOverride" + key + "!  "
							+ "Tried to use " + override.getString("type")
							+ ", but that was not found.");
				}
				
				IRangeGroup group = new RangeGroup(key, this);
				IRangeProducer producer = type.createRangeProducer(group,
						override);
				
				rangeProducers.put(key, producer);
			}
		}
		
		// Set up the range producer used with permission requests.
		RangeGroup requestRangeGroup = new RangeGroup("<Permission requests>", this);
		this.requestRangeProducer = new TransientRangeProducer(requestRangeGroup, this);
		rangeProducers.put("<Permission requests>", this.requestRangeProducer);
	}
	
	/**
	 * Gets a world-specific configuration setting, or the default one if 
	 * there is none set for the configuration.
	 * 
	 * @param world
	 *            The world to use.
	 * @param key
	 *            The config key, which should be in the format of 
	 *            "canDoNewThings", not "wdl.canDoNewThings".
	 * @return
	 */
	private String getWorldStringConfigValue(World world, String key) {
		String worldName = world.getName();
		String worldKey = "wdl.per-world." + worldName + "." + key;
		if (getConfig().isString(worldKey)) {
			return getConfig().getString(worldKey);
		}
		
		return getConfig().getString("wdl." + key);
	}

	/**
	 * Gets the server's entity range settings.
	 * 
	 * @param player
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private Map<String, Integer> getEntityRanges(Player player) {
		String worldName = player.getWorld().getName();
		
		if (worldEntityRanges.containsKey(worldName)) {
			return worldEntityRanges.get(worldName);
		}
		
		Map<String, Integer> ranges = new HashMap<>();
		
		try {
			int animalRange, monsterRange, miscRange, otherRange;
			
			File configFile = new File(getServer().getWorldContainer()
					.getParentFile(), "spigot.yml");
			if (!configFile.exists()) {
				worldEntityRanges.put(worldName, ranges);
				
				getLogger().warning("Failed to find entity ranges for world " 
						+ worldName + ".");
				getLogger().warning("spigot.yml does not exist.");
				getLogger().warning("If you're not running spigot, this " 
						+ "doesn't matter.");
				
				return ranges;
			}
			
			YamlConfiguration config = YamlConfiguration
					.loadConfiguration(configFile);
			
			ConfigurationSection defaults = config
					.getConfigurationSection("world-settings.default");
			ConfigurationSection world = config
					.getConfigurationSection("world-settings."
							+ player.getWorld().getName());
			
			if (world == null) {
				animalRange = defaults.getInt("entity-tracking-range.animals");
				monsterRange = defaults.getInt("entity-tracking-range.monsters");
				miscRange = defaults.getInt("entity-tracking-range.misc");
				otherRange = defaults.getInt("entity-tracking-range.other");
			} else {
				if (world.isInt("entity-tracking-range.animals")) {
					animalRange = world.getInt("entity-tracking-range.animals");
				} else {
					animalRange = defaults.getInt("entity-tracking-range.animals");
				}
				if (world.isInt("entity-tracking-range.monsters")) {
					monsterRange = world.getInt("entity-tracking-range.monsters");
				} else {
					monsterRange = defaults.getInt("entity-tracking-range.monsters");
				}
				if (world.isInt("entity-tracking-range.misc")) {
					miscRange = world.getInt("entity-tracking-range.misc");
				} else {
					miscRange = defaults.getInt("entity-tracking-range.misc");
				}
				if (world.isInt("entity-tracking-range.other")) {
					otherRange = world.getInt("entity-tracking-range.other");
				} else {
					otherRange = defaults.getInt("entity-tracking-range.other");
				}
			}
			
			for (EntityType type : EntityType.values()) {
				if (type.getName() == null) {
					continue;
				}
				
				//Based off of spigot's TrackingRange and ActivationRange.
				int range;
				
				if (Monster.class.isAssignableFrom(type.getEntityClass()) ||
						Slime.class.isAssignableFrom(type.getEntityClass())) {
					range = monsterRange;
				} else if (Creature.class.isAssignableFrom(type.getEntityClass()) ||
						Ambient.class.isAssignableFrom(type.getEntityClass())) {
					range = animalRange;
				} else if (ItemFrame.class.isAssignableFrom(type.getEntityClass()) ||
						Painting.class.isAssignableFrom(type.getEntityClass()) ||
						Item.class.isAssignableFrom(type.getEntityClass()) ||
						ExperienceOrb.class.isAssignableFrom(type.getEntityClass())) {
					range = miscRange;
				} else {
					range = otherRange;
				}
				
				ranges.put(type.getName(), range);
			}
			
			ranges.put("Hologram", otherRange);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed to find entity ranges for " 
					+ "world " + worldName + ".", e);
			
			ranges.clear();
		}
		
		worldEntityRanges.put(worldName, ranges);
		
		return ranges;
	}
	
	/**
	 * Gets the ranges that apply to the given player.
	 */
	private Map<String, List<ProtectionRange>> getRanges(Player player) {
		Map<String, List<ProtectionRange>> ranges = new HashMap<>();
		
		for (Map.Entry<String, IRangeProducer> e : rangeProducers.entrySet()) {
			ranges.put(e.getKey(), e.getValue().getInitialRanges(player));
		}
		return ranges;
	}
	
	/**
	 * Creates the byte arrays for all of the WDL packets.
	 * 
	 * @param player
	 * @return
	 */
	private byte[][] createWDLPackets(Player player) {
		byte[][] packets = new byte[5][];
		
		//Packet #1
		boolean globalIsEnabled = permissionHandler.getCanDownloadInGeneral(player);
		int saveRadius = permissionHandler.getSaveRadius(player);
		boolean cacheChunks = permissionHandler.getCanCacheChunks(player);
		boolean saveEntities = permissionHandler.getCanSaveEntities(player);
		boolean saveTileEntities = permissionHandler.getCanSaveEntities(player);
		boolean saveContainers = permissionHandler.getCanSaveContainers(player);

		packets[1] = WDLPackets.createWDLPacket1(globalIsEnabled, saveRadius, cacheChunks,
				saveEntities, saveTileEntities, saveContainers);
		
		//Packet #0
		boolean canDoNewThings = permissionHandler.getCanDoNewThings(player);
		packets[0] = WDLPackets.createWDLPacket0(canDoNewThings && globalIsEnabled);
		
		//Packet #2
		Map<String, Integer> entityMap = new HashMap<>();
		if (globalIsEnabled && saveEntities && permissionHandler.getSendEntityRanges(player)) {
			entityMap.putAll(getEntityRanges(player));
		}
		packets[2] = WDLPackets.createWDLPacket2(entityMap);
		
		//Packet #3
		String requestMessage = getWorldStringConfigValue(player.getWorld(),
				"requestMessage");
		packets[3] = WDLPackets.createWDLPacket3(requestMessage);
		
		//Packet #4
		Map<String, List<ProtectionRange>> ranges = getRanges(player);
		packets[4] = WDLPackets.createWDLPacket4(ranges);
		
		return packets;
	}
	
	/**
	 * Queues a plugin channel packet to be sent to the given player on the next
	 * tick.
	 * 
	 * @param player
	 *            The player to send the packet to.
	 * @param data
	 *            The packet to send.
	 */
	void queuePacket(Player to, byte[] data) {
		if (to == null) {
			throw new IllegalArgumentException("'to' must not be null!");
		}
		if (data == null) {
			throw new IllegalArgumentException("'data' must not be null!");
		}
		
		synchronized (packetsToSend) {
			boolean addRunnable = packetsToSend.isEmpty();
			
			PacketInfo packet = new PacketInfo(to, CONTROL_CHANNEL_NAME, data);
			
			packetsToSend.add(packet);
			
			if (addRunnable) {
				Bukkit.getScheduler().runTask(this, new Runnable() {
					@Override
					public void run() {
						synchronized (packetsToSend) {
							for (PacketInfo packet : packetsToSend) {
								packet.player.sendPluginMessage(
										WDLCompanion.this, packet.channel,
										packet.data);
							}
							
							packetsToSend.clear();
						}
					}
				});
			}
		}
	}
	
	/**
	 * Packets to send on the next server tick.
	 */
	private List<PacketInfo> packetsToSend = new ArrayList<>();
	
	/**
	 * Packet to send to a player.
	 */
	private class PacketInfo {
		public PacketInfo(Player player, String channel, byte[] data) {
			this.player = player;
			this.channel = channel;
			this.data = data;
		}
		
		public final Player player;
		public final String channel;
		public final byte[] data;
	}
}
