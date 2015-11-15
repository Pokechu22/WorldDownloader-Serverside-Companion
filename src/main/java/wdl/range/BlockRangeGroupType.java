package wdl.range;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

/**
 * {@link IRangeProducer} that uses block positions.
 */
public final class BlockRangeGroupType implements
		IRangeGroupType<SimpleRangeProducer> {
	@Override
	public SimpleRangeProducer createRangeProducer(IRangeGroup group,
			ConfigurationSection config) {
		String tag = config.getString("tag");
		boolean whitelist = config.getBoolean("whitelist");
		int x1 = config.getInt("x1") / 16;
		int z1 = config.getInt("z1") / 16;
		int x2 = config.getInt("x2") / 16;
		int z2 = config.getInt("z2") / 16;
		
		return new SimpleRangeProducer(group, whitelist, tag, x1, z1, x2, z2);
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
		if (!config.isInt("z1")) {
			errors.add("'z1' must be an int!");
			hasErrors = true;
		}
		if (!config.isInt("z2")) {
			errors.add("'z2' must be an int!");
			hasErrors = true;
		}
		
		if (config.getInt("x1") > config.getInt("x2")) {
			warnings.add("'x1' should be not be greater than 'x2'!");
		}
		if (config.getInt("z1") > config.getInt("z2")) {
			warnings.add("'z1' should be not be greater than 'z2'!");
		}
		
		return !hasErrors;
	}
}
