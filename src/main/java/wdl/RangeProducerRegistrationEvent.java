package wdl;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import wdl.range.IRangeProducer;

/**
 * Event that is raised when {@link WDLCompanion} is ready to receive new
 * {@link IRangeProducer} registrations.
 */
public final class RangeProducerRegistrationEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private final WDLCompanion plugin;
	
	RangeProducerRegistrationEvent(WDLCompanion plugin) {
		super(false);
		
		this.plugin = plugin;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	/**
	 * Registers a new {@link IRangeProducer}, with the given name.
	 * 
	 * @param id
	 *            The ID to register under.
	 * @param rangeProducer
	 *            The {@link IRangeProducer} to register.
	 * @throws IllegalArgumentException
	 *             If there already is a registration for the given ID
	 * @throws IllegalArgumentException
	 *             If either id or rangeProducer are null.
	 */
	public void addRegistration(String id, IRangeProducer rangeProducer)
			throws IllegalArgumentException {
		if (id == null) {
			throw new IllegalArgumentException("id must not be null!");
		}
		if (rangeProducer == null) {
			throw new IllegalArgumentException("rangeProducer must not be null!");
		}
		if (plugin.rangeProducers.containsKey(id)) {
			throw new IllegalArgumentException(
					"RangeProducer already registered for id '" + id
							+ "'; tried to register " + rangeProducer
							+ " over it (currently registered: "
							+ plugin.rangeProducers.get(id) + ")");
		}
		plugin.rangeProducers.put(id, rangeProducer);
		plugin.getLogger().fine(
				"Registered IRangeProducer " + rangeProducer + " under '" + id
						+ "'.");
	}
}
