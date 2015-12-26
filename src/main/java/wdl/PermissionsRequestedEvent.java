package wdl;

import java.util.List;
import java.util.Map;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import wdl.range.ProtectionRange;

import com.google.common.collect.ImmutableMap;

/**
 * Base event class for when permissions are requested.
 * 
 * These will probably allow accepting the request / teleporting to the correct
 * player, but I'm not completely sure.
 */
public abstract class PermissionsRequestedEvent extends Event {
	protected PermissionsRequestedEvent(String requestReason,
			Map<String, String> requestedPerms,
			List<ProtectionRange> rangeRequests) {
		if (requestReason == null) {
			throw new IllegalArgumentException("requestReason must not be null!");
		}
		if (requestedPerms == null) {
			throw new IllegalArgumentException("requestedPerms must not be null!");
		}
		if (rangeRequests == null) {
			throw new IllegalArgumentException("rangeRequests must not be null!");
		}
		this.requestReason = requestReason;
		this.requestedPerms = requestedPerms;
		this.rangeRequests = rangeRequests;
	}
	
	private final String requestReason;
	private final Map<String, String> requestedPerms;
	private final List<ProtectionRange> rangeRequests;
	
	/**
	 * Gets the player-specified request message.
	 */
	public final String getRequestReason() {
		return requestReason;
	}
	
	/**
	 * Gets the map of permissions that the player requested.
	 */
	public final Map<String, String> getRequestedPermissions() {
		return ImmutableMap.copyOf(requestedPerms);
	}
	
	/**
	 * Gets the list of ProtectionRanges that the player requested.
	 */
	public List<ProtectionRange> getRangeRequests() {
		return rangeRequests;
	}
	
	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public final HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	/**
	 * Gets information about the location of the player requesting permissions.
	 * 
	 * For example, this might be "In world 'World' at 25 64 492", or
	 * "On server 'minigame3241' in world 'World_the_end' at 42 -32 12".
	 * 
	 * This information is displayed to the user.
	 */
	public abstract String getLocationInfo();
	
	/**
	 * Gets the EXACT name of the player requesting permissions.
	 * 
	 * @return A player name, as would be found via the
	 *         {@link org.bukkit.entity.Player#getName()} method.
	 */
	public abstract String getPlayerName();
	
	/**
	 * Gets some information about the player requesting permissions.
	 * 
	 * For instance, this might be "§9[Skylord]§aDashbar §r(dashbar)" - IE,
	 * using the display name and text name.
	 */
	public abstract String getPlayerInfo();
	
	/**
	 * Gets the text for a command an admin can execute to get to the player
	 * requesting permissions. For instance, this might be "/tp [playername]" or
	 * "/server minigame3241".
	 */
	public abstract String getTeleportCommand();
	
	/**
	 * Should this event be forwarded to other servers?  Events that were
	 * triggered on this server should be, but ones that were already
	 * forwarded (EG with bungeecord) should not be.
	 */
	public abstract boolean shouldForward();
	
	@Override
	public String toString() {
		return getEventName() + ": " + getPlayerInfo() + " "
				+ getLocationInfo() + " is requesting " + requestedPerms.size()
				+ " perms and " + rangeRequests.size()
				+ " chunk overrides (run" + getTeleportCommand()
				+ " to get to them)";
	}
}
