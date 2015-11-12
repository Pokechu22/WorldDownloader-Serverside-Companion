package wdl.range;

import org.bukkit.entity.Player;

import wdl.WDLCompanion;

/**
 * Group of {@link ProtectionRange}s, which can be edited and will send
 * packets as needed.
 */
public class RangeGroup {
	/**
	 * Name of this range group.
	 */
	private final String groupName;
	
	RangeGroup(String groupName) {
		if (groupName == null) {
			throw new IllegalArgumentException("groupName must not be null!");
		}
		this.groupName = groupName;
	}
	
	/**
	 * Gets the name of this group.
	 *
	 * @return {@link #groupName}
	 */
	public String getGroupName() {
		return groupName;
	}
	
	/**
	 * Add the given ranges to this range group.
	 * 
	 * @param ranges An array of the ranges to add.
	 * 
	 * @throws IllegalArgumentException If player is null.
	 * @throws IllegalArgumentException If ranges is null, or any value in ranges is null.
	 */
	public void addRanges(Player player, ProtectionRange... ranges) {
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (ranges == null) {
			throw new IllegalArgumentException("ranges must not be null!  (group is " + groupName + ")");
		}
		for (int i = 0; i < ranges.length; i++) {
			if (ranges[i] == null) {
				throw new IllegalArgumentException("No range in ranges may be null!  (#" + i + " was; group is " + groupName + ")");
			}
		}
		
		//TODO
	}
	
	/**
	 * Sets all of the ranges in this range group.
	 * 
	 * @param ranges An array of the new ranges.
	 * 
	 * @throws IllegalArgumentException If player is null.
	 * @throws IllegalArgumentException If ranges is null, or any value in ranges is null.
	 */
	public void setRanges(Player player, ProtectionRange... ranges) {
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (ranges == null) {
			throw new IllegalArgumentException("ranges must not be null!  (group is " + groupName + ")");
		}
		for (int i = 0; i < ranges.length; i++) {
			if (ranges[i] == null) {
				throw new IllegalArgumentException("No range in ranges may be null!  (#" + i + " was; group is " + groupName + ")");
			}
		}
		
		//TODO
	}
	
	/**
	 * Removes the range groups marked with the given tags from this range group.
	 * 
	 * @param tags An array of tags to remove.
	 * 
	 * @throws IllegalArgumentException If player is null.
	 * @throws IllegalArgumentException If tags is null, or any value in tags is null.
	 */
	public void removeRangesByTags(Player player, String... tags) {
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (tags == null) {
			throw new IllegalArgumentException("tags must not be null!  (group is " + groupName + ")");
		}
		for (int i = 0; i < tags.length; i++) {
			if (tags[i] == null) {
				throw new IllegalArgumentException("No tag in tags may be null!  (#" + i + " was; group is " + groupName + ")");
			}
		}
		
		//TODO
	}
	
	/**
	 * Removes all of the ranges with the given tag from this range group, and
	 * then adds the given ProtectionRanges to this range group.
	 * 
	 * @param tag The tag to overwrite.
	 * @param ranges The new ranges for that tag.
	 * 
	 * @throws IllegalArgumentException If player is null.
	 * @throws IllegalArgumentException If tag is null.
	 * @throws IllegalArgumentException If ranges is null, or any value in ranges is null.
	 * @throws IllegalArgumentException If any range in ranges does not have the same tag.
	 */
	public void setTagRanges(Player player, String tag, ProtectionRange... ranges) {
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (tag == null) {
			throw new IllegalArgumentException("tag must not be null!  (group is " + groupName + ")");
		}
		if (ranges == null) {
			throw new IllegalArgumentException("ranges must not be null!  (tag is " + tag + "; group is " + groupName + ")");
		}
		for (int i = 0; i < ranges.length; i++) {
			if (ranges[i] == null) {
				throw new IllegalArgumentException("No range in ranges may be null!  (#" + i + " was; tag is " + tag + "; group is " + groupName + ")");
			}
			if (!tag.equals(ranges[i].tag)) {
				throw new IllegalArgumentException("No range in ranges may have a tag different from the tag!  (#" + i + " had tag " + ranges[i].tag + "; expected tag is " + tag + "; group is " + groupName + ")");
			}
		}
		
		//TODO
	}
	
	/**
	 * Is the given player a player that runs WDL and thus should have
	 * permissions sent? Use this before running any large calculations (yes,
	 * the plugin message won't be sent if they aren't running, but if you
	 * calculate regions or something like that, it will be a waste).
	 * 
	 * @param player
	 *            The player to check.
	 * @return Whether the player should receive permissions.
	 */
	public boolean isWDLPlayer(Player player) {
		return player.getListeningPluginChannels().contains(
				WDLCompanion.CONTROL_CHANNEL_NAME);
	}
}
