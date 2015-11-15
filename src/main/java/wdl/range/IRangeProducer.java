package wdl.range;

import java.util.List;

import org.bukkit.entity.Player;

/**
 * Something that generates {@link ProtectionRange}s for a player.
 */
public interface IRangeProducer {
	/**
	 * Gets the initial ranges for the given player. This is called when they
	 * first request permissions.
	 * 
	 * @param player
	 *            The player to get ranges for.
	 * @return A list of {@link ProtectionRange}s.
	 */
	public abstract List<ProtectionRange> getInitialRanges(Player player);

	/**
	 * Gets the range group that corresponds with this {@link IRangeProducer}.
	 * This group should be passed to and set in the constructor.
	 * 
	 * @return The corresponding {@link RangeGroup}.
	 */
	public abstract RangeGroup getRangeGroup();
}
