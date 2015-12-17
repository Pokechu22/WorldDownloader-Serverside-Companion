package wdl;

import java.util.List;
import java.util.Map;

import org.bukkit.event.HandlerList;

import wdl.range.ProtectionRange;

/**
 * Permissions were requested via a WDL|REQUEST plugin message.
 */
public class PluginChannelPermissionsRequestedEvent extends
		PermissionsRequestedEvent {
	public PluginChannelPermissionsRequestedEvent(String requestReason,
			Map<String, String> requestedPerms,
			List<ProtectionRange> rangeRequests) {
		super(requestReason, requestedPerms, rangeRequests);
	}

	//TODO: Put in the custom logic here.
	
	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
