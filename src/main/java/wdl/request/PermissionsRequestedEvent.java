package wdl.request;

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
	public final List<ProtectionRange> getRangeRequests() {
		return rangeRequests;
	}
	
	/**
	 * Accept this request, firing the needed events and packets.
	 * 
	 * This method calls the abstract {@link #doAccept()} method.
	 */
	public final void acceptRequest() {
		doAccept();
		//TODO: Events, etc...
	}
	
	/**
	 * Reject this request, firing the needed events and packets.
	 * 
	 * This method calls the abstract {@link #doReject()} method.
	 */
	public final void rejectRequest() {
		doReject();
		//TODO: Events, etc...
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
	 * Gets the EXACT name of the player requesting permissions.  This
	 * is used as an ID.
	 * 
	 * @return A player name, as would be found via the
	 *         {@link org.bukkit.entity.Player#getName()} method.
	 */
	public abstract String getPlayerName();
	
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
	 * Has this event been forwarded from another server?  Events that were
	 * triggered on this server are not; events that were received from
	 * another server are (EG with bungeecord).
	 * 
	 * The return value of this method is used to determine if the event
	 * should be forwarded again; incorrect values may result in an infinite
	 * loop.
	 */
	public abstract boolean isForwarded();
	
	/**
	 * Internally accept the event; this involves sending the needed
	 * packets or such.
	 */
	protected abstract void doAccept();
	
	/**
	 * Internally reject the event; this involves sending the needed
	 * packets or such.
	 */
	protected abstract void doReject();
	
	@Override
	public String toString() {
		return getEventName() + ": " + getPlayerInfo() + " "
				+ getLocationInfo() + " is requesting " + requestedPerms.size()
				+ " perms and " + rangeRequests.size()
				+ " chunk overrides (run" + getTeleportCommand()
				+ " to get to them)";
	}
}
