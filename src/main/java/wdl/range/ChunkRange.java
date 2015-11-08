package wdl.range;

/**
 * A range of chunks.  Note that if x1 is greater than x2, they will be
 * swapped, and if y1 is greater than y2, they will be swapped.
 */
public final class ChunkRange {
	public ChunkRange(int x1, int y1, int x2, int y2, boolean isWhitelist) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.isWhitelist = isWhitelist;
	}
	
	/**
	 * Range of coordinates.  If x1 is greater than x2, they will be swapped
	 * (and the same for y).  These are chunk coordinates.
	 */
	public final int x1, y1, x2, y2;
	/**
	 * Whether to allow downloading in the given range of chunks.
	 * 
	 * True: All downloading is allowed in that chunk.
	 * False: No downloading is allowed in that chunk.
	 */
	public final boolean isWhitelist;
}
