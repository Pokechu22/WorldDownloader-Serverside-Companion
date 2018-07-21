package wdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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
	 * Instance of the plugin.
	 */
	private final WDLCompanion plugin;
	
	/**
	 * Has this RangeGroup been disposed?
	 */
	private boolean isDisposed;
	
	/**
	 * Creates a new RangeGroup. Intended for use by the WDLCompanion plugin
	 * only; this is a wrapper. Others should use {@link IRangeGroupType} /
	 * {@link IRangeProducer}, which will receive an instance of this class.
	 * 
	 * @param groupName
	 *            Name of the range group.
	 * @param plugin
	 *            The plugin instance.
	 */
	RangeGroup(String groupName, WDLCompanion plugin) {
		if (groupName == null) {
			throw new IllegalArgumentException("groupName must not be null!");
		}
		this.groupName = groupName;
		this.plugin = plugin;
	}
	
	@Override
	public String getGroupName() {
		if (this.isDisposed) {
			throw new IllegalStateException("This RangeGroup has been disposed!  You shouldn't be using it anymore (or even have an instance!)");
		}
		return groupName;
	}
	
	@Override
	public void addRanges(Player player, ProtectionRange... ranges) {
		if (this.isDisposed) {
			throw new IllegalStateException("This RangeGroup has been disposed!  You shouldn't be using it anymore (or even have an instance!)");
		}
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (!isWDLPlayer(player)) {
			throw new IllegalArgumentException("player " + playerToString(player) + " does not have WDL installed! (they aren't listening on the WDL channel)");
		}
		if (ranges == null) {
			throw new IllegalArgumentException("ranges must not be null!  (group is " + groupName + ")");
		}
		for (int i = 0; i < ranges.length; i++) {
			if (ranges[i] == null) {
				throw new IllegalArgumentException("No range in ranges may be null!  (#" + i + " was; group is " + groupName + ")");
			}
		}
		
		plugin.queuePacket(player, WDLPackets.createWDLPacket5(groupName,
				false, compactRanges(ranges)));
	}
	
	@Override
	public void addRanges(Player player, List<ProtectionRange> ranges) {
		if (this.isDisposed) {
			throw new IllegalStateException("This RangeGroup has been disposed!  You shouldn't be using it anymore (or even have an instance!)");
		}
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (!isWDLPlayer(player)) {
			throw new IllegalArgumentException("player " + playerToString(player) + " does not have WDL installed! (they aren't listening on the WDL channel)");
		}
		if (ranges == null) {
			throw new IllegalArgumentException("ranges must not be null!  (group is " + groupName + ")");
		}
		for (int i = 0; i < ranges.size(); i++) {
			if (ranges.get(i) == null) {
				throw new IllegalArgumentException("No range in ranges may be null!  (#" + i + " was; group is " + groupName + ")");
			}
		}
		
		plugin.queuePacket(player, WDLPackets.createWDLPacket5(groupName,
				false, compactRanges(ranges)));
	}

	@Override
	public void setRanges(Player player, ProtectionRange... ranges) {
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (!isWDLPlayer(player)) {
			throw new IllegalArgumentException("player " + playerToString(player) + " does not have WDL installed! (they aren't listening on the WDL channel)");
		}
		if (ranges == null) {
			throw new IllegalArgumentException("ranges must not be null!  (group is " + groupName + ")");
		}
		for (int i = 0; i < ranges.length; i++) {
			if (ranges[i] == null) {
				throw new IllegalArgumentException("No range in ranges may be null!  (#" + i + " was; group is " + groupName + ")");
			}
		}
		
		plugin.queuePacket(player, WDLPackets.createWDLPacket5(groupName, true,
				compactRanges(ranges)));
	}
	
	@Override
	public void setRanges(Player player, List<ProtectionRange> ranges) {
		if (this.isDisposed) {
			throw new IllegalStateException("This RangeGroup has been disposed!  You shouldn't be using it anymore (or even have an instance!)");
		}
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (!isWDLPlayer(player)) {
			throw new IllegalArgumentException("player " + playerToString(player) + " does not have WDL installed! (they aren't listening on the WDL channel)");
		}
		if (ranges == null) {
			throw new IllegalArgumentException("ranges must not be null!  (group is " + groupName + ")");
		}
		for (int i = 0; i < ranges.size(); i++) {
			if (ranges.get(i) == null) {
				throw new IllegalArgumentException("No range in ranges may be null!  (#" + i + " was; group is " + groupName + ")");
			}
		}
		
		plugin.queuePacket(player, WDLPackets.createWDLPacket5(groupName, true,
				compactRanges(ranges)));
	}

	@Override
	public void removeRangesByTags(Player player, String... tags) {
		if (this.isDisposed) {
			throw new IllegalStateException("This RangeGroup has been disposed!  You shouldn't be using it anymore (or even have an instance!)");
		}
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (!isWDLPlayer(player)) {
			throw new IllegalArgumentException("player " + playerToString(player) + " does not have WDL installed! (they aren't listening on the WDL channel)");
		}
		if (tags == null) {
			throw new IllegalArgumentException("tags must not be null!  (group is " + groupName + ")");
		}
		for (int i = 0; i < tags.length; i++) {
			if (tags[i] == null) {
				throw new IllegalArgumentException("No tag in tags may be null!  (#" + i + " was; group is " + groupName + ")");
			}
		}
		
		plugin.queuePacket(player,
				WDLPackets.createWDLPacket6(groupName, Arrays.asList(tags)));
	}
	
	@Override
	public void removeRangesByTags(Player player, List<String> tags) {
		if (this.isDisposed) {
			throw new IllegalStateException("This RangeGroup has been disposed!  You shouldn't be using it anymore (or even have an instance!)");
		}
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (!isWDLPlayer(player)) {
			throw new IllegalArgumentException("player " + playerToString(player) + " does not have WDL installed! (they aren't listening on the WDL channel)");
		}
		if (tags == null) {
			throw new IllegalArgumentException("tags must not be null!  (group is " + groupName + ")");
		}
		for (int i = 0; i < tags.size(); i++) {
			if (tags.get(i) == null) {
				throw new IllegalArgumentException("No tag in tags may be null!  (#" + i + " was; group is " + groupName + ")");
			}
		}
		
		plugin.queuePacket(player, WDLPackets.createWDLPacket6(groupName, tags));
	}

	@Override
	public void setTagRanges(Player player, String tag, ProtectionRange... ranges) {
		if (this.isDisposed) {
			throw new IllegalStateException("This RangeGroup has been disposed!  You shouldn't be using it anymore (or even have an instance!)");
		}
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (!isWDLPlayer(player)) {
			throw new IllegalArgumentException("player " + playerToString(player) + " does not have WDL installed! (they aren't listening on the WDL channel)");
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
		
		plugin.queuePacket(player, WDLPackets.createWDLPacket7(groupName, tag,
				compactRanges(ranges)));
	}
	
	@Override
	public void setTagRanges(Player player, String tag, List<ProtectionRange> ranges) {
		if (this.isDisposed) {
			throw new IllegalStateException("This RangeGroup has been disposed!  You shouldn't be using it anymore (or even have an instance!)");
		}
		if (player == null) {
			throw new IllegalArgumentException("player must not be null!");
		}
		if (!isWDLPlayer(player)) {
			throw new IllegalArgumentException("player " + playerToString(player) + " does not have WDL installed! (they aren't listening on the WDL channel)");
		}
		if (tag == null) {
			throw new IllegalArgumentException("tag must not be null!  (group is " + groupName + ")");
		}
		if (ranges == null) {
			throw new IllegalArgumentException("ranges must not be null!  (tag is " + tag + "; group is " + groupName + ")");
		}
		for (int i = 0; i < ranges.size(); i++) {
			if (ranges.get(i) == null) {
				throw new IllegalArgumentException("No range in ranges may be null!  (#" + i + " was; tag is " + tag + "; group is " + groupName + ")");
			}
			if (!tag.equals(ranges.get(i).tag)) {
				throw new IllegalArgumentException("No range in ranges may have a tag different from the tag!  (#" + i + " had tag " + ranges.get(i).tag + "; expected tag is " + tag + "; group is " + groupName + ")");
			}
		}
		
		plugin.queuePacket(player, WDLPackets.createWDLPacket7(groupName, tag,
				compactRanges(ranges)));
	}

	@Override
	public boolean isWDLPlayer(Player player) {
		if (this.isDisposed) {
			throw new IllegalStateException("This RangeGroup has been disposed!  You shouldn't be using it anymore (or even have an instance!)");
		}
		return player.getListeningPluginChannels().contains(WDLCompanion.CONTROL_CHANNEL_NAME_113) ||
				player.getListeningPluginChannels().contains(WDLCompanion.CONTROL_CHANNEL_NAME_112);
	}
	
	/**
	 * Convert the given list of ranges into an equivalent list with ranges
	 * partially merged together.
	 */
	private List<ProtectionRange> compactRanges(ProtectionRange... ranges) {
		return compactRanges(Arrays.asList(ranges));
	}
	
	/**
	 * Convert the given list of ranges into an equivalent list with ranges
	 * partially merged together.
	 */
	private List<ProtectionRange> compactRanges(List<ProtectionRange> ranges) {
		// Based off of http://stackoverflow.com/a/8072813/3991344
		// Try to form horizontal or vertical rectangles.  Not perfect, but
		// hopefully it's good enough in practice.
		
		/**
		 * A single point, for use in calculations.
		 */
		class Point {
			public Point(int x, int z) {
				this.x = x;
				this.z = z;
			}
			public Point(ProtectionRange range) {
				assert range.x1 == range.x2;
				assert range.z1 == range.z2;
				
				this.x = range.x1;
				this.z = range.z1;
			}
			
			public final int x;
			public final int z;
			
			public Point oneLeft() {
				return new Point(this.x + 1, this.z);
			}
			public Point oneDown() {
				return new Point(this.x, this.z + 1);
			}
			
			@Override
			public int hashCode() {
				final int prime = 1117; //Larger prime because x and z may be small.
				int result = 1;
				result = prime * result + x;
				result = prime * result + z;
				return result;
			}
			
			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (!(obj instanceof Point)) {
					return false;
				}
				Point other = (Point) obj;
				if (x != other.x) {
					return false;
				}
				if (z != other.z) {
					return false;
				}
				return true;
			}
			
			@Override
			public String toString() {
				return "Point [x=" + x + ", z=" + z + "]";
			}
		}
		
		Multimap<String, ProtectionRange> rangesByTag = HashMultimap.create();
		
		for (ProtectionRange range : ranges) {
			rangesByTag.put(range.tag, range);
		}
		
		List<ProtectionRange> finalRanges = new ArrayList<>();
		
		for (Map.Entry<String, Collection<ProtectionRange>> e : rangesByTag.asMap().entrySet()) {
			String tag = e.getKey();
			
			List<ProtectionRange> newRanges = new ArrayList<>();
			
			int lowestX = Integer.MAX_VALUE;
			int lowestZ = Integer.MAX_VALUE;
			int highestX = Integer.MIN_VALUE;
			int highestZ = Integer.MIN_VALUE;
			
			Map<Point, ProtectionRange> rangesByLocation = new HashMap<>();
			
			for (ProtectionRange range : e.getValue()) {
				if (range.x1 == range.x2 && range.z1 == range.z2) {
					if (range.x1 < lowestX) {
						lowestX = range.x1;
					}
					if (range.z1 < lowestZ) {
						lowestZ = range.z1;
					}
					if (range.x1 > highestX) {
						highestX = range.x1;
					}
					if (range.z1 > highestZ) {
						highestZ = range.z1;
					}
					// Will also overwrite other ranges if there's two at the
					// same place, which is beneficial.
					rangesByLocation.put(new Point(range), range);
				} else {
					newRanges.add(range);
				}
			}
			
			int rectStartX = 0, rectStartZ = 0;
			int rectEndX = 0, rectEndZ = 0;
			boolean inRectangle = false;
			
			// Horizontal groups first.
			for (int z = lowestZ; z <= highestZ; z++) {
				for (int x = lowestX; x <= highestX; x++) {
					Point point = new Point(x, z);
					
					if (rangesByLocation.containsKey(point)) {
						if (inRectangle) {
							//Expand the rectangle.
							rectEndX = x;
							
							//We've used up this point.
							rangesByLocation.remove(point);
						} else {
							// Start a new rectangle if there
							// is at least one more range.
							if (rangesByLocation.containsKey(point.oneLeft())) {
								inRectangle = true;
								rectStartX = x;
								rectStartZ = z;
								rectEndX = x;
								rectEndZ = z;
								
								//We've used up this point.
								rangesByLocation.remove(point);
							}
						}
					} else {
						if (inRectangle) {
							//Current rectangle finished - edge reached.
							ProtectionRange range = new ProtectionRange(tag, rectStartX,
									rectStartZ, rectEndX, rectEndZ);
							newRanges.add(range);
							inRectangle = false;
						}
					}
				}
				
				if (inRectangle) {
					//Current rectangle finished - line finished.
					ProtectionRange range = new ProtectionRange(tag, rectStartX,
							rectStartZ, rectEndX, rectEndZ);
					newRanges.add(range);
					inRectangle = false;
				}
			}
			
			// Now do vertical groups.
			for (int x = lowestX; x <= highestX; x++) {
				for (int z = lowestZ; z <= highestZ; z++) {
					Point point = new Point(x, z);
					
					if (rangesByLocation.containsKey(point)) {
						if (inRectangle) {
							//Expand the rectangle.
							rectEndZ = z;
							
							//We've used up this point.
							rangesByLocation.remove(point);
						} else {
							// Start a new rectangle if there
							// is at least one more range.
							if (rangesByLocation.containsKey(point.oneDown())) {
								inRectangle = true;
								rectStartX = x;
								rectStartZ = z;
								rectEndX = x;
								rectEndZ = z;
								
								//We've used up this point.
								rangesByLocation.remove(point);
							}
						}
					} else {
						if (inRectangle) {
							//Current rectangle finished - edge reached.
							ProtectionRange range = new ProtectionRange(tag, rectStartX,
									rectStartZ, rectEndX, rectEndZ);
							newRanges.add(range);
							inRectangle = false;
						}
					}
				}
				
				if (inRectangle) {
					//Current rectangle finished - line finished.
					ProtectionRange range = new ProtectionRange(tag, rectStartX,
							rectStartZ, rectEndX, rectEndZ);
					newRanges.add(range);
					inRectangle = false;
				}
			}
			
			//Add the remaining single ranges.
			newRanges.addAll(rangesByLocation.values());
			
			finalRanges.addAll(newRanges);
		}
		
		return finalRanges;
	}
	
	/**
	 * Gets a string version of a player for use in exceptions.  This includes
	 * their UUID, name, and display name.
	 */
	private String playerToString(Player player) {
		return player.getDisplayName() + " (" + player.getName() + " / "
				+ player.getUniqueId() + ")";
	}
	
	@Override
	public void dispose() {
		this.isDisposed = true;
	}
}
