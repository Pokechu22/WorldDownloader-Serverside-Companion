package wdl.range;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

/**
 * {@link IProtectionRange} over a range of chunks.
 */
public class ChunkProtectionRange implements IProtectionRange {
	public ChunkProtectionRange(ConfigurationSection section)
			throws InvalidConfigurationException {
		if (!section.isSet("x1")) { throw new InvalidConfigurationException("x1 is not set"); }
		if (!section.isInt("x1")) { throw new InvalidConfigurationException("x1 not an int"); }
		if (!section.isSet("y1")) { throw new InvalidConfigurationException("y1 is not set"); }
		if (!section.isInt("y1")) { throw new InvalidConfigurationException("y1 not an int"); }
		if (!section.isSet("x2")) { throw new InvalidConfigurationException("x2 is not set"); }
		if (!section.isInt("x2")) { throw new InvalidConfigurationException("x2 not an int"); }
		if (!section.isSet("y2")) { throw new InvalidConfigurationException("y2 is not set"); }
		if (!section.isInt("y2")) { throw new InvalidConfigurationException("y2 not an int"); }
		if (!section.isSet("whitelist")) { throw new InvalidConfigurationException("whitelist is not set"); }
		if (!section.isBoolean("whitelist")) { throw new InvalidConfigurationException("whitelist not a boolean"); }
		
		this.chunkX1 = section.getInt("x1");
		this.chunkY1 = section.getInt("y1");
		this.chunkX2 = section.getInt("x2");
		this.chunkY2 = section.getInt("y2");
		this.isWhitelist = section.getBoolean("whitelist");
	}
	
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
