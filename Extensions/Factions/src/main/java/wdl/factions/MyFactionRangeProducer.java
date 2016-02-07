package wdl.factions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.massivecraft.factions.entity.Board;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import com.massivecraft.factions.event.EventFactionsPermChange;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.util.MUtil;

import wdl.range.IRangeGroup;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

public class MyFactionRangeProducer implements IRangeProducer, Listener {
	private final IRangeGroup rangeGroup;
	private final FactionsSupportPlugin plugin;
	
	public MyFactionRangeProducer(IRangeGroup rangeGroup,
			FactionsSupportPlugin plugin) {
		this.rangeGroup = rangeGroup;
		this.plugin = plugin;
	}
	
	@Override
	public List<ProtectionRange> getInitialRanges(Player player) {
		ArrayList<ProtectionRange> ranges = new ArrayList<>();
		MPlayer mplayer = MPlayer.get(player);
		
		if (mplayer.hasFaction()) {
			Faction faction = mplayer.getFaction();
			MPerm downloadPerm = plugin.getOrRegisterDownloadPerm();
			if (downloadPerm.has(mplayer, faction, false)) {
				Set<PS> positions = getFactionPositions(player.getWorld(), faction);
				ranges.addAll(convertPSToRanges(player.getWorld(), positions, faction));
			}
		}
		
		return ranges;
	}

	@Override
	public IRangeGroup getRangeGroup() {
		return rangeGroup;
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
		
		Set<PS> newPS = getFactionPositions(player.getWorld(), e.getNewFaction());
		List<ProtectionRange> newRanges = convertPSToRanges(player.getWorld(), newPS, e.getNewFaction());
		rangeGroup.setRanges(player, newRanges);
	}
	
	/**
	 * Called when the permission for a certain relation within a faction
	 * changes.  Updates the permissions for the affected players.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPermChange(EventFactionsPermChange e) {
		if (e.getPerm() == plugin.getOrRegisterDownloadPerm()) {
			List<MPlayer> players = e.getFaction().getMPlayersWhereRole(
					e.getRel());
			
			for (MPlayer mplayer : players) {
				Player player = convertMPlayerToPlayer(mplayer);
				
				if (player == null) {
					continue;
				}
				if (e.getNewValue() == true) {
					Set<PS> newPS = getFactionPositions(player.getWorld(),
							e.getFaction());
					List<ProtectionRange> newRanges = convertPSToRanges(
							player.getWorld(), newPS, e.getFaction());
					// Give the player their new ranges
					rangeGroup.setRanges(player, newRanges);
				} else {
					// Remove the old ranges
					rangeGroup.setRanges(player, new ProtectionRange[0]);
				}
			}
		}
	}
	
	/**
	 * Called when a faction's chunks change.  Regenerates the chunk list of
	 * all players who were effected.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFactionChunksChange(EventFactionsChunksChange e) {
		//NOTE: This is NOT an exhaustive list.  It only covers changed chunks.
		//It should be safe to assume that these chunks are no longer for that faction.
		Map<Faction, Set<PS>> oldFactionChunk = e.getOldFactionChunks();
		Set<Faction> oldFactions = oldFactionChunk.keySet();
		
		MPerm downloadPerm = plugin.getOrRegisterDownloadPerm();
		
		// Build a list of players who had chunks removed.
		for (Faction faction : oldFactions) {
			if (faction.isNone()) {
				continue;
			}
			
			List<Player> online = faction.getOnlinePlayers();
			
			if (online.isEmpty()) {
				continue;
			}
			
			Map<World, List<ProtectionRange>> rangeCache = new HashMap<>();
			for (Player player : online) {
				if (rangeGroup.isWDLPlayer(player)) {
					MPlayer mplayer = convertPlayerToMPlayer(player);
					if (!downloadPerm.has(mplayer, faction, false)) {
						continue;
					}
					
					if (rangeCache.containsKey(player.getWorld())) {
						rangeGroup.setRanges(player, rangeCache.get(player.getWorld()));
					} else {
						// Add all the old chunks and then filter out the now-removed chunks.
						Set<PS> chunks = new HashSet<>();
						chunks.addAll(getFactionPositions(player.getWorld(), faction));
						chunks.removeAll(oldFactionChunk.get(faction));
						
						List<ProtectionRange> ranges = convertPSToRanges(player.getWorld(), chunks, faction);
						rangeCache.put(player.getWorld(), ranges);
						rangeGroup.setRanges(player, ranges);
					}
				}
			}
		}
		
		//Similarly, we can assume that all new chunks are added to the faction.
		Set<PS> newChunks = e.getChunks();
		Faction newFaction = e.getNewFaction();
		List<Player> newFactionPlayers = newFaction.getOnlinePlayers();
		
		if (newFactionPlayers.isEmpty()) {
			return;
		}
		
		Map<World, List<ProtectionRange>> rangeCache = new HashMap<>();
		for (Player player : newFactionPlayers) {
			if (rangeGroup.isWDLPlayer(player)) {
				MPlayer mplayer = convertPlayerToMPlayer(player);
				if (!downloadPerm.has(mplayer, newFaction, false)) {
					continue;
				}
				
				if (rangeCache.containsKey(player.getWorld())) {
					rangeGroup.setRanges(player, rangeCache.get(player.getWorld()));
				} else {
					// Add all the old chunks and the new chunks
					Set<PS> chunks = new HashSet<>();
					chunks.addAll(getFactionPositions(player.getWorld(), newFaction));
					chunks.addAll(newChunks);
					
					List<ProtectionRange> ranges = convertPSToRanges(player.getWorld(), chunks, newFaction);
					rangeCache.put(player.getWorld(), ranges);
					rangeGroup.setRanges(player, ranges);
				}
			}
		}
	}
	
	/**
	 * Gets a set of all chunks owned by the given faction in the given world.
	 */
	public Set<PS> getFactionPositions(World world, Faction faction) {
		if (faction.isNone()) {
			// Don't send the billion ranges for Wilderness.
			return new HashSet<PS>();
		}
		
		//TODO: Squish this down to a single rectangle, if possible.
		Board board = Board.get(world.getName());
		
		Set<PS> positions = board.getChunks(faction);
		
		return positions;
	}
	
	/**
	 * Converts a Set of MassiveCraft {@link PS}s to {@link ProtectionRange}s.
	 */
	public List<ProtectionRange> convertPSToRanges(World world, Set<PS> positions, Faction owningFaction) {
		List<ProtectionRange> ranges = new ArrayList<>();
		
		for (PS position : positions) {
			if (!position.getWorld().equals(world.getName())) {
				continue;
			}
			int x = position.getChunkX();
			int z = position.getChunkZ();
			
			ranges.add(new ProtectionRange(owningFaction.getName(), x, z, x, z));
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
	}
}
