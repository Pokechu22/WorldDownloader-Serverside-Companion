package wdl.request;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import wdl.WDLCompanion;
import wdl.range.ProtectionRange;

/**
 * Keeps track of requests.
 */
public class RequestManager implements Listener {
	private final WDLCompanion plugin;
	
	public RequestManager(WDLCompanion plugin) {
		this.plugin = plugin;
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	/**
	 * Active requests, by player name.
	 * 
	 * Player names are lowercased first to help with finding the right player.
	 */
	private Map<String, PermissionsRequestedEvent> requests = new HashMap<>();
	
	@EventHandler(priority=EventPriority.LOWEST)
	private void onRequest(PermissionsRequestedEvent event) {
		plugin.getLogger().info("Received request: " + event.toString());
		plugin.getLogger().info("Requested permissions: ");
		for (Map.Entry<String, String> e : event.getRequestedPermissions().entrySet()) {
			plugin.getLogger().info(" * " + e.getKey() + ": " + e.getValue());
		}
		plugin.getLogger().info("Range requests: ");
		for (ProtectionRange range : event.getRangeRequests()) {
			plugin.getLogger().info(" * " + range.toString());
		}
		plugin.getLogger().info("Request reason: " + event.getRequestReason());
		
		requests.put(event.getPlayerName().toLowerCase(), event);
	}
	
	/**
	 * Gets the request for the given player, or <code>null</code> if they have
	 * none.
	 * 
	 * @param player The name of the player (case insensitive).
	 * @return player's request
	 */
	public PermissionsRequestedEvent getPlayerRequest(String player) {
		return requests.get(player.toLowerCase());
	}
}
