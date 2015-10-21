package wdl.range;

public interface IProtectionRange {
	/**
	 * Gets the lowest chunk x coordinate.
	 * @return
	 */
	public abstract int getChunkX1();
	/**
	 * Gets the lowest chunk y coordinate.
	 * @return
	 */
	public abstract int getChunkY1();
	/**
	 * Gets the lowest chunk y coordinate.
	 * @return
	 */
	public abstract int getChunkX2();
	/**
	 * Gets the highest chunk y coordinate.
	 * @return
	 */
	public abstract int getChunkY2();
}
