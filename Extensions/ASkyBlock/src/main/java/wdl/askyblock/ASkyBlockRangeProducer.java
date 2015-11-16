package wdl.askyblock;

import java.util.List;

import org.bukkit.entity.Player;

import wdl.range.IRangeGroup;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

public class ASkyBlockRangeProducer implements IRangeProducer {
	private final IRangeGroup group;
	
	public ASkyBlockRangeProducer(IRangeGroup group) {
		this.group = group;
	}
	
	@Override
	public List<ProtectionRange> getInitialRanges(Player player) {
		return null;
	}

	@Override
	public IRangeGroup getRangeGroup() {
		return group;
	}
}
