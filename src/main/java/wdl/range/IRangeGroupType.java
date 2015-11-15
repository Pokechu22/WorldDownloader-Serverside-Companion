package wdl.range;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

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
			RangeGroup group, ConfigurationSection config);
}
