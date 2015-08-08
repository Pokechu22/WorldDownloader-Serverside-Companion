package wdl;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigValidation {
	public static void validateConfig(ConfigurationSection config,
			CommandSender warnTo) {
		if (!config.isSet("wdl.canDoNewThings")) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting " + 
					"'wdl.canDoNewThings' is not set!");
		} else if (!config.isBoolean("wdl.canDoNewThings")) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					"'wdl.canDoNewThings' is not a boolean!");
		}
		if (!config.isSet("wdl.canDownloadInGeneral")) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting " + 
					"'wdl.canDownloadInGeneral' is not set!");
		} else if (!config.isBoolean("wdl.canDownloadInGeneral")) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					"'wdl.canDownloadInGeneral' is not a boolean!");
		}
		if (!config.isSet("wdl.saveRadius")) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting " + 
					"'wdl.saveRadius' is not set!");
		} else if (!config.isInt("wdl.saveRadius")) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					"'wdl.saveRadius' is not an integer!");
		}
		if (!config.isSet("wdl.canCacheChunks")) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting " + 
					"'wdl.canCacheChunks' is not set!");
		} else if (!config.isBoolean("wdl.canCacheChunks")) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					"'wdl.canCacheChunks' is not a boolean!");
		}
		if (!config.isSet("wdl.canSaveEntities")) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting " + 
					"'wdl.canSaveEntities' is not set!");
		} else if (!config.isBoolean("wdl.canSaveEntities")) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					"'wdl.canSaveEntities' is not a boolean!");
		}
		if (!config.isSet("wdl.canSaveContainers")) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting " + 
					"'wdl.canSaveContainers' is not set!");
		} else if (!config.isBoolean("wdl.canSaveContainers")) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					"'wdl.canSaveContainers' is not a boolean!");
		}
		if (!config.isSet("wdl.sendEntityRanges")) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting " + 
					"'wdl.sendEntityRanges' is not set!");
		} else if (!config.isBoolean("wdl.sendEntityRanges")) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					"'wdl.sendEntityRanges' is not a boolean!");
		}
		
		if (config.getInt("wdl.saveRadius") != -1 && 
				config.getBoolean("wdl.canCacheChunks") == true) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting " +
					"'wdl.saveRadius' is set, but 'wdl.canCacheChunks' " + 
					"is set to true!");
			warnTo.sendMessage("§eWDL ignores the saveRadius value when " +
					"chunk caching is enabled, due to technical constraints.");
		}
		
		if (!config.isSet("wdl.logMode")) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting " + 
					"'wdl.logMode' is not set!");
		} else if (!config.isString("wdl.logMode")) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					"'wdl.logMode' is not one of the valid options!");
			warnTo.sendMessage("§c[WDL] Must be 'none', 'individual', or " +
					"'combined'!");
		} else {
			String logMode = config.getString("wdl.logMode");
			
			if (!(logMode.equalsIgnoreCase("none")
					|| logMode.equalsIgnoreCase("individual") 
					|| logMode.equalsIgnoreCase("combined"))) {
				warnTo.sendMessage("§c[WDL] ERROR: Config setting "
						+ "'wdl.logMode' is not one of the valid options!");
				warnTo.sendMessage("§c[WDL] Must be 'none', 'individual', or " +
						"'combined'!");
			}
		}
	}
}
