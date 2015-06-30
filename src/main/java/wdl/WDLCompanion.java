package wdl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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
				public ConfigBooleanPlotter(String key) {
					super(getConfig().getBoolean(key) ? "true" : "false");
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
					sender.sendMessage("�cYou must be a player.");
					return true;
				}
				Player player = (Player)sender;
				if (!player.getListeningPluginChannels()
						.contains(CONTROL_CHANNEL_NAME)) {
					sender.sendMessage("�cYou don't have WDL installed!");
					sender.sendMessage("�c(not listening on "
							+ CONTROL_CHANNEL_NAME + ")");
					return true;
				}
				
				updatePlayer(player);
				sender.sendMessage("�aUpdated your WDL permissions.");
				
				return true;
			}
			if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("wdl.reloadConfig")) {
					sender.sendMessage("�cYou don't have permission!");
					return true;
				}
				
				if (args.length != 1) {
					return false;
				}
				
				reloadConfig();
				
				updateAllPlayers();
				
				sender.sendMessage("�aWDL configuration reloaded.");
				return true;
			}
			if (args[0].equalsIgnoreCase("update")) {
				if (!sender.hasPermission("wdl.updatePlayer")) {
					sender.sendMessage("�cYou don't have permission!");
					return true;
				}
				
				if (args.length != 2) {
					return false;
				}
				
				Player player = getServer().getPlayer(args[1]);
				if (player == null) {
					sender.sendMessage("�cThere is no player named " + 
							args[1] + ".");
					return true;
				}
				
				if (!player.getListeningPluginChannels()
						.contains(CONTROL_CHANNEL_NAME)) {
					sender.sendMessage("�c" + player.getDisplayName() + 
							" doesn't have WDL installed!");
					sender.sendMessage("�c(not listening on "
							+ CONTROL_CHANNEL_NAME + ")");
					return true;
				}
				
				updatePlayer(player);
				sender.sendMessage("�aUpdated " + player.getDisplayName() +
						"'s WDL permissions.");
				
				return true;
			}
			if (args[0].equalsIgnoreCase("updateall")) {
				if (!sender.hasPermission("wdl.updatePlayer")) {
					sender.sendMessage("�cYou don't have permission!");
					return true;
				}
				
				if (args.length != 1) {
					return false;
				}
				
				int updatedCount = updateAllPlayers();
				
				sender.sendMessage("�aUpdated the WDL permissions of " + 
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
					" has WDL enabled.");
			
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
		final int configDownloadRadius = getConfig().getInt("wdl.saveRadius", -1);
		
		if (configDownloadRadius <= -1) {
			return -1;
		}

		if (player.hasPermission("wdl.fullDownloadRadius")) {
			return -1;
		} else {
			return configDownloadRadius;
		}
	}

	/**
	 * Gets a value from the config for the given player, unless it is
	 * overwritten.
	 * 
	 * @param player
	 *            The player to check permissions for.
	 * @param configKey
	 *            The key in the config to check.
	 * @param overridePerm
	 *            The permission that overrides the config value.
	 * @return The value from the config or permissions.
	 */
	private boolean getConfigValue(Player player, String configKey,
			String overridePerm) {
		if (player.hasPermission(overridePerm)) {
			return true;
		} else {
			return getConfig().getBoolean(configKey);
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
		Map<String, Integer> ranges = new HashMap<>();
		
		try {
			int animalRange, monsterRange, miscRange, otherRange;
			
			File configFile = new File(getServer().getWorldContainer()
					.getParentFile(), "spigot.yml");
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
			//Ignore it; server probably isn't running spigot.
			System.err.println("Ex in entityRanges: " + e);
			e.printStackTrace();
			ranges.clear();
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
		byte[][] packets = new byte[3][];
		
		//Packet #0
		boolean canDoNewThings = getConfigValue(player, 
				"wdl.canDoNewThings", "wdl.overrideCanDoNewThings");
		packets[0] = createWDLPacket0(canDoNewThings);
		
		//Packet #1
		boolean globalIsEnabled = getConfigValue(player,
				"wdl.canDownloadInGeneral", "wdl.overrideCanDownloadInGeneral");
		int saveRadius = getSaveRadius(player);
		boolean cacheChunks = getConfigValue(player, "wdl.canCacheChunks",
				"wdl.overrideCanCacheChunks");
		boolean saveEntities = getConfigValue(player, "wdl.canSaveEntities",
				"wdl.overrideCanSaveEntities");
		boolean saveTileEntities = getConfigValue(player,
				"wdl.canSaveTileEntities", "wdl.overrideCanSaveTileEntities");
		boolean saveContainers = getConfigValue(player,
				"wdl.canSaveContainers", "wdl.overrideCanSaveContainers");

		packets[1] = createWDLPacket1(globalIsEnabled, saveRadius, cacheChunks,
				saveEntities, saveTileEntities, saveContainers);
		//Packet #2
		Map<String, Integer> entityMap = new HashMap<>();
		if (saveEntities && getConfigValue(player,
				"wdl.sendEntityRanges", "wdl.overrideSendEntityRanges")) {
			entityMap.putAll(getEntityRanges(player));
		}
		packets[2] = createWDLPacket2(entityMap);
		
		return packets;
	}

	/**
	 * Creates a byte array for the WDL control packet #0.
	 * 
	 * @param canDoNewThings
	 *            Whether players can use new functions that aren't known to
	 *            this plugin.
	 * @return
	 */
	private byte[] createWDLPacket0(boolean canDoNewThings) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		output.writeInt(0);
		
		output.writeBoolean(canDoNewThings);
		
		return output.toByteArray();
	}

	/**
	 * Creates a byte array for the WDL control packet #1.
	 * 
	 * @param globalIsEnabled
	 *            Whether or not all of WDL is enabled.
	 * @param saveRadius
	 *            The distance of chunks that WDL can download from the player's
	 *            position.
	 * @param cacheChunks
	 *            Whether or not chunks that the player previously entered but
	 *            since exited are to be saved. If <code>false</code>, only the
	 *            currently loaded chunks will be saved; if <code>true</code>,
	 *            the player will download terrain as they move.
	 * @param saveEntities
	 *            Whether or not entities and their appearance are to be saved.
	 *            This includes Minecart Chest contents.
	 * @param saveTileEntities
	 *            Whether or not tile entities (General ones that are reloaded
	 *            such as signs and banners, as well as chests) are to be saved.
	 *            If <code>false</code>, no tile entities will be saved. If
	 *            <code>true</code>, they will be saved.)
	 * @param saveContainers
	 *            Whether or not container tile entities are to be saved. If
	 *            <code>saveTileEntities</code> is <code>false</code>, this
	 *            value is ignored and treated as false. If this value is
	 *            <code>false</code>, then container tile entities (ones that
	 *            players need to open to save) will not be opened. If this
	 *            value is <code>true</code>, then said tile entities can be
	 *            saved by players as they are opened.
	 * @return The byte array used for creating that plugin channel message.
	 */
	private byte[] createWDLPacket1(boolean globalIsEnabled, int saveRadius,
			boolean cacheChunks, boolean saveEntities,
			boolean saveTileEntities, boolean saveContainers) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		output.writeInt(1);

		output.writeBoolean(globalIsEnabled);
		output.writeInt(saveRadius);
		output.writeBoolean(cacheChunks && globalIsEnabled);
		output.writeBoolean(saveEntities && globalIsEnabled);
		output.writeBoolean(saveTileEntities && globalIsEnabled);
		output.writeBoolean(saveContainers && saveTileEntities
				&& globalIsEnabled);

		return output.toByteArray();
	}
	
	/**
	 * Creates the WDL packet #2.  This packet contains the server's
	 * tracking ranges for entities.  (If the server doesn't run spigot, or
	 * the player receives a 0-length list).  WDL uses this data to know when
	 * an entity leaves the range and thus should be saved.
	 * <br/>
	 * Its structure is simply an int, giving the number of entries,
	 * and then a series of strings and ints containing the tracking
	 * ranges.  The string value is the entity's savegame name, and the
	 * int is the tracking range.
	 * 
	 * @return
	 */
	private byte[] createWDLPacket2(Map<String, Integer> ranges) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		
		output.writeInt(2);
		
		output.writeInt(ranges.size());
		
		for (Map.Entry<String, Integer> e : ranges.entrySet()) {
			output.writeUTF(e.getKey());
			output.writeInt(e.getValue());
		}
		
		return output.toByteArray();
	}
}
