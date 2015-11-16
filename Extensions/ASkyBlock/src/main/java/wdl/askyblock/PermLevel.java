package wdl.askyblock;

/**
 * Level of permission needed to download.
 */
public enum PermLevel {
	/**
	 * Must be the owner of the island.
	 */
	OWNER,
	/**
	 * Must be the owner of the island or in the island's team.
	 */
	TEAM_MEMBER,
	/**
	 * May be the owner, a team member, or in coop.
	 */
	COOP;
	
	/**
	 * Attempts to parse the given String into a PermLevel.
	 * 
	 * @param name The name of the perm level.
	 * @return the corresponding PermLevel, or null if it could not be found.
	 */
	public static PermLevel parse(String name) {
		for (PermLevel level : values()) {
			if (level.name().equalsIgnoreCase(name)) {
				return level;
			}
		}
		
		return null;
	}
}
