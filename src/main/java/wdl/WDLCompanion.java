package wdl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
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
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

/**
 * Very simple WDL companion plugin.
 * 
 * This goes on the server, and can be used to limit or disable WDL. Note that
 * this doesn't guarantee people can't use WDL to download more than they
 * should; it simply limits the options.
 * 
 * @author Pokechu22
 *
 */
public class WDLCompanion extends JavaPlugin implements Listener, PluginMessageListener {
	/**
	 * The name of the plugin channel used for WDL control.
	 */
	private static final String INIT_CHANNEL_NAME = "WDL|INIT";
	
	/**
	 * The name of the plugin channel used for WDL control.
	 */
	private static final String CONTROL_CHANNEL_NAME = "WDL|CONTROL";

	/**
	 * Cached entity ranges.
	 * <br/>
	 * Key is worldname, value is the map between entity name string and
	 * track distance.
	 */
	private Map<String, Map<String, Integer>> worldEntityRanges 
			= new HashMap<>();
	
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
				.registerOutgoingPluginChannel(this, CONTROL_CHANNEL_NAME);
		
		updateAllPlayers();
		
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

			Graph usingPlayers = metrics.createGraph("usingPlayers");
			usingPlayers.addPlotter(new Plotter("Does not have WDL installed") {
				@Override
				public int getValue() {
					int nonRunning = 0;
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (!player.getListeningPluginChannels().contains(
								INIT_CHANNEL_NAME)) {
							nonRunning++;
						}
					}

					return nonRunning;
				}
			});
			usingPlayers.addPlotter(new Plotter("Has WDL installed") {
				@Override
				public int getValue() {
					int running = 0;
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getListeningPluginChannels().contains(
								INIT_CHANNEL_NAME)) {
							running++;
						}
					}

					return running;
				}
			});

			metrics.start();
		} catch (IOException e) {
			getLogger().warning("Failed to start PluginMetrics :(");
		}
		
		// Warn about incorrect config setup.
		// Using the console sender because it supports coloration. 
		ConfigValidation.validateConfig(getConfig(), getServer()
				.getConsoleSender());
	}

	@Override
	public void onDisable() {
		this.getServer().getMessenger()
				.unregisterIncomingPluginChannel(this, INIT_CHANNEL_NAME);
		this.getServer().getMessenger()
				.unregisterOutgoingPluginChannel(this, CONTROL_CHANNEL_NAME);
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
				if (!sender.hasPermission("wdl.reloadConfig")) {
					sender.sendMessage("§cYou don't have permission!");
					return true;
				}
				
				if (args.length != 1) {
					return false;
				}
				
				reloadConfig();
				ConfigValidation.validateConfig(getConfig(), sender);
				
				updateAllPlayers();
				
				sender.sendMessage("§aWDL configuration reloaded.");
				return true;
			}
			if (args[0].equalsIgnoreCase("update")) {
				if (!sender.hasPermission("wdl.updatePlayer")) {
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
				if (!sender.hasPermission("wdl.updatePlayer")) {
					sender.sendMessage("§cYou don't have permission!");
					return true;
				}
				
				if (args.length != 1) {
					return false;
				}
				
				int updatedCount = updateAllPlayers();
				
				sender.sendMessage("§aUpdated the WDL permissions of " + 
						updatedCount + " players.");
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
						"They are running a version of WDL before 1.8d.");
			} else {
				try {
					String version = new String(data, "UTF-8");
					getLogger().info(
							"They are running WDL version " + version + ".");
				} catch (UnsupportedEncodingException e) {
					throw new Error(":(", e);
				}
			}
			
			updatePlayer(player);
		}
	}

	/**
	 * Gets the download radius a player has, based off of their permissions and
	 * the <code>saveRadius</code> value in the config file.
	 * 
	 * @param player
	 *            The player to get the download radius of.
	 * @return The download radius applicable to that player.
	 */
	private int getSaveRadius(Player player) {
		if (player.hasPermission("wdl.fullDownloadRadius")) {
			return -1;
		}
		
		int configDownloadRadius;
		
		String worldConfigKey = "wdl.per-world." + 
				player.getWorld().getName() + ".saveRadius";
		if (getConfig().isInt(worldConfigKey)) {
			configDownloadRadius = getConfig().getInt(worldConfigKey);
		} else {
			configDownloadRadius = getConfig().getInt("wdl.saveRadius");
		}
		
		if (configDownloadRadius <= -1) {
			return -1;
		}

		return configDownloadRadius;
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
	private boolean getWorldConfigValue(World world, String key) {
		String worldName = world.getName();
		String worldKey = "wdl.per-world." + worldName + "." + key;
		if (getConfig().isBoolean(worldKey)) {
			return getConfig().getBoolean(worldKey);
		}
		
		return getConfig().getBoolean("wdl." + key);
	}
	
	/**
	 * Gets a value from the config for the given player, unless it is
	 * overwritten.
	 * 
	 * @param player
	 *            The player to check permissions for.
	 * @param configKey
	 *            The key in the config to check.  It should be in the 
	 *            format of "canDoNewThings", not "wdl.canDoNewThings". 
	 * @param overridePerm
	 *            The permission that overrides the config value.  Unlike
	 *            the configKey, this should be fully qualified, for
	 *            example "wdl.canDoNewThings".
	 * @return The value from the config or permissions.
	 */
	private boolean getConfigValue(Player player, String configKey,
			String overridePerm) {
		if (player.hasPermission(overridePerm)) {
			return true;
		} else {
			return getWorldConfigValue(player.getWorld(), configKey);
		}
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
	 * Creates the byte arrays for all of the WDL packets.
	 * 
	 * @param player
	 * @return
	 */
	private byte[][] createWDLPackets(Player player) {
		byte[][] packets = new byte[3][];
		
		//Packet #1
		boolean globalIsEnabled = getConfigValue(player,
				"canDownloadInGeneral", "wdl.overrideCanDownloadInGeneral");
		int saveRadius = getSaveRadius(player);
		boolean cacheChunks = getConfigValue(player, "canCacheChunks",
				"wdl.overrideCanCacheChunks");
		boolean saveEntities = getConfigValue(player, "canSaveEntities",
				"wdl.overrideCanSaveEntities");
		boolean saveTileEntities = getConfigValue(player,
				"canSaveTileEntities", "wdl.overrideCanSaveTileEntities");
		boolean saveContainers = getConfigValue(player,
				"canSaveContainers", "wdl.overrideCanSaveContainers");

		packets[1] = WDLPackets.createWDLPacket1(globalIsEnabled, saveRadius, cacheChunks,
				saveEntities, saveTileEntities, saveContainers);
		
		//Packet #0
		boolean canDoNewThings = getConfigValue(player, 
				"canDoNewThings", "wdl.overrideCanDoNewThings");
		packets[0] = WDLPackets.createWDLPacket0(canDoNewThings && globalIsEnabled);
		
		//Packet #2
		Map<String, Integer> entityMap = new HashMap<>();
		if (globalIsEnabled && saveEntities && getConfigValue(player,
				"sendEntityRanges", "wdl.overrideSendEntityRanges")) {
			entityMap.putAll(getEntityRanges(player));
		}
		packets[2] = WDLPackets.createWDLPacket2(entityMap);
		
		return packets;
	}
}
