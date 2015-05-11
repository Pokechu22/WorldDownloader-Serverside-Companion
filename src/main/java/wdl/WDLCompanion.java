package wdl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
public class WDLCompanion extends JavaPlugin implements Listener {
	/**
	 * The name of the plugin channel used for WDL control.
	 */
	private static final String CONTROL_CHANNEL_NAME = "WDL|CONTROL";

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		this.getServer().getMessenger()
				.registerOutgoingPluginChannel(this, CONTROL_CHANNEL_NAME);

		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		this.getServer().getMessenger()
				.unregisterOutgoingPluginChannel(this, CONTROL_CHANNEL_NAME);
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		Player player = e.getPlayer();

		if (e.getPlayer().getListeningPluginChannels()
				.contains(CONTROL_CHANNEL_NAME)) {
			getLogger().info(
					"Player " + e.getPlayer().toString()
							+ " connected with WDL enabled!");
		}

		player.sendPluginMessage(
				this,
				CONTROL_CHANNEL_NAME,
				createWDLPacket(
						player.hasPermission("wdl.canDownloadInGeneral"),
						getDownloadRadius(player),
						player.hasPermission("wdl.cacheChunks"),
						player.hasPermission("wdl.saveEntities"),
						player.hasPermission("wdl.saveTileEntities"),
						player.hasPermission("wdl.saveContainers")));
	}

	/**
	 * Gets the download radius a player has, based off of their permissions and
	 * the <code>saveRadius</code> value in the config file.
	 * 
	 * @param player
	 *            The player to get the download radius of.
	 * @return The download radius applicable to that player.
	 */
	private int getDownloadRadius(Player player) {
		final int configDownloadRadius = getConfig().getInt("saveRadius", -1);
		final int serverViewDistance = getServer().getViewDistance();

		if (configDownloadRadius <= -1) {
			return serverViewDistance;
		}

		if (player.hasPermission("wdl.fullDownloadRadius")) {
			return serverViewDistance;
		} else {
			return configDownloadRadius;
		}
	}

	/**
	 * Creates a byte array for the WDL control packet.
	 * 
	 * @param globalIsEnabled
	 *            Whether or not all of WDL is enabled.
	 * @param radius
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
	private final byte[] createWDLPacket(boolean globalIsEnabled, int radius,
			boolean cacheChunks, boolean saveEntities,
			boolean saveTileEntities, boolean saveContainers) {
		final int VERSION = 1;

		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		output.writeInt(VERSION);

		output.writeBoolean(globalIsEnabled);
		output.writeInt(radius);
		output.writeBoolean(cacheChunks && globalIsEnabled);
		output.writeBoolean(saveEntities && globalIsEnabled);
		output.writeBoolean(saveTileEntities && globalIsEnabled);
		output.writeBoolean(saveContainers && saveTileEntities
				&& globalIsEnabled);

		return output.toByteArray();
	}
}
