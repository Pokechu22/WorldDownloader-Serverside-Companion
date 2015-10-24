package wdl.range;

import org.bukkit.configuration.InvalidConfigurationException;

/**
 * Base interface for any protection range.
 * <br/>
 * Used to either explicitly <b>blacklist</b> / disable saving of
 * or <b>whitelist</b> allow saving of the chunk, even if saving is
 * normally disabled.
 * <br/>
 * Additionally, implementors will need a constructor taking a
 * ConfigurationSection.  This constructor may throw a
 * {@link InvalidConfigurationException} if the configuration is
 * not acceptable.
 */
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
	
	/**
	 * Is this range a whitelist or a blacklist?
	 * 
	 * @return <code>true</code> to whitelist the given chunks,
	 *         <code>false</code> to blacklist the given chunks.
	 */
	public abstract boolean isWhitelist();
}
