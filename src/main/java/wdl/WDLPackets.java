package wdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import wdl.range.ProtectionRange;
import wdl.request.PermissionRequest;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * Reads and writes WDL plugin channel packets.  The structures of these
 * packets are documented
 * <a href="http://wiki.vg/User:Pokechu22/World_downloader">on wiki.vg</a>.
 */
public class WDLPackets {

	/**
	 * Creates a byte array for the WDL control packet #0.
	 * 
	 * @param canDoNewThings
	 *            Whether players can use new functions that aren't known to
	 *            this plugin.
	 * @return
	 */
	public static byte[] createWDLPacket0(boolean canDoNewThings) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
	
		output.writeInt(0);
		
		output.writeBoolean(canDoNewThings);
		
		return output.toByteArray();
	}

	/**
	 * Creates a byte array for the WDL control packet #1.
	 * 
	 * @param globalIsEnabled
	 *            Whether or not all of WDL is enabled.
	 * @param saveRadius
	 *            The distance of chunks that WDL can download from the player's
	 *            position.
	 * @param cacheChunks
	 *            Whether or not chunks that the player previously entered but
	 *            since exited are to be saved. If <code>false</code>, only the
	 *            currently loaded chunks will be saved; if <code>true</code>,
	 *            the player will download terrain as they move.
	 * @param saveEntities
	 *            Whether or not entities and their appearance are to be saved.
	 *            This includes Minecart Chest contents.
	 * @param saveTileEntities
	 *            Whether or not tile entities (General ones that are reloaded
	 *            such as signs and banners, as well as chests) are to be saved.
	 *            If <code>false</code>, no tile entities will be saved. If
	 *            <code>true</code>, they will be saved.)
	 * @param saveContainers
	 *            Whether or not container tile entities are to be saved. If
	 *            <code>saveTileEntities</code> is <code>false</code>, this
	 *            value is ignored and treated as false. If this value is
	 *            <code>false</code>, then container tile entities (ones that
	 *            players need to open to save) will not be opened. If this
	 *            value is <code>true</code>, then said tile entities can be
	 *            saved by players as they are opened.
	 * @return The byte array used for creating that plugin channel message.
	 */
	public static byte[] createWDLPacket1(boolean globalIsEnabled, int saveRadius,
			boolean cacheChunks, boolean saveEntities,
			boolean saveTileEntities, boolean saveContainers) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
	
		output.writeInt(1);
	
		output.writeBoolean(globalIsEnabled);
		output.writeInt(saveRadius);
		output.writeBoolean(cacheChunks && globalIsEnabled);
		output.writeBoolean(saveEntities && globalIsEnabled);
		output.writeBoolean(saveTileEntities && globalIsEnabled);
		output.writeBoolean(saveContainers && saveTileEntities
				&& globalIsEnabled);
	
		return output.toByteArray();
	}

	/**
	 * Creates the WDL packet #2.  This packet contains the server's
	 * tracking ranges for entities.  (If the server doesn't run spigot, or
	 * the player receives a 0-length list).  WDL uses this data to know when
	 * an entity leaves the range and thus should be saved.
	 * <br/>
	 * Its structure is simply an int, giving the number of entries,
	 * and then a series of strings and ints containing the tracking
	 * ranges.  The string value is the entity's savegame name, and the
	 * int is the tracking range.
	 * 
	 * @return
	 */
	public static byte[] createWDLPacket2(Map<String, Integer> ranges) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		
		output.writeInt(2);
		
		output.writeInt(ranges.size());
		
		for (Map.Entry<String, Integer> e : ranges.entrySet()) {
			output.writeUTF(e.getKey());
			output.writeInt(e.getValue());
		}
		
		return output.toByteArray();
	}

	/**
	 * Creates the WDL packet #3.
	 * 
	 * This packet gives information to display when requesting permissions.
	 * 
	 * The structure is a boolean (which controls whether requests are enabled,
	 * but <b>will always be true</b>), followed by a UTF-string to display
	 * to the player.
	 * 
	 * @return
	 */
	public static byte[] createWDLPacket3(String message) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		
		output.writeInt(3);
		
		output.writeBoolean(true);
		output.writeUTF(message);
		
		return output.toByteArray();
	}
	
	/**
	 * Creates the WDL packet #4.
	 * 
	 * This packet specifies the initial overrides for what chunks can and
	 * cannot be saved.
	 *
	 * This packet starts with an int stating the number of keys, and then a
	 * series of values for 1 range group. The range group starts with its name
	 * (the key in ranges), then an int (the number of ranges) and then each of
	 * the ranges as generated by
	 * {@link #writeProtectionRange(ProtectionRange, ByteArrayDataOutput)}.
	 */
	public static byte[] createWDLPacket4(
			Map<String, List<ProtectionRange>> ranges) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		
		output.writeInt(4);
		
		output.writeInt(ranges.size());
		
		for (String key : ranges.keySet()) {
			output.writeUTF(key);
			List<ProtectionRange> rangeGroup = ranges.get(key);
			output.writeInt(rangeGroup.size());
			for (ProtectionRange range : rangeGroup) {
				writeProtectionRange(range, output);
			}
		}
		
		return output.toByteArray();
	}
	
	/**
	 * Creates the WDL packet #5.
	 * 
	 * This packet specifies adds additional overrides to or sets all of the
	 * overrides in a single group.
	 *
	 * This packet starts with a String stating the group, then a boolean that
	 * specifies whether it is setting (true) or adding (false) the ranges, and
	 * then an int (the number of ranges that will be added). Then, each range,
	 * formated by
	 * {@link #writeProtectionRange(ProtectionRange, ByteArrayDataOutput)}.
	 */
	public static byte[] createWDLPacket5(String group,
			boolean replace, List<ProtectionRange> ranges) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		
		output.writeInt(5);
		
		output.writeUTF(group);
		output.writeBoolean(replace);
		output.writeInt(ranges.size());
		
		for (ProtectionRange range : ranges) {
			writeProtectionRange(range, output);
		}
		
		return output.toByteArray();
	}
	
	/**
	 * Creates the WDL packet #6.
	 * 
	 * This packet removes a series of ranges in the given group based off of
	 * the tags.
	 * 
	 * This packet is simply a string (the group), followed by an int (number of
	 * tags), followed by each of the tags.
	 */
	public static byte[] createWDLPacket6(String group, List<String> tags) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		
		output.writeInt(6);
		
		output.writeUTF(group);
		output.writeInt(tags.size());
		
		for (String tag : tags) {
			output.writeUTF(tag);
		}
		
		return output.toByteArray();
	}
	
	/**
	 * Creates the WDL packet #7.
	 * 
	 * This packet replaces all of the ranges with the given tag with a new set
	 * of ranges.
	 *
	 * This packet starts with a String stating the group, then a second string
	 * that specifies the tag to replace. After that, there is an int stating
	 * the number of ranges, and then each range as formated by
	 * {@link #writeProtectionRange(ProtectionRange, ByteArrayDataOutput)}.
	 */
	public static byte[] createWDLPacket7(String group,
			String tag, List<ProtectionRange> newRanges) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		
		output.writeInt(7);
		
		output.writeUTF(group);
		output.writeUTF(tag);
		
		output.writeInt(newRanges.size());
		
		for (ProtectionRange range : newRanges) {
			writeProtectionRange(range, output);
		}
		
		return output.toByteArray();
	}
	
	/**
	 * Reads a permission request.
	 */
	public static PermissionRequest readPermissionRequest(
			Player player, byte[] data) {
		ByteArrayDataInput input = ByteStreams.newDataInput(data);
		
		String requestReason = input.readUTF();
		
		Map<String, String> requestedPerms = new HashMap<>();
		int numRequests = input.readInt();
		for (int i = 0; i < numRequests; i++) {
			String key = input.readUTF();
			String value = input.readUTF();
			
			requestedPerms.put(key, value);
		}
		
		List<ProtectionRange> rangeRequests = new ArrayList<>();
		int numRangeRequests = input.readInt();
		for (int i = 0; i < numRangeRequests; i++) {
			rangeRequests.add(readProtectionRange(input));
		}
		
		return new PermissionRequest(player, requestReason, requestedPerms,
				rangeRequests);
	}
	
	/**
	 * Writes a protection range to the given output stream.
	 * 
	 * This is a string with the range's tag, then 4 integers for the
	 * coordinates (x1, z1, x2, z2). This method also swaps x1 and x2 if x1 is
	 * greater than x2.
	 */
	private static void writeProtectionRange(ProtectionRange range, 
			ByteArrayDataOutput output) {
		output.writeUTF(range.tag);
		
		int x1 = range.x1, z1 = range.z1;
		int x2 = range.x2, z2 = range.z2;
		
		if (x1 > x2) {
			x2 = range.x1;
			x1 = range.x2;
		}
		if (z1 > z2) {
			z2 = range.z1;
			z1 = range.z2;
		}
		
		output.writeInt(x1);
		output.writeInt(z1);
		output.writeInt(x2);
		output.writeInt(z2);
	}
	
	/**
	 * Reads a protection range from the given input stream.
	 * 
	 * This is a string with the range's tag, then 4 integers for the
	 * coordinates (x1, z1, x2, z2). This method also swaps x1 and x2 if x1 is
	 * greater than x2.
	 */
	private static ProtectionRange readProtectionRange(ByteArrayDataInput input) {
		String tag = input.readUTF();
		
		int x1 = input.readInt();
		int z1 = input.readInt();
		int x2 = input.readInt();
		int z2 = input.readInt();
		
		if (x1 > x2) {
			int temp = x2;
			x2 = x1;
			x1 = temp;
		}
		if (z1 > z2) {
			int temp = z2;
			z2 = z1;
			z1 = temp;
		}
		
		return new ProtectionRange(tag, x1, z1, x2, z2);
	}
}

