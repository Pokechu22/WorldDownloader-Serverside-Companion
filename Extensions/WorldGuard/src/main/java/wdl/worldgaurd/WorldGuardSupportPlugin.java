package wdl.worldgaurd;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import wdl.RangeGroupTypeRegistrationEvent;

public class WorldGuardSupportPlugin extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void registerRangeGroupTypes(RangeGroupTypeRegistrationEvent e) {
		e.addRegistration("Owned WorldGuard regions", new WorldGuardRangeGroupType());
	}
}
