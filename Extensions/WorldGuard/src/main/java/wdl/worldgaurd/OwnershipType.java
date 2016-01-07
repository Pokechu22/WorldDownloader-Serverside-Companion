package wdl.worldgaurd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public enum OwnershipType {
	OWNER_OR_MEMBER {
		@Override
		public boolean has(LocalPlayer player, ProtectedRegion region) {
			return region.isMember(player);
		}
	},
	OWNER_ONLY {
		@Override
		public boolean has(LocalPlayer player, ProtectedRegion region) {
			return region.isOwner(player);
		}
	},
	MEMBER_ONLY {
		@Override
		public boolean has(LocalPlayer player, ProtectedRegion region) {
			return region.isMemberOnly(player);
		}
	};
	
	/**
	 * All aliases used by this ownership type.
	 */
	public final List<String> aliases;
	/**
	 * All valid names for ownership types.
	 */
	public static final List<String> NAMES;
	/**
	 * A map of aliases to instances.  Note: Keys should be toUpperCased()
	 */
	private static final Map<String, OwnershipType> BY_ALIAS;
	
	/**
	 * Constructor.
	 * 
	 * @param aliases Possible names that can be found in the configuration.
	 */
	private OwnershipType(String... aliases) {
		this.aliases = ImmutableList.copyOf(aliases);
	}
	
	/**
	 * Does the given player have the required type of ownership in the given
	 * region?
	 * 
	 * @param player
	 *            The (wrapped) player to test.
	 * @param region
	 *            The region to test in.
	 * @return Whether the player has the required ownership type.
	 */
	public abstract boolean has(LocalPlayer player, ProtectedRegion region);
	
	/**
	 * Gets the OwnershipType with the given name or alias.
	 * 
	 * @param name the alias
	 * @return the type, or null if it can't be found.
	 */
	public static OwnershipType match(String name) {
		return BY_ALIAS.get(name);
	}
	
	static {
		List<String> names = new ArrayList<String>();
		Map<String, OwnershipType> byAlias = new HashMap<>();
		for (OwnershipType type : values()) {
			for (String alias : type.aliases) {
				names.add(alias.toUpperCase());
				byAlias.put(alias.toUpperCase(), type);
			}
		}
		
		NAMES = ImmutableList.copyOf(names);
		BY_ALIAS = ImmutableMap.copyOf(byAlias);
	}
}
