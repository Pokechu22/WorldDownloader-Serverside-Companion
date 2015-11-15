package wdl.range;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

/**
 * Basic range group producer that takes parameters and uses those.
 */
public final class SimpleRangeProducer implements IRangeProducer {
	private final IRangeGroup group;
	/**
	 * Whether the created ranges are whitelisting or blacklisting.
	 */
	public final boolean whitelist;
	/**
	 * The tag to use for the created ranges.
	 */
	public final String tag;
	/**
	 * Coordinates for the created ranges.
	 */
	public final int x1, z1, x2, z2;
	
	/**
	 * Creates a new {@link SimpleRangeProducer}.
	 * 
	 * @param group The owning range group.
	 * @param whitelist Whether the created groups are whitelisting or blacklisting.
	 * @param tag The tag to use for the created groups.
	 * @param x1 X1 coordinate for the created groups.
	 * @param z1 Z1 coordinate for the created groups.
	 * @param x2 X2 coordinate for the created groups.
	 * @param z2 Z2 coordinate for the created groups.
	 */
	public SimpleRangeProducer(IRangeGroup group, boolean whitelist,
			String tag, int x1, int z1, int x2, int z2) {
		this.group = group;
		this.whitelist = whitelist;
		this.tag = tag;
		this.x1 = x1;
		this.z1 = z1;
		this.x2 = x2;
		this.z2 = z2;
	}
	
	@Override
	public List<ProtectionRange> getInitialRanges(Player player) {
		return Arrays.asList(new ProtectionRange(tag, whitelist, x1, z1, x2, z2));
	}
	
	@Override
	public IRangeGroup getRangeGroup() {
		return this.group;
	}
}
