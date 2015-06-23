package wdl;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
					.createGraph("Config: canDoNewThings");
			canDoNewThingsGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canDoNewThings"));
			
			Graph globalEnabledGraph = metrics
					.createGraph("Config: canDownloadInGeneral");
			globalEnabledGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canDownloadInGeneral"));

			Graph saveRadiusGraph = metrics.createGraph("Config: saveRadius");
			saveRadiusGraph.addPlotter(new Plotter(saveRadiusText) {
				@Override
				public int getValue() {
					return 1;
				}
			});

			Graph canCacheChunksGraph = metrics
					.createGraph("Config: canCacheChunks");
			canCacheChunksGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canCacheChunks"));

			Graph canSaveEntitiesGraph = metrics
					.createGraph("Config: canSaveEntities");
			canSaveEntitiesGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canSaveEntities"));

			Graph canSaveTileEntitiesGraph = metrics
					.createGraph("Config: canSaveTileEntities");
			canSaveTileEntitiesGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canSaveTileEntities"));

			Graph canSaveContainersGraph = metrics
					.createGraph("Config: canSaveContainers");
			canSaveContainersGraph.addPlotter(new ConfigBooleanPlotter(
					"wdl.canSaveContainers"));

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
				
				int updatedCount = 0;
				for (Player player : getServer().getOnlinePlayers()) {
					if (player.getListeningPluginChannels().contains(
							CONTROL_CHANNEL_NAME)) {
						updatePlayer(player);
						updatedCount++;
					}
				}
				
				sender.sendMessage("§aUpdated the WDL permissions of " + 
						updatedCount + " players.");
			}
		}
		
		return false;
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
	 * Creates the byte arrays for all of the WDL packets.
	 * 
	 * @param player
	 * @return
	 */
	private byte[][] createWDLPackets(Player player) {
		byte[][] packets = new byte[2][];
		
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
}
