package ee.lutsu.alpha.mc.aperf.sys.entity.limits;

import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ee.lutsu.alpha.mc.aperf.commands.CommandException;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntityHelper;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntitySafeListModule;
import ee.lutsu.alpha.mc.aperf.sys.objects.Filter;
import ee.lutsu.alpha.mc.aperf.sys.objects.SpawnLimit;

public class Count extends SpawnLimit
{
	public int limit;
	
	@Override
	protected void load(Map<String, String> args) throws Exception 
	{
		super.load(args);
		limit = getInt(args, "limit");
	}
	
	@Override
	protected void save(Map<String, String> args) 
	{
		super.save(args);
		args.put("limit", String.valueOf(limit));
	}
	
	@Override
	protected void getArguments(Map<String, String> list)
	{
		list.put("limit", "Integer. How many can spawn.");
		super.getArguments(list);
	}
	
	@Override
	public boolean canSpawn(Entity e, World world) 
	{
		int current = 0;
		if (type == Type.CountServer)
		{
			for (WorldServer w : MinecraftServer.getServer().worldServers)
				current += countEntitys(w.loadedEntityList);
		}
		else
		{
			current += countEntitys(world.loadedEntityList);
		}
		
		return current < limit;
	}
	
	protected int countEntitys(List entityList)
	{ 
		int cnt = 0;

		for (int i = 0; i < entityList.size(); i++)
		{
			Entity e = (Entity)entityList.get(i);
			String name = null;

			if (!(e instanceof EntityLiving) || e instanceof EntityPlayer || EntitySafeListModule.isEntitySafe(e))
				continue;
			
			if (!filter.hitsAll(e))
				continue;
			
			//Log.info("Hit count ++ on " + e.getClass().getSimpleName());
			cnt++;
		}
		
		return cnt;
	}
}
