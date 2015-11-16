package wdl.askyblock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Settings;

import wdl.range.IRangeGroup;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

/**
 * {@link IRangeProducer} that grants players permission to download on
 * their ASkyBlock islands.
 */
public class ASkyBlockRangeProducer implements IRangeProducer, Listener {
	private final IRangeGroup group;
	private final PermLevel requiredPerm;
	private final ASkyBlockAPI api;
	
	public ASkyBlockRangeProducer(IRangeGroup group, PermLevel requiredPerm) {
		this.group = group;
		this.requiredPerm = requiredPerm;
		this.api = ASkyBlockAPI.getInstance();
	}
	
	@Override
	public List<ProtectionRange> getInitialRanges(Player player) {
		ArrayList<ProtectionRange> ranges = new ArrayList<>();
		
		if (player.getWorld().equals(api.getIslandWorld())) {
			UUID playerID = player.getUniqueId();
			if (api.hasIsland(playerID) || api.inTeam(playerID)) {
				ranges.add(getProtectionRangeForPlayerIsland(playerID));
			}
		}
		
		return ranges;
	}

	@Override
	public IRangeGroup getRangeGroup() {
		return group;
	}
	
	/**
	 * Gets a {@link ProtectionRange} for the island owned by the player with the
	 * given {@link UUID unique ID}.
	 */
	private ProtectionRange getProtectionRangeForPlayerIsland(UUID playerID) {
		Location islandLoc = api.getIslandLocation(playerID);
		int protectionRange = Settings.island_protectionRange;
		
		return getProtectionRangeForIsland(islandLoc, protectionRange);
	}
	
	/**
	 * Gets a {@link ProtectionRange} for the island at the given position.
	 * 
	 * @param center The center location of the island.
	 * @param protectionRange The distance an island is protected for.
	 */
	private ProtectionRange getProtectionRangeForIsland(Location center,
			int protectionRange) {
		int x1 = center.getBlockX() - protectionRange / 2;
		int z1 = center.getBlockZ() - protectionRange / 2;
		int x2 = center.getBlockX() + protectionRange / 2;
		int z2 = center.getBlockZ() + protectionRange / 2;
		String tag = getIslandTag(ASkyBlockAPI.getInstance().getOwner(center));
		return new ProtectionRange(tag, x1, z1, x2, z2);
	}
	
	/**
	 * Gets the name of the player with the given unique ID.
	 */
	private String getPlayerName(UUID uniqueID) {
		return Bukkit.getOfflinePlayer(uniqueID).getName();
	}
	
	/**
	 * Gets a tag that can be used for a player's island.
	 */
	private String getIslandTag(UUID ownerID) {
		return getPlayerName(ownerID) + "'s island";
	}
}
