package wdl.range;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import wdl.RangeProducerRegistrationEvent;

/**
 * Something that converts a configuration file into a series of
 * {@link ChunkRange}s.
 * 
 * To register a new instance with the plugin, subscribe to the
 * {@link RangeProducerRegistrationEvent} event and use its
 * {@link RangeProducerRegistrationEvent#addRegistration(String, IRangeProducer)
 * addRegistration} method.
 */
public interface IRangeProducer {
	/**
	 * Gets the ranges for the given player. This will <b>only</b> called if
	 * {@link #isValidConfig(ConfigurationSection, List, List)} returned true.
	 * 
	 * @param player
	 *            The player to get ranges for.
	 * @param config
	 *            The configuration to use.
	 * @return A list of {@link ChunkRange}s.
	 */
	public abstract List<ChunkRange> getRanges(Player player,
			ConfigurationSection config);
	
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
}
