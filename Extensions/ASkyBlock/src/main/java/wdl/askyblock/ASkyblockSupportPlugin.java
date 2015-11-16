package wdl.askyblock;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import wdl.RangeGroupTypeRegistrationEvent;

/**
 * Plugin that provides simple support for ASkyBlock chunk overrides.
 * 
 * @see https://github.com/tastybento/askyblock
 */
public class ASkyblockSupportPlugin extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void registerRangeGroupTypes(RangeGroupTypeRegistrationEvent e) {
		e.addRegistration("ASkyBlock island", new ASkyBlockRangeGroupType());
	}
}
