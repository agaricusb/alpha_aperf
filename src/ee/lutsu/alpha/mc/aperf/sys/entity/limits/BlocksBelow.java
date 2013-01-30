package ee.lutsu.alpha.mc.aperf.sys.entity.limits;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.base.Joiner;

import net.minecraft.world.World;

public class BlocksBelow extends VerticalBlockComparer
{
	public ArrayList<int[]> blocks = new ArrayList<int[]>();
	
	public BlocksBelow()
	{
		upwards = false;
		isVoidThisType = false;
	}
	
	@Override
	protected void load(Map<String, String> args) throws Exception 
	{
		super.load(args);
		count = getInt(args, "count", 1);
		max = getInt(args, "max", 0);
		
		String s = getString(args, "blocks");
		for (String b : s.split("/"))
		{
			String[] p = b.split("\\.");
			
			int id = Integer.parseInt(p[0]);
			int sub = p.length > 1 ? Integer.parseInt(p[1]) : -1;
			
			blocks.add(new int[] { id, sub });
		}
	}
	
	@Override
	protected void save(Map<String, String> args) 
	{
		super.save(args);
		if (count != 1) args.put("count", String.valueOf(count));
		if (max > 0) args.put("max", String.valueOf(max));
		
		ArrayList<String> arr = new ArrayList<String>();
		for (int[] b : blocks)
		{
			arr.add(String.valueOf(b[0]) + (b[1] >= 0 ? "." + String.valueOf(b[1]) : ""));
		}
		args.put("blocks", Joiner.on("/").join(arr));
	}
	
	@Override
	protected void getArguments(Map<String, String> list)
	{
		list.put("blocks", "Block id list. <id>[.<sub>][/<id>[.<sub>]] Blocks on which the mob can spawn.");
		list.put("count?", "Integer. How many blocks have to be one of the specified types below the mob feet. Defaults to 1.");
		list.put("max?", "Integer. How many of these types is the maximum. Defaults to 0.");
		
		super.getArguments(list);
	}
	
	@Override
	protected boolean isBlockCorrect(World world, int x, int y, int z)
	{
		int type = world.getBlockId(x, y, z);
		int sub = world.getBlockMetadata(x, y, z);
		
		for (int[] block : blocks)
		{
			if (block[0] != type)
				continue;
			
			if (block[1] >= 0 && block[1] != sub)
				continue;
			
			return true;
		}
		
		return false;
	}
}
