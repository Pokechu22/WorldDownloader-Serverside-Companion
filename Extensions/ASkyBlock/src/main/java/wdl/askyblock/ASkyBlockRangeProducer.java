package wdl.askyblock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Settings;
import com.wasteofplastic.askyblock.events.IslandJoinEvent;
import com.wasteofplastic.askyblock.events.IslandLeaveEvent;
import com.wasteofplastic.askyblock.events.IslandNewEvent;

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
			List<Location> locations = getIslandsFor(player.getUniqueId());
			int protectionRange = Settings.island_protectionRange;
			
			for (Location location : locations) {
				ranges.add(getProtectionRangeForIsland(location, protectionRange));
			}
		}
		
		return ranges;
	}

	@Override
	public IRangeGroup getRangeGroup() {
		return group;
	}
	
	/**
	 * Occurs when ASkyBlock fires an {@link IslandLeaveEvent}: a player has
	 * left an island team.
	 */
	@EventHandler
	public void onLeaveIsland(IslandLeaveEvent e) {
		Player player = Bukkit.getPlayer(e.getPlayer());
		group.removeRangesByTags(player, getIslandTag(e.getTeamLeader()));
	}
	
	/**
	 * Occurs when ASkyBlock fires an {@link IslandJoinEvent}: a player has
	 * joined an island team.
	 */
	@EventHandler
	public void onJoinIsland(IslandJoinEvent e) {
		Player player = Bukkit.getPlayer(e.getPlayer());
		
		String tag = getIslandTag(e.getTeamLeader());
		ProtectionRange range = getProtectionRangeForIsland(e.getIslandLocation(), e.getProtectionSize());
		
		group.setTagRanges(player, tag, range);
	}
	
	/**
	 * Occurs when ASkyBlock fires an {@link IslandNewEvent}: a player has
	 * created a new island.
	 */
	@EventHandler
	public void onNewIsland(IslandNewEvent e) {
		Player player = e.getPlayer();
		
		ProtectionRange range = getProtectionRangeForIsland(
				e.getIslandLocation(), e.getProtectionSize());
		
		group.setTagRanges(player, player.getName() + "'s island", range);
	}
	
	/**
	 * Gets the locations for all islands useable by the given player.
	 * Owned is defined as accessible under {@link #requiredPerm}.
	 * 
	 * @param playerID The unique ID of the player.
	 * @return A list of all islands the player can use. 
	 */
	private List<Location> getIslandsFor(UUID playerID) {
		List<Location> returned = new ArrayList<>();
		
		Location islandLoc = api.getIslandLocation(playerID);
		if (islandLoc != null) {
			if (requiredPerm == PermLevel.OWNER) {
				if (api.hasIsland(playerID)) {
					// Island must be owned by that player.
					returned.add(islandLoc);
				}
			} else {
				returned.add(islandLoc);
			}
		}
		
		if (requiredPerm == PermLevel.COOP) {
			returned.addAll(api.getCoopIslands(Bukkit.getPlayer(playerID)));
		}
		
		return returned;
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
