package wdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import wdl.range.IRangeProducer;

public class ConfigValidation {
	/**
	 * Options that are valid for per-world config sections.
	 */
	private static final List<String> worldConfigOptions = Arrays.asList(
			"canDoNewThings", "canDownloadInGeneral", "saveRadius",
			"canCacheChunks", "canSaveEntities", "canSaveTileEntities", 
			"canSaveContainers", "sendEntityRanges", "requestMessage");
	/**
	 * Options that are valid for the main config.
	 */
	private static final List<String> generalConfigOptions = Arrays.asList(
			//Per-world and main config
			"canDoNewThings", "canDownloadInGeneral", "saveRadius",
			"canCacheChunks", "canSaveEntities", "canSaveTileEntities", 
			"canSaveContainers", "sendEntityRanges", "requestMessage",
			//Main-config specific
			"logMode", "per-world", "chunkOverrides");
	
	/**
	 * Validates the entire configuration.
	 * 
	 * @param config The root configuration.
	 * @param warnTo The player to complain at if there's something wrong.
	 * @param plugin The instance of the plugin.
	 */
	public static void validateConfig(Configuration config,
			CommandSender warnTo, WDLCompanion plugin) {
		ConfigurationSection section = config.getConfigurationSection("wdl");
		
		validateIsBool("canDoNewThings", section, warnTo);
		validateIsBool("canDownloadInGeneral", section, warnTo);
		validateIsInt("saveRadius", section, warnTo);
		validateIsBool("canCacheChunks", section, warnTo);
		validateIsBool("canSaveEntities", section, warnTo);
		validateIsBool("canSaveTileEntities", section, warnTo);
		validateIsBool("canSaveContainers", section, warnTo);
		validateIsBool("sendEntityRanges", section, warnTo);
		validateIsString("requestMessage", section, warnTo);
		
		validateStringLength("requestMessage", section, 15000, warnTo);
		
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
					"'wdl.logMode' is not set!  The default value of " + 
					config.get("wdl.logMode") + " will be used instead!");
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
		
		// Check for the per-world options.
		if (section.isSet("per-world")) {
			if (!section.isConfigurationSection("per-world")) {
				warnTo.sendMessage("§c[WDL] ERROR: Per-world configs (wdl." + 
						"per-world) is not a mapping!  Per-world configs " +
						"will not be used!");
			} else {
				ConfigurationSection perWorld = section
						.getConfigurationSection("per-world");
				Set<String> worlds = perWorld.getKeys(false);
				
				for (String world : worlds) {
					if (Bukkit.getWorld(world) == null) {
						warnTo.sendMessage("§e[WDL] WARNING: Config setting " + 
								"wdl.per-world." + world + " corresponds with " +
								"a world that does not exist!");
					}
					
					validateWorldSection(world, config, warnTo, plugin);
				}
			}
		}
		
		// Check for the per-world options.
		if (section.isSet("chunkOverrides")) {
			if (!section.isConfigurationSection("chunkOverrides")) {
				warnTo.sendMessage("§c[WDL] ERROR: Chunk overrides (wdl." + 
						"chunkOverrides) is not a mapping!  Chunk overrides " +
						"will not be used!");
			} else {
				ConfigurationSection chunkOverrides = section
						.getConfigurationSection("chunkOverrides");
				List<String> toRemove = new ArrayList<>();
				
				for (String key : chunkOverrides.getKeys(false)) {
					if (!chunkOverrides.isConfigurationSection(key)) {
						warnTo.sendMessage("§c[WDL] ERROR: Chunk override '" + key
								+ "' is not a mapping!  It will be ignored!");
						toRemove.add(key);
					}
					ConfigurationSection chunkOverride = (ConfigurationSection)
							chunkOverrides.getConfigurationSection(key);
					if (!validateChunkOverride(chunkOverride, key, warnTo, plugin)) {
						toRemove.add(key);
					}
				}
				
				for (String key : toRemove) {
					chunkOverrides.set(key, null);
				}
			}
		}
		
