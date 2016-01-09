package wdl.worldgaurd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import wdl.range.IRangeGroup;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

/**
 * IRangeProducer that gets the ranges owned by a player.
 * 
 * TODO:
 * 1. Make sure that all of the regions are loaded.
 * 2. Handle new regions dynamically. (there doesn't _seem_ to be events for it)
 * 3. Maybe there is a better way to get all of the regions owned by a player.
 */
public class WorldGuardRangeProducer implements IRangeProducer {
	private final IRangeGroup group;
	private final OwnershipType ownershipType;
	private final boolean preservePolygons;
	
	public WorldGuardRangeProducer(IRangeGroup group, OwnershipType ownershipType, 
			boolean preservePolygons) {
		this.group = group;
		this.ownershipType = ownershipType;
		this.preservePolygons = preservePolygons;
	}
	
	@Override
	public List<ProtectionRange> getInitialRanges(Player player) {
		ArrayList<ProtectionRange> ranges = new ArrayList<>();
		LocalPlayer localPlayer = WGBukkit.getPlugin().wrapPlayer(player);
		
		RegionManager manager = WGBukkit.getRegionManager(player.getWorld());
		
		Collection<ProtectedRegion> regions = manager.getRegions().values();
		for (ProtectedRegion region : regions) {
			if (ownershipType.has(localPlayer, region)) {
				ranges.addAll(regionToRange(region));
			}
		}
		
		return ranges;
	}

	@Override
	public IRangeGroup getRangeGroup() {
		return group;
	}

	/**
	 * Converts a {@link ProtectedRegion} to a collection of
	 * {@link ProtectionRange}s.
	 */
	private List<ProtectionRange> regionToRange(ProtectedRegion region) {
		BlockVector max = region.getMaximumPoint();
		BlockVector min = region.getMinimumPoint();
		
		int x1 = min.getBlockX() / 16;
		int z1 = min.getBlockZ() / 16;
		int x2 = max.getBlockX() / 16;
		int z2 = max.getBlockZ() / 16;
		
		List<ProtectionRange> ranges = new ArrayList<>();
		
		if (!region.isPhysicalArea()) {
			// Return an empty list.
			return ranges;
		}
		
		if (preservePolygons && region instanceof ProtectedPolygonalRegion) {
			//TODO: there's probably something I can do to improve this logic.
			
			for (int x = x1; x <= x2; x++) {
				for (int z = z1; z <= z2; z++) {
					// Only checking the corners - probably a bad idea, but this
					// should cover _most_ parts.
					
					if (region.contains(new BlockVector2D(x * 16, z * 16))
							|| region.contains(new BlockVector2D(x * 16 + 15,
									z * 16))
							|| region.contains(new BlockVector2D(x * 16,
									z * 16 + 15))
							|| region.contains(new BlockVector2D(x * 16 + 15,
									z * 16 + 15))) {
						ranges.add(new ProtectionRange(region.getId(), x, z, x,
								z));
					}
				}
			}
		} else {
			ranges.add(new ProtectionRange(region.getId(), x1, z1, x2, z2));
		}
		
		return ranges;
	}
	
	@Override
	public void dispose() { }
}
