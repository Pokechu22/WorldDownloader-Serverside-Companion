package wdl;

/**
 * Standard range of areas, eg for chunks.
 */
public class ProtectionRange {
	public ProtectionRange(boolean whitelist, int x1, int y1, int x2, int y2) {
		this.isWhitelist = whitelist;
		
		//Ensure that coordinates are always in the right order.
		if (x1 <= x2) {
			this.x1 = x1;
			this.x2 = x2;
		} else {
			this.x1 = x2;
			this.x2 = x1;
		}
		
		if (y1 <= y2) {
			this.y1 = y1;
			this.y2 = y2;
		} else {
			this.y1 = y2;
			this.y2 = y1;
		}
	}

	public final boolean isWhitelist;
	
	/**
	 * Coordinates.
	 * 
	 * number 2 is never greater than number 1.
	 */
	public final int x1, y1, x2, y2;
}
