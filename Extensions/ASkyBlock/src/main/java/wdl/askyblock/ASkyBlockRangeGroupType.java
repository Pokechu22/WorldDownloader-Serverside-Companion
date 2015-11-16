package wdl.askyblock;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;

public class ASkyBlockRangeGroupType implements
		IRangeGroupType<ASkyBlockRangeProducer> {
	@Override
	public ASkyBlockRangeProducer createRangeProducer(IRangeGroup group,
			ConfigurationSection config) {
		return null;
	}

	@Override
	public boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors) {
		if (!config.isString("requiredPerm")) {
			errors.add("'requiredPerm' must be one of 'OWNER', 'TEAM_MEMBER', "
					+ "or 'COOP'!");
			return false;
		}
		String requiredPerm = config.getString("requiredPerm");
		if (PermLevel.parse(requiredPerm) == null) {
			errors.add("'requiredPerm' must be one of 'OWNER', 'TEAM_MEMBER', "
					+ "or 'COOP'; currently set to '" + requiredPerm + "'!");
			return false;
		}
		return true;
	}
}
