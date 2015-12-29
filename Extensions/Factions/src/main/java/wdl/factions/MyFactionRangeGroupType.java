package wdl.factions;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;

public class MyFactionRangeGroupType implements IRangeGroupType<MyFactionRangeProducer> {
	private final FactionsSupportPlugin plugin;
	
 	public MyFactionRangeGroupType(FactionsSupportPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public MyFactionRangeProducer createRangeProducer(IRangeGroup group,
			ConfigurationSection config) {
		MyFactionRangeProducer producer = new MyFactionRangeProducer(group,
				plugin);
		Bukkit.getPluginManager().registerEvents(producer, plugin);
		return producer;
	}
	
	@Override
	public boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors) {
		return true;
	}
	
	@Override
	public void dispose() { }
}
