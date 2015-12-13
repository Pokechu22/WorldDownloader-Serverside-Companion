package wdl.range;

import java.util.List;

import org.bukkit.entity.Player;

/**
 * Interface for range groups.  Plugins should NOT implement this;
 * this is used to create and delegate packets.
 */
public interface IRangeGroup {

	/**
	 * Gets the name of this group.
	 *
	 * @return {@link #groupName}
	 */
	public abstract String getGroupName();

	/**
	 * Add the given ranges to this range group. It is legal to call this method
	 * asynchronously.
	 * 
	 * @param player
	 *            The player to edit the ranges of.
	 * @param ranges
	 *            An array of the ranges to add.
	 * 
	 * @throws IllegalArgumentException
	 *             If player is null.
	 * @throws IllegalArgumentException
	 *             If player does not have WDL installed.
	 * @throws IllegalArgumentException
	 *             If ranges is null, or any value in ranges is null.
	 */
	public abstract void addRanges(Player player, ProtectionRange... ranges);

	/**
	 * Add the given ranges to this range group. It is legal to call this method
	 * asynchronously.
	 * 
	 * @param player
	 *            The player to edit the ranges of.
	 * @param ranges
	 *            A list of the ranges to add.
	 * 
	 * @throws IllegalArgumentException
	 *             If player is null.
	 * @throws IllegalArgumentException
	 *             If player does not have WDL installed.
	 * @throws IllegalArgumentException
	 *             If ranges is null, or any value in ranges is null.
	 */
	public abstract void addRanges(Player player, List<ProtectionRange> ranges);

	/**
	 * Sets all of the ranges in this range group. It is legal to call this
	 * method asynchronously.
	 * 
	 * @param player
	 *            The player to edit the ranges of.
	 * @param ranges
	 *            An array of the new ranges.
	 * 
	 * @throws IllegalArgumentException
	 *             If player is null.
	 * @throws IllegalArgumentException
	 *             If player does not have WDL installed.
	 * @throws IllegalArgumentException
	 *             If ranges is null, or any value in ranges is null.
	 */
	public abstract void setRanges(Player player, ProtectionRange... ranges);

	/**
	 * Sets all of the ranges in this range group. It is legal to call this
	 * method asynchronously.
	 * 
	 * @param player
	 *            The player to edit the ranges of.
	 * @param ranges
	 *            A list of the new ranges.
	 * 
	 * @throws IllegalArgumentException
	 *             If player is null.
	 * @throws IllegalArgumentException
	 *             If player does not have WDL installed.
	 * @throws IllegalArgumentException
	 *             If ranges is null, or any value in ranges is null.
	 */
	public abstract void setRanges(Player player, List<ProtectionRange> ranges);

	/**
	 * Removes the range groups marked with the given tags from this range
	 * group. It is legal to call this method asynchronously.
	 * 
	 * @param player
	 *            The player to edit the ranges of.
	 * @param tags
	 *            An array of tags to remove.
	 * 
	 * @throws IllegalArgumentException
	 *             If player is null.
	 * @throws IllegalArgumentException
	 *             If player does not have WDL installed.
	 * @throws IllegalArgumentException
	 *             If tags is null, or any value in tags is null.
	 */
	public abstract void removeRangesByTags(Player player, String... tags);

	/**
	 * Removes the range groups marked with the given tags from this range
	 * group. It is legal to call this method asynchronously.
	 * 
	 * @param player
	 *            The player to edit the ranges of.
	 * @param tags
	 *            A list of tags to remove.
	 * 
	 * @throws IllegalArgumentException
	 *             If player is null.
	 * @throws IllegalArgumentException
	 *             If player does not have WDL installed.
	 * @throws IllegalArgumentException
	 *             If tags is null, or any value in tags is null.
	 */
	public abstract void removeRangesByTags(Player player, List<String> tags);

	/**
	 * Removes all of the ranges with the given tag from this range group, and
	 * then adds the given ProtectionRanges to this range group.It is legal to
	 * call this method asynchronously.
	 * 
	 * @param player
	 *            The player to edit the ranges of.
	 * @param tag
	 *            The tag to overwrite.
	 * @param ranges
	 *            An array of new ranges for that tag.
	 * 
	 * @throws IllegalArgumentException
	 *             If player is null.
	 * @throws IllegalArgumentException
	 *             If player does not have WDL installed.
	 * @throws IllegalArgumentException
	 *             If tag is null.
	 * @throws IllegalArgumentException
	 *             If ranges is null, or any value in ranges is null.
	 * @throws IllegalArgumentException
	 *             If any range in ranges does not have the same tag.
	 */
	public abstract void setTagRanges(Player player, String tag,
			ProtectionRange... ranges);

	/**
	 * Removes all of the ranges with the given tag from this range group, and
	 * then adds the given ProtectionRanges to this range group.It is legal to
	 * call this method asynchronously.
	 * 
	 * @param player
	 *            The player to edit the ranges of.
	 * @param tag
	 *            The tag to overwrite.
	 * @param ranges
	 *            A list of new ranges for that tag.
	 * 
	 * @throws IllegalArgumentException
	 *             If player is null.
	 * @throws IllegalArgumentException
	 *             If player does not have WDL installed.
	 * @throws IllegalArgumentException
	 *             If tag is null.
	 * @throws IllegalArgumentException
	 *             If ranges is null, or any value in ranges is null.
	 * @throws IllegalArgumentException
	 *             If any range in ranges does not have the same tag.
	 */
	public abstract void setTagRanges(Player player, String tag,
			List<ProtectionRange> ranges);

	/**
	 * Is the given player a player that runs WDL and thus should have
	 * permissions sent? Use this before running any large calculations (yes,
	 * the plugin message won't be sent if they aren't running, but if you
	 * calculate regions or something like that, it will be a waste). It is
	 * legal to call this method asynchronously.
	 * 
	 * @param player
	 *            The player to check.
	 * @return Whether the player should receive permissions.
	 */
	public abstract boolean isWDLPlayer(Player player);

}