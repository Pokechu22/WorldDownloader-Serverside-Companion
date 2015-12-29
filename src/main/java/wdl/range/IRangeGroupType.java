package wdl.range;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * A type for range groups that can be generated via the configuration.
 * <br/>
 * Acts as a factory for {@link IRangeProducer}s.
 */
public interface IRangeGroupType<T extends IRangeProducer> {
	/**
	 * Validates that the given {@link ConfigurationSection} is acceptable.
	 * 
	 * @param config
	 *            The config to check.
	 * @param warnings
	 *            A list to put warnings into if if any occur.
	 * @param errors
	 *            A list to put errors into if if any occur.
	 * @return <code>false</code> if the configuration is fatally unacceptable
	 *         to the point where this {@link IRangeProducer} should not
	 *         use it.  Note that other {@link ConfigurationSection}s may still
	 *         be used with this {@link IRangeProducer}.
	 */
	public abstract boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors);
	
	/**
	 * Creates a new {@link IRangeProducer} from the given configuration
	 * section. Will only be called if
	 * {@link #isValidConfig(ConfigurationSection, List, List)} returned true.
	 * 
	 * @param config
	 *            The config to check.
	 */
	public abstract T createRangeProducer(
			IRangeGroup group, ConfigurationSection config);
	
	/**
	 * Disposes of this {@link IRangeGroupType}.  Events should be unregistered,
	 * etc.  Note that this should NOT dispose any created {@link IRangeProducer}s;
	 * they will be handled manually.
	 * 
	 * After this method has been called, this {@link IRangeGroupType} instance
	 * will no longer be used.  In most cases, a new instance will be created
	 * (potentially with a different configuration).
	 * 
	 * Note: To unregister all events in a {@link Listener}, you can call
	 * {@link HandlerList#unregisterAll(Listener)}.
	 */
	public abstract void dispose();
}
