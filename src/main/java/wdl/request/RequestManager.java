package wdl.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

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
	
	public static void addRequest(PermissionRequest request) {
		requestsByName.put(request.playerName.toLowerCase(), request);
		requestsById.put(request.playerId, request);
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
}
