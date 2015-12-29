package wdl.factions;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;

public class NearbyFactionsRangeGroupType implements IRangeGroupType<NearbyFactionsRangeProducer> {
	private final FactionsSupportPlugin plugin;
	
	public NearbyFactionsRangeGroupType(FactionsSupportPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors) {
		int minDistance = Bukkit.getViewDistance() + 2;
		if (!config.isSet("distance")) {
			errors.add("'distance' (distance from a player to send faction " +
					"info) must be set!  (Minimum value: " + minDistance + ")");
			return false;
		} else if (!config.isInt("distance")) {
			errors.add("'distance' (distance from a player to send faction " +
					"info) must be an integer!  (Minimum value: " +
					minDistance + ")");
			return false;
		}
		int distance = config.getInt("distance");
		//Technical restriction
		if (distance < Bukkit.getViewDistance() + 2) {
			errors.add("'distance' (distance from a player to send faction " +
					"info) must be at least " + minDistance +
					" (server view distance + 2).  " +
					"This is a technical restriction, sorry.");
			return false;
		}
		return true;
	}

	@Override
	public NearbyFactionsRangeProducer createRangeProducer(IRangeGroup group,
			ConfigurationSection config) {
		NearbyFactionsRangeProducer producer = new NearbyFactionsRangeProducer(
				plugin, group, config.getInt("distance"));
		
		producer.runTaskTimer(plugin, 1, 1);
		Bukkit.getPluginManager().registerEvents(producer, plugin);
		
		return producer;
	}

	@Override
	public void dispose() { }
}
