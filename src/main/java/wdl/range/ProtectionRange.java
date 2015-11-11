package wdl.range;

/**
 * A range of chunks.  Note that if x1 is greater than x2, they will be
 * swapped, and if z1 is greater than z2, they will be swapped.
 */
public final class ProtectionRange {
	public ProtectionRange(int x1, int z1, int x2, int z2, boolean isWhitelist) {
		this.x1 = x1;
		this.z1 = z1;
		this.x2 = x2;
		this.z2 = z2;
		this.isWhitelist = isWhitelist;
	}
	
	/**
	 * Range of coordinates.  If x1 is greater than x2, they will be swapped
	 * (and the same for y).  These are chunk coordinates.
	 */
	public final int x1, z1, x2, z2;
	/**
	 * Whether to allow downloading in the given range of chunks.
	 * 
	 * True: All downloading is allowed in that chunk.
	 * False: No downloading is allowed in that chunk.
	 */
	public final boolean isWhitelist;
}
