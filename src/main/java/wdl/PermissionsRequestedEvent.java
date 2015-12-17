package wdl;

import java.util.List;
import java.util.Map;

import org.bukkit.event.Event;

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
}
