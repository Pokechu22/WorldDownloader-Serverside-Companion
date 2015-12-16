package wdl;

import java.util.Map;

import org.bukkit.event.Event;

import com.google.common.collect.ImmutableMap;

/**
 * Base event class for when permissions are requested.
 * 
 * These will probably allow accepting the request / teleporting to the correct
 * player, but I'm not completely sure.
 */
public abstract class PermissionsRequestedEvent extends Event {
	protected PermissionsRequestedEvent(Map<String, String> requestedPerms) {
		this.requestedPerms = requestedPerms;
	}
	
	private final Map<String, String> requestedPerms;
	
	public final Map<String, String> getRequestedPermissions() {
		return ImmutableMap.copyOf(requestedPerms);
	}
}
