package wdl.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import wdl.WDLCompanion;
import wdl.range.ProtectionRange;

/**
 * Keeps track of requests.
 */
public class RequestManager {
	@Deprecated
	private RequestManager() {
		throw new AssertionError();
	}
	
	/**
	 * Active requests, by player name.
	 * 
	 * Player names are lowercased first to help with finding the right player.
	 */
	private static Map<String, PermissionRequest> requestsByName = new HashMap<>();
	/**
	 * Active requests, by player ID.
	 */
	private static Map<UUID, PermissionRequest> requestsById = new HashMap<>();
	
	public static void addRequest(PermissionRequest request, WDLCompanion plugin) {
		if (requestsById.containsKey(request.playerId)) {
			PermissionRequest oldRequest = requestsById.get(request.playerId);
			Player player = Bukkit.getPlayer(oldRequest.playerId);
			if (player != null) {
				plugin.getLogger().info(
						request.playerName + " overwrote their old request.");
				plugin.getLogger().info("Their old request was in state " + oldRequest.state);
				plugin.getLogger().info("It would expire at " + oldRequest.expirationTime);
				plugin.getLogger().info("Permissions (" + request.requestedPerms.size() + "):");
				for (Map.Entry<String, String> e : request.requestedPerms.entrySet()) {
					plugin.getLogger().info(" * " + e.getKey() + "=" + e.getValue());
				}
				plugin.getLogger().info("Ranges (" + request.rangeRequests.size() + "):");
				for (ProtectionRange range : request.rangeRequests) {
					plugin.getLogger().info(" * " + range);
				}
				
				if (oldRequest.state == PermissionRequest.State.WAITING
						|| oldRequest.state == PermissionRequest.State.ACCEPTED) {
					player.sendMessage("You withdrew your old permission request.");
					oldRequest.state = PermissionRequest.State.WITHDRAWN;
				}
				
				if (request.expireTask != null) {
					request.expireTask.cancel();
				}
				
				plugin.updatePlayer(player);
				plugin.requestRangeProducer.removeRanges(player, request.rangeRequests);
			}
		}
		
		requestsByName.put(request.playerName.toLowerCase(), request);
		requestsById.put(request.playerId, request);
		
		plugin.getLogger().info(
				request.playerName + " submitted a new permission request.");
		plugin.getLogger().info("Request reason: " + request.requestReason);
		plugin.getLogger().info("Permissions (" + request.requestedPerms.size() + "):");
		for (Map.Entry<String, String> e : request.requestedPerms.entrySet()) {
			plugin.getLogger().info(" * " + e.getKey() + "=" + e.getValue());
		}
		plugin.getLogger().info("Ranges (" + request.rangeRequests.size() + "):");
		for (ProtectionRange range : request.rangeRequests) {
			plugin.getLogger().info(" * " + range);
		}
	}
	
	/**
	 * Gets the request for the given player, or <code>null</code> if they have
	 * none.
	 * 
	 * @param player The name of the player (case insensitive).
	 * @return player's request
	 */
	public static PermissionRequest getPlayerRequest(String player) {
		return requestsByName.get(player.toLowerCase());
	}
	
	/**
	 * Gets the request for the given player, or <code>null</code> if they have
	 * none.
	 * 
	 * @param player The player
	 * @return player's request
	 */
	public static PermissionRequest getPlayerRequest(Player player) {
		return requestsById.get(player.getUniqueId());
	}
	
	public static List<PermissionRequest> getRequests() {
		return new ArrayList<>(requestsById.values());
	}
	
	//TODO: I'm not happy with the way that this class is coupled to the plugin
	//instance - I should either make methods package-level, or the class non-
	//static, or eliminate the plugin argument...
	
	/**
	 * Accepts the given permission request.
	 * 
	 * @param durationSeconds
	 * @param request
	 * @param plugin
	 */
	public static void acceptRequest(long durationSeconds,
			PermissionRequest request, WDLCompanion plugin) {
		if (request.state != PermissionRequest.State.WAITING) {
			throw new IllegalArgumentException("request is in invalid state!  " +
					"The state must be 'WAITING'; it actually was " + request.state);
		}
		plugin.getLogger().info(request.playerName + "'s request has been accepted.");
		plugin.getLogger().info("It will expire in " + durationSeconds + " seconds.");
		plugin.getLogger().info("Permissions (" + request.requestedPerms.size() + "):");
		for (Map.Entry<String, String> e : request.requestedPerms.entrySet()) {
			plugin.getLogger().info(" * " + e.getKey() + "=" + e.getValue());
		}
		plugin.getLogger().info("Ranges (" + request.rangeRequests.size() + "):");
		for (ProtectionRange range : request.rangeRequests) {
			plugin.getLogger().info(" * " + range);
		}
		
		//TODO: Allow changing the amount of time a request lasts.
		request.expirationTime = System.currentTimeMillis()
				+ (durationSeconds * 1000);
		
		RequestCleanupTask task = new RequestCleanupTask(request, plugin);
		request.expireTask = task.runTaskLater(plugin, durationSeconds * 20);
		
		if (request.requestedPerms.size() > 0) {
			plugin.updatePlayer(Bukkit.getPlayer(request.playerId));
		}
		if (request.rangeRequests.size() > 0) {
			plugin.requestRangeProducer.addRanges(Bukkit.getPlayer(request.playerId),
					durationSeconds * 20, request.rangeRequests);
		}
	}
	
	public static void rejectRequest(PermissionRequest request, WDLCompanion plugin) {
		if (request.state != PermissionRequest.State.WAITING) {
			throw new IllegalArgumentException("request is in invalid state!  " +
					"The state must be 'WAITING'; it actually was " + request.state);
		}
		
		request.state = PermissionRequest.State.REJECTED;
	}
	
	public static void revokeRequest(PermissionRequest request, WDLCompanion plugin) {
		if (request.state != PermissionRequest.State.ACCEPTED) {
			throw new IllegalArgumentException("request is in invalid state!  " +
					"The state must be 'ACCEPTED'; it actually was " + request.state);
		}
		
		request.state = PermissionRequest.State.REVOKED;
		
		if (request.expireTask != null) {
			request.expireTask.cancel();
		}
		
		Player player = Bukkit.getPlayer(request.playerId);
		plugin.updatePlayer(player);
		plugin.requestRangeProducer.removeRanges(player, request.rangeRequests);
	}
	
	private static class RequestCleanupTask extends BukkitRunnable {
		public RequestCleanupTask(PermissionRequest request, WDLCompanion plugin) {
			this.request = request;
			this.plugin = plugin;
		}
		
		public final PermissionRequest request;
		private final WDLCompanion plugin;
		
		@Override
		public void run() {
			// Request has expired at this point.
			request.state = PermissionRequest.State.EXPIRED;
			Player player = Bukkit.getPlayer(request.playerId);
			if (player != null) {
				plugin.updatePlayer(player);
			}
			
			plugin.getLogger().info(request + " has expired.");
		}
	}
}
