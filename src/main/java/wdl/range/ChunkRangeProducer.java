package wdl.range;

import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * {@link IRangeProducer} that uses chunk positions.
 */
public class ChunkRangeProducer implements IRangeProducer {
	@Override
	public List<ProtectionRange> getRanges(Player player, ConfigurationSection config) {
		boolean whitelist = config.getBoolean("whitelist");
		int x1 = config.getInt("x1");
		int y1 = config.getInt("y1");
		int x2 = config.getInt("x2");
		int y2 = config.getInt("y2");
		
		return Arrays.asList(new ProtectionRange(x1, y1, x2, y2, whitelist));
	}

	@Override
	public boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors) {
		boolean hasErrors = false;
		if (!config.isBoolean("whitelist")) {
			errors.add("'whitelist' must be a boolean!");
			hasErrors = true;
		}
		if (!config.isInt("x1")) {
			errors.add("'x1' must be an int!");
			hasErrors = true;
		}
		if (!config.isInt("x2")) {
			errors.add("'x2' must be an int!");
			hasErrors = true;
		}
		if (!config.isInt("y1")) {
			errors.add("'y1' must be an int!");
			hasErrors = true;
		}
		if (!config.isInt("y2")) {
			errors.add("'y2' must be an int!");
			hasErrors = true;
		}
		
		if (config.getInt("x1") > config.getInt("x2")) {
			warnings.add("'x1' should be not be greater than 'x2'!");
		}
		if (config.getInt("y1") > config.getInt("y2")) {
			warnings.add("'y1' should be not be greater than 'y2'!");
		}
		
		return !hasErrors;
	}
}
