package wdl.request;

import org.bukkit.event.HandlerList;

/**
 * Event for when a previously-accepted request has been revoked.
 */
public class RequestRevokedEvent extends RequestEvent {
	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
