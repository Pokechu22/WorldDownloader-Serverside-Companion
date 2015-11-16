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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors) {
		// TODO Auto-generated method stub
		return false;
	}
}
