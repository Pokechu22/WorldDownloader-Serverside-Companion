package wdl.factions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import wdl.range.IRangeGroup;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import com.massivecraft.factions.event.EventFactionsPermChange;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.util.MUtil;

/**
 * {@link IRangeProducer} that gives information on the <i>nearby</i> factions,
 * similar to <code>/f map</code>.
 * 
 * The reason it needs to work off of nearby factions is that the world is big.
 * If all factions were sent at once, that would be a lot of data.
 * Right now, it does send a lot of data, but that will be improved when range
 * compacting is fixed.
 */
public class NearbyFactionsRangeProducer extends BukkitRunnable implements
		IRangeProducer, Listener {
	private final FactionsSupportPlugin plugin;
	private final IRangeGroup rangeGroup;
	/**
	 * Distance in chunks to send updates.
	 */
	private final int trackDistance;
	/**
	 * List of players to update on the next tick.
	 */
	private final Set<UUID> playersNeedingUpdating = new HashSet<>();
	
	public NearbyFactionsRangeProducer(FactionsSupportPlugin plugin,
			IRangeGroup group, int trackDistance) {
		this.plugin = plugin;
		this.rangeGroup = group;
		this.trackDistance = trackDistance;
	}

	@Override
	public IRangeGroup getRangeGroup() {
		return rangeGroup;
	}

	@Override
	public List<ProtectionRange> getInitialRanges(Player player) {
		Map<PS, Faction> chunkMap = getNearbyChunks(player, trackDistance);
		List<ProtectionRange> ranges = convertChunkMapToRanges(player, chunkMap);
		
		return ranges;
	}
	
	/**
	 * Called each tick; try to empty out {@link #playersNeedingUpdating}.
	 */
	@Override
	public void run() {
		for (UUID uuid : playersNeedingUpdating) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null && rangeGroup.isWDLPlayer(player)) {
				Map<PS, Faction> chunkMap = getNearbyChunks(player,
						trackDistance);
				List<ProtectionRange> ranges = convertChunkMapToRanges(player,
						chunkMap);

				rangeGroup.setRanges(player, ranges);
			}
		}
		
		playersNeedingUpdating.clear();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPermChange(EventFactionsPermChange e) {
		if (e.getPerm() == plugin.getOrRegisterDownloadPerm()) {
			if (e.getRel().isRank()) {
				// isRank means that it's a member of the faction;
				// members of the faction should be updated directly
				// rather than searching for nearby players.
				List<MPlayer> players = e.getFaction().getMPlayersWhereRole(
						e.getRel());
				
				// TODO: It may be faster to add a nearby check, or maybe
				// not, but for now we'll update all players with this
				// relation whether they need it or not.
				for (MPlayer mplayer : players) {
					Player player = convertMPlayerToPlayer(mplayer);
					if (player != null) {
						playersNeedingUpdating.add(player.getUniqueId());
					}
				}
			} else {
				Set<String> worlds = new HashSet<String>();
				Multimap<String, PS> chunksByWorld = HashMultimap.create();
				
				for (PS ps : BoardColl.get().getChunks(e.getFaction())) {
					worlds.add(ps.getWorld());
					chunksByWorld.put(ps.getWorld(), ps);
				}
				
				//TODO: This doesn't seem to be a good algorithm.
				for (String world : worlds) {
					playerLoop: for (Player player : Bukkit.getWorld(world).getPlayers()) {
						if (!rangeGroup.isWDLPlayer(player)) {
							continue;
						}
						
						MPlayer mplayer = convertPlayerToMPlayer(player);
						
						if (e.getFaction().getRelationTo(mplayer) != e.getRel()) {
							// Permission changes only affect players with that
							// relation.
							continue;
						}
						
						PS playerPs = PS.valueOf(player);
						
						for (PS ps : chunksByWorld.get(world)) {
							if (isWithinUpdateDistance(playerPs, ps)) {
								playersNeedingUpdating.add(player.getUniqueId());
								continue playerLoop;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Called when someone's membership in a faction changes.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMembershipChange(EventFactionsMembershipChange e) {
		MPlayer mplayer = e.getMPlayer();
		Player player = convertMPlayerToPlayer(mplayer);
		if (player == null) {
			return;
		}
		
		playersNeedingUpdating.add(player.getUniqueId());
	}
	
	/**
	 * Regenerates the chunk list for all players near the changed chunks.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFactionChunksChange(EventFactionsChunksChange e) {
		Set<String> worlds = new HashSet<String>();
		Multimap<String, PS> chunksByWorld = HashMultimap.create();
		
		for (PS ps : e.getChunks()) {
			worlds.add(ps.getWorld());
			chunksByWorld.put(ps.getWorld(), ps);
		}
		//TODO: This doesn't seem to be a good algorithm.
		for (String world : worlds) {
			playerLoop: for (Player player : Bukkit.getWorld(world).getPlayers()) {
				if (!rangeGroup.isWDLPlayer(player)) {
					continue;
				}
				
				PS playerPs = PS.valueOf(player);
				
				for (PS ps : chunksByWorld.get(world)) {
					if (isWithinUpdateDistance(playerPs, ps)) {
						playersNeedingUpdating.add(player.getUniqueId());
						continue playerLoop;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerChangeChunks(PlayerMoveEvent e) {
		if (!rangeGroup.isWDLPlayer(e.getPlayer())) {
			return;
		}
		
		Location from = e.getFrom();
		Location to = e.getTo();
		
		// Didn't change chunks.
		if (from.getBlockX() >> 4 == to.getBlockX() >> 4
				&& from.getBlockZ() >> 4 == to.getBlockZ() >> 4) {
			return;
		}
		
		playersNeedingUpdating.add(e.getPlayer().getUniqueId());
	}
	
	/**
	 * Are the given chunks close enough that changes in one should notify someone
	 * in the other?
	 */
	private boolean isWithinUpdateDistance(PS ps1, PS ps2) {
		int x1 = ps1.getChunkX(true);
		int x2 = ps2.getChunkX(true);
		int z1 = ps1.getChunkZ(true);
		int z2 = ps2.getChunkZ(true);
		
		int distX = Math.abs(x1 - x2);
		int distZ = Math.abs(z1 - z2);
		
		return distX < trackDistance || distZ < trackDistance;
	}
	
	/**
	 * Gets the chunk-faction map for the chunks nearby the given player.
	 *
	 * A range of 0 returns 1 chunk, while a range of 1 returns a 3x3.
	 */
	private Map<PS, Faction> getNearbyChunks(Player player, int range) {
		PS center = PS.valueOf(player.getLocation());
		Set<PS> nearby = BoardColl.getNearbyChunks(center, range);
		
		//TODO: Verify that this is the right world.
		return BoardColl.getChunkFaction(nearby);
	}
	
	/**
	 * Converts a Map (of MassiveCraft {@link PS}s to Factions) to a list of
	 * {@link ProtectionRange}s.  Only chunks that the player can download in are
	 * given.
	 */
	public List<ProtectionRange> convertChunkMapToRanges(Player player, Map<PS, Faction> chunkMap) {
		MPlayer mplayer = convertPlayerToMPlayer(player);
		String world = player.getWorld().getName();
		List<ProtectionRange> ranges = new ArrayList<>();
		
		MPerm downloadPerm = plugin.getOrRegisterDownloadPerm();
		
		for (Map.Entry<PS, Faction> entry : chunkMap.entrySet()) {
			if (!entry.getKey().getWorld().equals(world)) {
				continue;
			}
			if (!downloadPerm.has(mplayer, entry.getValue(), false)) {
				continue;
			}
			
			int x = entry.getKey().getChunkX();
			int z = entry.getKey().getChunkZ();
			String tag = entry.getValue().getName();
			
			ranges.add(new ProtectionRange(tag, x, z, x, z));
		}
		
		return ranges;
	}
	
	/**
	 * Converts an {@link MPlayer} to a {@link Player}.
	 *
	 * @return The player, or <code>null</code> if they aren't a player, or do
	 *         not have WDL installed.
	 */
	private Player convertMPlayerToPlayer(MPlayer mplayer) {
		if (mplayer == null) {
			return null;
		}
		if (!mplayer.isPlayer()) {
			return null;
		}
		
		Player player = mplayer.getPlayer();
		if (!MUtil.isPlayer(player)) {
			// If they aren't a player (IE, they are an NPC?)
			return null;
		}
		
		if (!rangeGroup.isWDLPlayer(player)) {
			return null;
		}
		
		return player;
	}
	
	/**
	 * Converts a {@link Player} to an {@link MPlayer}.
	 *
	 * @return The mplayer.
	 */
	private MPlayer convertPlayerToMPlayer(Player player) {
		if (player == null) {
			return null;
		}
		if (!MUtil.isPlayer(player)) {
			// If they aren't a player (IE, they are an NPC?)
			return null;
		}
		
		return MPlayer.get(player);
	}
	
	@Override
	public void dispose() {
		HandlerList.unregisterAll(this);
		
		this.cancel();
	}
}