		List<String> keys = new ArrayList<>(section.getKeys(false));
		keys.removeAll(generalConfigOptions);
		if (!keys.isEmpty()) {
			warnTo.sendMessage("§c[WDL] WARNING: There are config settings " +
					"that are not recognised!  They are:");
			for (String key : keys) {
				warnTo.sendMessage("§c[WDL]   * " + section.getCurrentPath()
						+ "." + key);
			}
		}
	}
	
	/**
	 * Validates a specific world's configuration.
	 * 
	 * @param worldName The name of the world. 
	 * @param config The root configuration.
	 * @param warnTo The player to complain to if something is wrong.
	 * @param plugin The instance of the plugin.
	 */
	private static void validateWorldSection(String worldName,
			Configuration config, CommandSender warnTo, WDLCompanion plugin) {
		String fullKey = "wdl.per-world." + worldName; 
		if (!config.isSet(fullKey)) {
			warnTo.sendMessage("§e[WDL] WARNING: Per-world config validation " +
					"issue -- tested wdl.per-world." + worldName + ", but it " +
					"doesn't exist!  This should NOT happen!");
			return;
		}
		if (!config.isConfigurationSection(fullKey)) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					fullKey + " is not a mapping!  The global values " +
					"will be used instead!");
		}
		
		ConfigurationSection section = config.getConfigurationSection(fullKey);
		
		validateIsBoolOrUnset("canDoNewThings", section, warnTo);
		validateIsBoolOrUnset("canDownloadInGeneral", section, warnTo);
		validateIsIntOrUnset("saveRadius", section, warnTo);
		validateIsBoolOrUnset("canCacheChunks", section, warnTo);
		validateIsBoolOrUnset("canSaveEntities", section, warnTo);
		validateIsBoolOrUnset("canSaveTileEntities", section, warnTo);
		validateIsBoolOrUnset("canSaveContainers", section, warnTo);
		validateIsBoolOrUnset("sendEntityRanges", section, warnTo);
		validateIsStringOrUnset("requestMessage", section, warnTo);
		
		validateStringLength("requestMessage", section, 15000, warnTo);
		
		List<String> keys = new ArrayList<>(section.getKeys(false));
		keys.removeAll(worldConfigOptions);
		if (!keys.isEmpty()) {
			warnTo.sendMessage("§c[WDL] WARNING: There are config settings " +
					"that are not recongised!  They are:");
			for (String key : keys) {
				warnTo.sendMessage("§c[WDL]   * " + section.getCurrentPath()
						+ "." + key);
			}
		}
	}
	
	/**
	 * Validates that the given key is set and is a boolean, warning the
	 * player if not.
	 * 
	 * @param key The key within the given section.
	 * @param config The section of the config to check.
	 * @param warnTo The player to complain to if something is wrong.
	 */
	private static void validateIsBool(String key, ConfigurationSection config, 
			CommandSender warnTo) {
		String fullKey = (config.getCurrentPath().isEmpty() ? key : config
				.getCurrentPath() + "." + key);
		
		if (!config.isSet(key)) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting '" + 
					fullKey + "' is not set!  The default value of " +
					config.getBoolean(key) + " will be used instead!");
		} else if (!config.isBoolean(key)) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					fullKey + " is not a boolean!  The default value of " +
					config.getBoolean(key) + " will be used instead!");
		}
	}
	
	/**
	 * Validates that the given key is set and is an int, warning the 
	 * player if not.
	 * 
	 * @param key The key within the given section.
	 * @param config The section of the config to check.
	 * @param warnTo The player to complain to if something is wrong.
	 */
	private static void validateIsInt(String key, ConfigurationSection config, 
			CommandSender warnTo) {
		String fullKey = (config.getCurrentPath().isEmpty() ? key : config
				.getCurrentPath() + "." + key);
		
		if (!config.isSet(key)) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting '" + 
					fullKey + "' is not set!  The default value of " +
					config.getInt(key) + " will be used instead!");
		} else if (!config.isInt(key)) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					fullKey + " is not an integer!  The default value of " +
					config.getInt(key) + " will be used instead!");
		}
	}
	
	/**
	 * Validates that the given key is set and is a String, warning the 
	 * player if not.
	 * 
	 * @param key The key within the given section.
	 * @param config The section of the config to check.
	 * @param warnTo The player to complain to if something is wrong.
	 */
	private static void validateIsString(String key, ConfigurationSection config, 
			CommandSender warnTo) {
		String fullKey = (config.getCurrentPath().isEmpty() ? key : config
				.getCurrentPath() + "." + key);
		
		if (!config.isSet(key)) {
			warnTo.sendMessage("§e[WDL] WARNING: Config setting '" + 
					fullKey + "' is not set!  The default value of \"" +
					config.getString(key) + "\" will be used instead!");
		} else if (!config.isString(key)) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					fullKey + " is not a string!  The default value of \"" +
					config.getString(key) + "\" will be used instead!");
		}
	}
	
	/**
	 * Validates that the given key is a boolean or is unset, warning the
	 * player if not.
	 * 
	 * @param key The key within the given section.
	 * @param config The section of the config to check.
	 * @param warnTo The player to complain to if something is wrong.
	 */
	private static void validateIsBoolOrUnset(String key,
			ConfigurationSection config, CommandSender warnTo) {
		String fullKey = (config.getCurrentPath().isEmpty() ? key : config
				.getCurrentPath() + "." + key);
		
		if (config.isSet(key) && !config.isBoolean(key)) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					fullKey + " is not a boolean!  The default value of " +
					config.getBoolean(key) + " will be used instead!");
		}
	}
	
	/**
	 * Validates that the given key is an int or is unset, warning the 
	 * player if not.
	 * 
	 * @param key The key within the given section.
	 * @param config The section of the config to check.
	 * @param warnTo The player to complain to if something is wrong.
	 */
	private static void validateIsIntOrUnset(String key,
			ConfigurationSection config, CommandSender warnTo) {
		String fullKey = (config.getCurrentPath().isEmpty() ? key : config
				.getCurrentPath() + "." + key);
		
		if (config.isSet(key) && !config.isBoolean(key)) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					fullKey + " is not an integer!  The default value of " +
					config.getInt(key) + " will be used instead!");
		}
	}
	
	/**
	 * Validates that the given key is a String or is unset, warning the 
	 * player if not.
	 * 
	 * @param key The key within the given section.
	 * @param config The section of the config to check.
	 * @param warnTo The player to complain to if something is wrong.
	 */
	private static void validateIsStringOrUnset(String key,
			ConfigurationSection config, CommandSender warnTo) {
		String fullKey = (config.getCurrentPath().isEmpty() ? key : config
				.getCurrentPath() + "." + key);
		
		if (config.isSet(key) && !config.isString(key)) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + 
					fullKey + " is not an integer!  The default value of " +
					config.getString(key) + " will be used instead!");
		}
	}
	
	/**
	 * Validates that the given key is a string with a length shorter
	 * than the given length.
	 * 
	 * @param key The key within the given section.
	 * @param config The section of the config to check.
	 * @param maxLength The maximum length for the string.
	 * @param warnTo The player to complain to if something is wrong.
	 */
	private static void validateStringLength(String key,
			ConfigurationSection config, int maxLength, CommandSender warnTo) {
		String fullKey = (config.getCurrentPath().isEmpty() ? key : config
				.getCurrentPath() + "." + key);

		//Don't check that it's set specifically -- that will be handled
		//elsewhere.
		if (config.get(key) != null
				&& config.getString(key).length() >= maxLength) {
			warnTo.sendMessage("§c[WDL] ERROR: Config setting " + fullKey
					+ " is too long!  It must be shorter than " + maxLength
					+ " characters!");
		}
	}
	
	/**
	 * Validates the given chunk override.
	 * 
	 * @param override The {@link ConfigurationSection} for the chunk override.
	 * @param key ID of the override
	 * @param warnTo The player to complain to if something is wrong.
	 * @param plugin The instance of the plugin.
	 * @return True if the override is valid, false otherwise.
	 */
	private static boolean validateChunkOverride(ConfigurationSection override,
			String key, CommandSender warnTo, WDLCompanion plugin) {
		if (!override.isString("type")) {
			 warnTo.sendMessage("§c[WDL] ERROR: 'type' for chunk " 
					+ "override '" + key + "' is not set or not a "
					+ "string!  It will be ignored.");
			 return false;
		}
		String type = override.getString("type");
		if (!plugin.rangeProducers.containsKey(type)) {
			 warnTo.sendMessage("§c[WDL] ERROR: 'type' for chunk " 
						+ "override '" + key + "' is not a valid option!  "
						+ "Currently '" + type + "', expected one of "
						+ plugin.rangeProducers.keySet() + ".  " 
						+ "It will be ignored.");
			 return false;
		}
		
		IRangeProducer producer = plugin.rangeProducers.get(type);
		
		List<String> warnings = new ArrayList<>();
		List<String> errors = new ArrayList<>();
		
		if (!producer.isValidConfig(override, warnings, errors)) {
			for (String s : warnings) {
				warnTo.sendMessage("§e[WDL] WARNING: " + s);
			}
			for (String s : errors) {
				warnTo.sendMessage("§c[WDL] ERROR: " + s);
			}
			warnTo.sendMessage("§c[WDL] ERROR: Config for chunk " 
					+ "override '" + key + "' is fatally incorrect!  "
					+ "It will be ignored.");
			return false;
		}
		return true;
	}
}
