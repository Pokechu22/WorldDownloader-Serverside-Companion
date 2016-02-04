package wdl.request;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wdl.range.ProtectionRange;

/**
 * Represents a single request.
 */
public class PermissionRequest {
	/**
	 * Different states a request can be in.
	 */
	public static enum State {
		/**
		 * The request is waiting on moderator action.
		 */
		WAITING(ChatColor.YELLOW),
		/**
		 * The request has been accepted and the player granted the right permissions.
		 */
		ACCEPTED(ChatColor.GREEN),
		/**
		 * The request has been rejected by a moderator.  (Currently unused)
		 */
		REJECTED(ChatColor.RED),
		/**
		 * The request has been withdrawn by the submitting player (either
		 * directly or by creating a new request).  (Currently unused)
		 */
		WITHDRAWN(ChatColor.GRAY, ChatColor.ITALIC);
		
		public final String prefix;
		
		private State(ChatColor... colors) {
			StringBuilder builder = new StringBuilder();
			for (ChatColor color : colors) {
				builder.append(color);
			}
			prefix = builder.toString();
		}
		
		@Override
		public String toString() {
			return prefix;
		}
	}
	
	/**
	 * Current state of this request.
	 */
	public State state;
	/**
	 * Unique ID of the requesting player.
	 */
	public final UUID playerId;
	/**
	 * Name of the requesting player.
	 */
	public final String playerName;
	/**
	 * The reason for the request from the given player.
	 */
	public final String requestReason;
	/**
	 * The permissions that were requested.
	 */
	public final Map<String, String> requestedPerms;
	/**
	 * The ranges that were requested.
	 */
	public final List<ProtectionRange> rangeRequests;
	
	public PermissionRequest(Player player, String requestReason,
			Map<String, String> requestedPerms,
			List<ProtectionRange> rangeRequests) {
		this.state = State.WAITING;
		
		this.playerId = player.getUniqueId();
		this.playerName = player.getName();
		this.requestReason = requestReason;
		this.requestedPerms = ImmutableMap.copyOf(requestedPerms);
		this.rangeRequests = ImmutableList.copyOf(rangeRequests);
	}
	
	@Override
	public String toString() {
		return playerName + ": " + requestedPerms.size() + " perm(s), " + rangeRequests.size() + " range(s)";
	}
}
