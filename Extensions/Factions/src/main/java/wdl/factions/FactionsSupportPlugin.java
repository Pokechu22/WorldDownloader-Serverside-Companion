package wdl.factions;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.event.EventFactionsCreatePerms;

import wdl.RangeGroupTypeRegistrationEvent;

public class FactionsSupportPlugin extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getOrRegisterDownloadPerm();
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void registerPerm(EventFactionsCreatePerms e) {
		getOrRegisterDownloadPerm();
	}
	
	@EventHandler
	public void registerRangeGroupTypes(RangeGroupTypeRegistrationEvent e) {
		e.addRegistration("Owned faction", new MyFactionRangeGroupType(this));
		e.addRegistration("Nearby factions", new NearbyFactionsRangeGroupType(this));
	}
	
	/**
	 * Gets or creates a permission that is applied to factions.
	 */
	public MPerm getOrRegisterDownloadPerm() {
		//Priority controls list order.  We may want to allow editing this.
		int priority = 1500;
		//Name and ID _can_ be different, but shouldn't be?  Maybe allow them to be different if needed.
		//Like, if there are two flags, we'll want both to have the same name but different IDS?
		String id = "download";
		String name = id;
		//Description
		String desc = "use the World Downloader mod";
		//What faction relations can, by default, do the thing provided by this perm.
		//Probably called standard because 'default' is a keyword.
		Set<Rel> standard = EnumSet.of(Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.RECRUIT, Rel.ALLY);
		//Does it affect territory usage?
		//I really don't know whether this should be true or false.
		//Quoting the factions doc:
		// Is this a territory perm meaning it has to do with territory construction, modification or interaction?
		// True Examples: build, container, door, lever etc.
		// False Examples: name, invite, home, sethome, deposit, withdraw etc.
		//It could go either way.  I'll go with false for now.
		boolean territory = false;
		//Can non-admins edit this flag (for their own faction, I hope)?
		boolean editable = true;
		//Should this flag be shown?
		boolean visible = true;
		
		//Create ('creative') or get a permission.
		return MPerm.getCreative(priority, id, name, desc, standard, territory, editable, visible);
	}
}
