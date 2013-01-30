package ee.lutsu.alpha.mc.aperf.sys.entity.limits;

import java.util.Map;

public class ClearAbove extends VerticalBlockComparer
{
	public ClearAbove()
	{
		upwards = true;
		blockToFind = 0;
		isSkyThisType = true;
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
		list.put("count", "Integer. How many blocks have to be free from the mob feet.");
		list.put("max?", "Integer. How many blocks free room above the required is the maximum. Used for cave mobs.");
		super.getArguments(list);
	}
}
