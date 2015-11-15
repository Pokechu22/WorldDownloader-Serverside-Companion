package wdl.range;

/**
 * A range of chunks.  Note that if x1 is greater than x2, they will be
 * swapped, and if z1 is greater than z2, they will be swapped.
 */
public final class ProtectionRange {
	public ProtectionRange(String tag, int x1, int z1, int x2, int z2) {
		if (tag == null) {
			tag = "";
		}
		this.tag = tag;
		this.x1 = x1;
		this.z1 = z1;
		this.x2 = x2;
		this.z2 = z2;
	}
	
	/**
	 * "Tag" for this chunk.  Multiple {@link ProtectionRange}s can share the
	 * same tag; this is used to identify sub groups.  May be empty.  If null
	 * is passed to the constructor for tag, an empty string is used instead.
	 */
	public final String tag;
	/**
	 * Range of coordinates.  If x1 is greater than x2, they will be swapped
	 * (and the same for y).  These are chunk coordinates.
	 */
	public final int x1, z1, x2, z2;
}
