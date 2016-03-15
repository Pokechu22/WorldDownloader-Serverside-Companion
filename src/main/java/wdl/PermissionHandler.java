package wdl;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import wdl.request.PermissionRequest;
import wdl.request.RequestManager;

/**
 * Handles checking if a player has permission.
 * 
 * @author Pokechu22
 *
 */
public class PermissionHandler {
	private WDLCompanion plugin;
	
	public PermissionHandler(WDLCompanion plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Can the given player download at all in all chunks?
	 */
	public boolean getCanDownloadInGeneral(Player player) {
		if (player.hasPermission("wdl.override.canDownloadInGeneral")) {
			return true;
		}
		if (getRequestedBoolean(player, "downloadInGeneral")) {
			return true;
		}
		
		return getPerworldBoolean(player, "canDownloadInGeneral");
	}
	
	/**
	 * Can the given player cache chunks as they move about the world?
	 */
	public boolean getCanCacheChunks(Player player) {
		if (player.hasPermission("wdl.override.canCacheChunks")) {
			return true;
		}
		if (getRequestedBoolean(player, "cacheChunks")) {
			return true;
		}
		
		return getPerworldBoolean(player, "canCacheChunks");
	}
	
	/**
	 * Can the given player save entities?
	 */
	public boolean getCanSaveEntities(Player player) {
		if (player.hasPermission("wdl.override.canSaveEntities")) {
			return true;
		}
		if (getRequestedBoolean(player, "saveEntities")) {
			return true;
		}
		
		return getPerworldBoolean(player, "canSaveEntities");
	}
	
	/**
	 * Can the given player save tile entities?
	 */
	public boolean getCanSaveTileEntities(Player player) {
		if (player.hasPermission("wdl.override.canSaveTileEntities")) {
			return true;
		}
		if (getRequestedBoolean(player, "saveTileEntities")) {
			return true;
		}
		
		return getPerworldBoolean(player, "canSaveTileEntities");
	}
	
	/**
	 * Can the given player save containers (e.g. chests)?
	 */
	public boolean getCanSaveContainers(Player player) {
		if (player.hasPermission("wdl.override.canSaveContainers")) {
			return true;
		}
		if (getRequestedBoolean(player, "saveContainers")) {
			return true;
		}
		
		return getPerworldBoolean(player, "canSaveContainers");
	}
	
	/**
	 * Can the given player use features of World Downloader that were not
	 * released when this version was released?
	 */
	public boolean getCanDoNewThings(Player player) {
		if (player.hasPermission("wdl.override.canDoNewThings")) {
			return true;
		}
		// No request value for this. 
		
		return getPerworldBoolean(player, "canDoNewThings");
	}
	
	/**
	 * Should the given player receive entity range data?
	 */
	public boolean getSendEntityRanges(Player player) {
		if (player.hasPermission("wdl.override.sendEntityRanges")) {
			return true;
		}
		if (getRequestedBoolean(player, "getEntityRanges")) {
			return true;
		}
		
		return getPerworldBoolean(player, "sendEntityRanges");
	}
	
	/**
	 * Gets the distance (in chunks) that the given player can download from
	 * themselves.
	 * 
	 */
	public int getSaveRadius(Player player) {
		if (player.hasPermission("wdl.override.maxSaveRadius")) {
			return -1;
		}
		
		PermissionRequest request = RequestManager.getPlayerRequest(player);
		if (request != null) {
			if (request.state == PermissionRequest.State.ACCEPTED) {
				String result = request.requestedPerms.get("saveRadius");
				if (result != null) {
					try {
						return Integer.parseInt(result);
					} catch (NumberFormatException e) {
						plugin.getLogger().log(Level.WARNING,
								"Failed to parse requested value for " + player
										+ ": '" + result + "'", e);
						// continue on to the per-world values.
					}
				}
			}
		}
		
		int configDownloadRadius;
		
		String worldConfigKey = "wdl.per-world." + 
				player.getWorld().getName() + ".saveRadius";
		if (plugin.getConfig().isInt(worldConfigKey)) {
			configDownloadRadius = plugin.getConfig().getInt(worldConfigKey);
		} else {
			configDownloadRadius = plugin.getConfig().getInt("wdl.saveRadius");
		}
		
		if (configDownloadRadius <= -1) {
			return -1;
		}

		return configDownloadRadius;
	}
	
	/**
	 * Gets a boolean from the configuration, using the overriden value
	 * from the given world if necessary.
	 */
	private boolean getPerworldBoolean(Player player, String configKey) {
		String worldName = player.getWorld().getName();
		String worldKey = "wdl.per-world." + worldName + "." + configKey;
		if (plugin.getConfig().isBoolean(worldKey)) {
			return plugin.getConfig().getBoolean(worldKey);
		}
		
		return plugin.getConfig().getBoolean("wdl." + configKey);
	}
	
	/**
	 * If the given player has requested the given permission, and their request
	 * was accepted, returns true.
	 */
	private boolean getRequestedBoolean(Player player, String key) {
		PermissionRequest request = RequestManager.getPlayerRequest(player);
		if (request == null) {
			return false;
		}
		if (request.state != PermissionRequest.State.ACCEPTED) {
			return false;
		}
		String result = request.requestedPerms.get(key);
		return result != null && result.equals("true");
	}
}
