package wdl;

import org.bukkit.entity.Player;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

/**
 * Group of {@link ProtectionRange}s, which can be edited and will send
 * packets as needed.
 */
class RangeGroup implements IRangeGroup {
	/**
	 * Name of this range group.
	 */
	private final String groupName;
	
	/**
	 * Creates a new RangeGroup. Intended for use by the WDLCompanion plugin
	 * only; this is a wrapper. Others should use {@link IRangeGroupType} /
	 * {@link IRangeProducer}, which will receive an instance of this class.
	 * 
	 * @param groupName
	 *            Name of the range group.
	 */
	RangeGroup(String groupName) {
		if (groupName == null) {
			throw new IllegalArgumentException("groupName must not be null!");
		}
		this.groupName = groupName;
	}
	
	@Override
	public String getGroupName() {
		return groupName;
	}
	
	@Override
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
	
	@Override
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
	
	@Override
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
	
	@Override
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
	
	@Override
	public boolean isWDLPlayer(Player player) {
		return player.getListeningPluginChannels().contains(
				WDLCompanion.CONTROL_CHANNEL_NAME);
	}
}
