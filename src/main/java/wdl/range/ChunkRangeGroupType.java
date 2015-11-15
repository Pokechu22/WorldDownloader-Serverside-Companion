package wdl.range;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 * {@link IRangeProducer} that uses chunk positions.
 */
public final class ChunkRangeGroupType implements
		IRangeGroupType<SimpleRangeProducer> {
	@Override
	public SimpleRangeProducer createRangeProducer(IRangeGroup group,
			ConfigurationSection config) {
		String tag = config.getString("tag");
		boolean whitelist = config.getBoolean("whitelist");
		int x1 = config.getInt("x1");
		int z1 = config.getInt("z1");
		int x2 = config.getInt("x2");
		int z2 = config.getInt("z2");
		String world = config.getString("world");
		
		return new SimpleRangeProducer(group, whitelist, tag, x1, z1, x2, z2,
				world);
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
		if (config.isSet("world") && !config.isString("world")) {
			errors.add("'world' must be a String or left unset!");
			hasErrors = true;
		}
		
		if (config.getInt("x1") > config.getInt("x2")) {
			warnings.add("'x1' should be not be greater than 'x2'!");
		}
		if (config.getInt("z1") > config.getInt("z2")) {
			warnings.add("'z1' should be not be greater than 'z2'!");
		}
		boolean isAllWorlds = (!config.isSet("world") || config.getString(
				"world").equals("*"));
		if (!isAllWorlds && Bukkit.getWorld(config.getString("world")) == null) {
			warnings.add("'world' (" + config.getString("world") + ") " +
					"corresponds with a world that currently does not exist!");
		}
		
		return !hasErrors;
	}
}
