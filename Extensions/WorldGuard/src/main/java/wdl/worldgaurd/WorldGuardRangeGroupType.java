package wdl.worldgaurd;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;

public class WorldGuardRangeGroupType implements
		IRangeGroupType<WorldGuardRangeProducer> {

	@Override
	public boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors) {
		if (!config.isSet("ownershipType")) {
			warnings.add("'ownershipType' is not specified!  The default, " +
					"OWNER_OR_MEMBER (either owner or member), will be used.");
		} else if (!config.isString("ownershipType")
				|| !(OwnershipType.NAMES.contains(config
						.getString("ownershipType").toUpperCase()))) {
			errors.add("'ownershipType' must be a String with one of these " + 
					"values: " + OwnershipType.NAMES);
			return false;
		}
		return true;
	}

	@Override
	public WorldGuardRangeProducer createRangeProducer(IRangeGroup group,
			ConfigurationSection config) {
		return new WorldGuardRangeProducer(group, 
				OwnershipType.match(config.getString("ownershipType")));
	}

	@Override
	public void dispose() { }

}
