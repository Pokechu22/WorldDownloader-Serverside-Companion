package wdl.worldgaurd;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;

public class WorldGuardRangeGroupType implements
		IRangeGroupType<WorldGuardRangeProducer> {

	private final WorldGuardSupportPlugin plugin;
	
	public WorldGuardRangeGroupType(WorldGuardSupportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors) {
		//TODO: There's a bit more here - see todo on WorldGaurdRangeProducer.
		return true;
	}

	@Override
	public WorldGuardRangeProducer createRangeProducer(IRangeGroup group,
			ConfigurationSection config) {
		return new WorldGuardRangeProducer(group);
	}

	@Override
	public void dispose() { }

}
