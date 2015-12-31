package wdl.request;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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

	@Override
	public String getLocationInfo() {
		Location loc = player.getLocation();
		return "In world " + loc.getWorld().getName() + " at "
				+ loc.getBlockX() + ", " + loc.getBlockY() + ", "
				+ loc.getBlockZ();
	}

	@Override
	public String getPlayerName() {
		return player.getName();
	}
	
	@Override
	public String getPlayerInfo() {
		return player.getDisplayName() + " (" + player.getName() + ")";
	}

	@Override
	public String getTeleportCommand() {
		return "/tp " + player.getName();
	}
	
	@Override
	public boolean isForwarded() {
		return false;
	}
	
	@Override
	protected void doAccept() {
		//TODO
	}
	
	@Override
	protected void doReject() {
		//TODO
	}
}
