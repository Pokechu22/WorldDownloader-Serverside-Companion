package wdl.request;

import org.bukkit.event.HandlerList;

/**
 * Event for when a player has been withdrawn.
 */
public class RequestWithdrawnEvent extends RequestEvent {
	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
