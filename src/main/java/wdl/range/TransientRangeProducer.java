package wdl.range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Basic RangeProducer that allows for adding temporary ranges to a player.
 * 
 * This data is NOT stored anywhere and will disappear on a server reload.
 * 
 * TODO: This creates a lot of events - is that a bad thing?
 */
public class TransientRangeProducer implements IRangeProducer {
	private final IRangeGroup rangeGroup;
	/**
	 * Owning plugin.
	 */
	private final Plugin owner;
	/**
	 * Current ranges.
	 */
	private final transient Map<UUID, List<ProtectionRange>> ranges = new HashMap<>();
	
	/**
	 * Creates a new {@link TransientRangeProducer}.
	 * 
	 * @param rangeGroup The range group.
	 * @param owner The owning plugin - used to create events.
	 */
	public TransientRangeProducer(IRangeGroup rangeGroup, Plugin owner) {
		if (rangeGroup == null) {
			throw new IllegalArgumentException("rangeGroup must not be null!");
		}
		if (owner == null) {
			throw new IllegalArgumentException("owner (owning plugin) must not be null!");
		}
		this.rangeGroup = rangeGroup;
		this.owner = owner;
	}
	
	@Override
	public List<ProtectionRange> getInitialRanges(Player player) {
		if (ranges.containsKey(player.getUniqueId())) {
			return ranges.get(player.getUniqueId());
		} else {
			return new ArrayList<>();
		}
	}
	
	@Override
	public IRangeGroup getRangeGroup() {
		return rangeGroup;
	}
	
	/**
	 * Gives the given player download permission in the given ranges until
	 * the next server reload.
	 * 
	 * @param player The player to give the ranges to.
	 * @param ranges The ranges.
	 */
	public void addRanges(Player player, ProtectionRange... ranges) {
		rangeGroup.addRanges(player, ranges);
	}
	
	/**
	 * Gives the given player download permission in the given ranges until
	 * the next server reload.
	 * 
	 * @param player The player to give the ranges to.
	 * @param ranges The ranges.
	 */
	public void addRanges(Player player, List<ProtectionRange> ranges) {
		rangeGroup.addRanges(player, ranges);
	}
	
	/**
	 * Gives the given player download permission in the given ranges until
	 * the given number of ticks have elapsed.
	 * 
	 * @param player The player to give the ranges to.
	 * @param ranges The ranges.
	 */
	public void addRanges(Player player, int ticks, ProtectionRange... ranges) {
		rangeGroup.addRanges(player, ranges);
		
		// Queue later removal.
		RemoveExpiredRangesTask task = new RemoveExpiredRangesTask(player, ranges);
		task.runTaskLater(owner, ticks);
		activeRemovalTasks.add(task);
	}
	
	/**
	 * Gives the given player download permission in the given ranges until
	 * the given number of ticks have elapsed.
	 * 
	 * @param player The player to give the ranges to.
	 * @param ranges The ranges.
	 */
	public void addRanges(Player player, int ticks, List<ProtectionRange> ranges) {
		rangeGroup.addRanges(player, ranges);
		
		// Queue later removal.
		RemoveExpiredRangesTask task = new RemoveExpiredRangesTask(player, ranges);
		task.runTaskLater(owner, ticks);
		activeRemovalTasks.add(task);
	}
	
	@Override
	public void dispose() {
		for (RemoveExpiredRangesTask task : activeRemovalTasks) {
			try {
				task.cancel();
			} catch (Exception e) {
				owner.getLogger().log(Level.WARNING,
						"Failed to cancel task " + task + " while disposing!",
						e);
			}
		}
	}
	
	/**
	 * List of {@link RemoveExpiredRangesTask}s that are currently running.
	 */
	private List<RemoveExpiredRangesTask> activeRemovalTasks = new ArrayList<>();
	
	/**
	 * Task that removes all of the given ranges; can be used to remove ranges
	 * after a delay.
	 */
	private class RemoveExpiredRangesTask extends BukkitRunnable {
		private final UUID uuid;
		private final List<ProtectionRange> rangesToRemove;
		
		public RemoveExpiredRangesTask(Player player, ProtectionRange... ranges) {
			this.uuid = player.getUniqueId();
			this.rangesToRemove = Arrays.asList(ranges);
		}
		
		public RemoveExpiredRangesTask(Player player, List<ProtectionRange> ranges) {
			this.uuid = player.getUniqueId();
			this.rangesToRemove = ranges;
		}
		
		@Override
		public void run() {
			TransientRangeProducer.this.ranges.get(uuid).removeAll(
					rangesToRemove);
			// Remove this task from the list of active tasks
			activeRemovalTasks.remove(this);
		}
	}
}
