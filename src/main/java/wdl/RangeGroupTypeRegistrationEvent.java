package wdl;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.AuthorNagException;

import wdl.range.IRangeGroupType;

/**
 * Event that is raised when {@link WDLCompanion} is ready to receive new
 * {@link IRangeGroupType} registrations.
 */
public final class RangeGroupTypeRegistrationEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private final WDLCompanion plugin;
	/**
	 * Has the registration finished (and thus no more registrations should be
	 * allowed via this event?)
	 */
	private boolean finished = false;
	
	RangeGroupTypeRegistrationEvent(WDLCompanion plugin) {
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
	 * Registers a new {@link IRangeGroupType}, with the given name.
	 * 
	 * @param id
	 *            The ID to register under.
	 * @param rangeGroupType
	 *            The {@link IRangeGroupType} to register.
	 * @throws IllegalArgumentException
	 *             If there already is a registration for the given ID
	 * @throws IllegalArgumentException
	 *             If either id or rangeProducer are null.
	 */
	public void addRegistration(String id, IRangeGroupType<?> rangeGroupType)
			throws IllegalArgumentException {
		if (finished) {
			// Well, I'm not 100% sure if this is what AuthorNagException is for
			// but I think this will do.  It doesn't seem to be thrown anywhere
			// else (EVER), but it looks like bukkit uses it to write a log
			// message to go "nag" at the author of the plugin (with their name
			// as found in plugin.yml).  Seems perfect for something that should
			// not happen unless someone is TRYING to break WDLCompanion.
			throw new AuthorNagException(
					"Group type registrations have already finished!  Did you "
							+ "grab on to this event to try and add new "
							+ "registrations later on?  Please don't do "
							+ "that, thanks.  The config has already been "
							+ "parsed and your new type would be ignored.  "
							+ "(Attempted to add a IRangeGroupType, " 
							+ rangeGroupType + ", with an ID of " + id
							+ ", to WDLCompanion's rangeGroup map in a "
							+ "RangeGroupTypeRegistrationEvent)");
		}
		if (id == null) {
			throw new IllegalArgumentException("id must not be null!");
		}
		if (rangeGroupType == null) {
			throw new IllegalArgumentException("rangeProducer must not be null!");
		}
		if (plugin.registeredRangeGroupTypes.containsKey(id)) {
			throw new IllegalArgumentException(
					"RangeProducer already registered for id '" + id
							+ "'; tried to register " + rangeGroupType
							+ " over it (currently registered: "
							+ plugin.registeredRangeGroupTypes.get(id) + ")");
		}
		plugin.registeredRangeGroupTypes.put(id, rangeGroupType);
		plugin.getLogger().fine(
				"Registered IRangeProducer " + rangeGroupType + " under '" + id
						+ "'.");
	}
	
	/**
	 * Mark this event as finished, blocking any more registrations.
	 */
	void markFinished() {
		this.finished = true;
	}
}
