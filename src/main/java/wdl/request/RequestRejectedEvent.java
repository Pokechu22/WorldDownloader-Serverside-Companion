package wdl.request;

import org.bukkit.event.HandlerList;

/**
 * Event for when a request has been rejected.
 */
public class RequestRejectedEvent extends RequestEvent {
	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
