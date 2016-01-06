package wdl.worldgaurd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import wdl.range.IRangeGroup;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

/**
 * IRangeProducer that gets the ranges owned by a player.
 * 
 * TODO:
 * 1. Handle polygonal regions better than just taking the min and max points.
 * 2. Make sure that ProtectedRegion#isPhysicalArea is true.
 * 3. Make sure that all of the regions are loaded.
 * 4. Handle new regions dynamically. (there doesn't _seem_ to be events for it)
 * 5. Differentiate between member, owner, and memberOnly.
 * 6. Maybe there is a better way to get all of the regions owned by a player.
 */
public class WorldGuardRangeProducer implements IRangeProducer {
	private final IRangeGroup group;
	
	public WorldGuardRangeProducer(IRangeGroup group) {
		this.group = group;
	}
	
	@Override
	public List<ProtectionRange> getInitialRanges(Player player) {
		ArrayList<ProtectionRange> ranges = new ArrayList<>();
		LocalPlayer localPlayer = WGBukkit.getPlugin().wrapPlayer(player);
		
		RegionManager manager = WGBukkit.getRegionManager(player.getWorld());
		
		Collection<ProtectedRegion> regions = manager.getRegions().values();
		for (ProtectedRegion region : regions) {
			if (region.isMember(localPlayer)) { //Member or owner
				ranges.add(regionToRange(region));
			}
		}
		
		return ranges;
	}

	@Override
	public IRangeGroup getRangeGroup() {
		return group;
	}

	/**
	 * Converts a {@link ProtectedRegion} to a {@link ProtectionRange}.
	 * This is a somewhat lossy conversion: Only the region's minimum and maximum
	 * coordinates are used; if it is a polygonal region then this will not work.
	 * But it's probably good enough, at least for now.
	 */
	private ProtectionRange regionToRange(ProtectedRegion region) {
		BlockVector max = region.getMaximumPoint();
		BlockVector min = region.getMinimumPoint();
		
		int x1 = min.getBlockX() / 16;
		int z1 = min.getBlockZ() / 16;
		int x2 = max.getBlockX() / 16;
		int z2 = max.getBlockZ() / 16;
		
		return new ProtectionRange(region.getId(), x1, z1, x2, z2);
	}
	
	@Override
	public void dispose() { }
}
