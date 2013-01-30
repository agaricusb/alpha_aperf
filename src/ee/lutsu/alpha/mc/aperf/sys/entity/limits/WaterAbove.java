package ee.lutsu.alpha.mc.aperf.sys.entity.limits;

import java.util.Map;

import net.minecraft.block.Block;

public class WaterAbove extends VerticalBlockComparer
{
	public WaterAbove()
	{
		upwards = true;
		blockToFind = Block.waterStill.blockID;
	}
	
	@Override
	protected void load(Map<String, String> args) throws Exception 
	{
		super.load(args);
		count = getInt(args, "count");
		max = getInt(args, "max", 0);
	}
	
	@Override
	protected void save(Map<String, String> args) 
	{
		super.save(args);
		args.put("count", String.valueOf(count));
		if (max > 0) args.put("max", String.valueOf(max));
	}
	
	@Override
	protected void getArguments(Map<String, String> list)
	{
		list.put("count", "Integer. How many blocks has to be water from the mob.");
		list.put("max?", "Integer. How many blocks water above the required is the maximum. Used for surface fish.");
		super.getArguments(list);
	}
}
