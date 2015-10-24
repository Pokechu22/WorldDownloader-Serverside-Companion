package wdl.range;

/**
 * {@link IProtectionRange} over a range of blocks.
 */
public class BlockProtectionRange implements IProtectionRange {
	public BlockProtectionRange(int chunkX1, int chunkY1, int chunkX2,
			int chunkY2, boolean isWhitelist) {
		this.blockX1 = chunkX1;
		this.blockY1 = chunkY1;
		this.blockX2 = chunkX2;
		this.blockY2 = chunkY2;
		this.isWhitelist = isWhitelist;
	}

	private final int blockX1, blockY1, blockX2, blockY2;
	private final boolean isWhitelist;
	
	@Override
	public int getChunkX1() {
		return blockX1 / 16;
	}

	@Override
	public int getChunkY1() {
		return blockY1 / 16;
	}

	@Override
	public int getChunkX2() {
		return blockX2 / 16;
	}

	@Override
	public int getChunkY2() {
		return blockY2 / 16;
	}

	@Override
	public boolean isWhitelist() {
		return isWhitelist;
	}

}
