package wdl.range;

/**
 * {@link IProtectionRange} over a range of chunks.
 */
public class ChunkProtectionRange implements IProtectionRange {
	public ChunkProtectionRange(int chunkX1, int chunkY1, int chunkX2,
			int chunkY2, boolean isWhitelist) {
		this.chunkX1 = chunkX1;
		this.chunkY1 = chunkY1;
		this.chunkX2 = chunkX2;
		this.chunkY2 = chunkY2;
		this.isWhitelist = isWhitelist;
	}

	private final int chunkX1, chunkY1, chunkX2, chunkY2;
	private final boolean isWhitelist;
	
	@Override
	public int getChunkX1() {
		return chunkX1;
	}

	@Override
	public int getChunkY1() {
		return chunkY1;
	}

	@Override
	public int getChunkX2() {
		return chunkX2;
	}

	@Override
	public int getChunkY2() {
		return chunkY2;
	}

	@Override
	public boolean isWhitelist() {
		return isWhitelist;
	}

}
