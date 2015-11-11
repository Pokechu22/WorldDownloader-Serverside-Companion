package wdl;

import java.util.List;
import java.util.Map;

import wdl.range.ProtectionRange;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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
	 * This packet specifies overrides for what chunks can and cannot be saved.
	 *
	 * This packet starts with a single boolean that states whether the old
	 * list of locations should be cleared or appended.  If true, append to
	 * the list.
	 * 
	 * It is followed by an int, stating the number of areas, and then a series
	 * of 1 boolean + 4 ints, the boolean being whether it is whitelisting 
	 * (true) or blacklisting (false) and the ints specifying the coordinates. 
	 * 
	 * @return
	 */
	public static byte[] createWDLPacket4(List<ProtectionRange> ranges) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		
		output.writeInt(4);
		
		output.writeBoolean(true); //Override old data.  For now, always true.
		
		output.writeInt(ranges.size());
		
		for (ProtectionRange range : ranges) {
			output.writeBoolean(range.isWhitelist);
			int x1 = range.x1, y1 = range.y1;
			int x2 = range.x2, y2 = range.y2;
			
			if (x1 > x2) {
				x2 = range.x1;
				x1 = range.x2;
			}
			if (y1 > y2) {
				y2 = range.y1;
				y1 = range.y2;
			}
			
			output.writeInt(x1);
			output.writeInt(y1);
			output.writeInt(x2);
			output.writeInt(y2);
		}
		
		return output.toByteArray();
	}
}
