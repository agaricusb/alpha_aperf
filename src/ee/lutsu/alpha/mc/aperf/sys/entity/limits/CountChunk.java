package ee.lutsu.alpha.mc.aperf.sys.entity.limits;

import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import ee.lutsu.alpha.mc.aperf.commands.CommandException;
import ee.lutsu.alpha.mc.aperf.sys.entity.limits.Count;
import ee.lutsu.alpha.mc.aperf.sys.objects.SpawnLimit.Type;

public class CountChunk extends Count
{
	public int radius;
	
	@Override
	protected void load(Map<String, String> args) throws Exception 
	{
		super.load(args);
		radius = getInt(args, "radius", 0);
	}
	
	@Override
	protected void save(Map<String, String> args) 
	{
		super.save(args);
		if (radius > 0) args.put("radius", String.valueOf(radius));
	}
	
	@Override
	protected void getArguments(Map<String, String> list)
	{
		list.put("radius?", "Integer. Count chunks around this chunk aswell. Radius of 1 means a 3x3 chunk area. Defaults to 0.");
		super.getArguments(list);
	}
	
	@Override
	public boolean canSpawn(Entity e, World world) 
	{
		int current = 0;
		int cx = (int)e.posX >> 4;
		int cz = (int)e.posZ >> 4;
		int range = radius > 0 ? radius : 0;
		IChunkProvider cp = world.getChunkProvider();

		for (int z = cz - range; z <= cz + range; z++)
		{
			for (int x = cx - range; x <= cx + range; x++)
			{
				Chunk chunk = cp.chunkExists(x, z) ? cp.provideChunk(x, z) : null;
				
				if (chunk != null && chunk.entityLists != null)
				{
					for (int i = 0; i < chunk.entityLists.length; i++)
					{
						current += countEntitys(chunk.entityLists[i]);
					}
				}
			}
		}
		
		return current < limit;
	}
}
