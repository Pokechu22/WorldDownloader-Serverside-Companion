package wdl;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import wdl.range.ProtectionRange;

/**
 * Permissions were requested via a WDL|REQUEST plugin message.
 */
public class PluginChannelPermissionsRequestedEvent extends
		PermissionsRequestedEvent {
	private final Player player;
	
	public PluginChannelPermissionsRequestedEvent(Player player,
			String requestReason, Map<String, String> requestedPerms,
			List<ProtectionRange> rangeRequests) {
		super(requestReason, requestedPerms, rangeRequests);
		
		this.player = player;
	}

	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public String getLocationInfo() {
		Location loc = player.getLocation();
		return "In world " + loc.getWorld().getName() + " at "
				+ loc.getBlockX() + ", " + loc.getBlockY() + ", "
				+ loc.getBlockZ();
	}

	@Override
	public String getPlayerInfo() {
		return player.getCustomName() + " (" + player.getName() + ")";
	}

	@Override
	public String getTeleportCommand() {
		return "/tp " + player.getName();
	}
}
