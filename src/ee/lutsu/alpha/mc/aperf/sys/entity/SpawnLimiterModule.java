package ee.lutsu.alpha.mc.aperf.sys.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ee.lutsu.alpha.mc.aperf.Log;
import ee.lutsu.alpha.mc.aperf.aPerf;
import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterLimit.LimitFilterType;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterLimit.LimitRange;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterLimit.LimitType;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;

public class SpawnLimiterModule extends ModuleBase
{
	private SpawnLimiterEvents events;
	public List<SpawnLimiterLimit> limits = new ArrayList<SpawnLimiterLimit>();
	public static SpawnLimiterModule instance = new SpawnLimiterModule();
	
	public SpawnLimiterModule()
	{
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.entity.cmd.SpawnLimiterCmd());
	}
	
	@Override
	public String getName() { return "SpawnLimiter"; }
	
	protected void loadConfig()
	{
		Map<String, Property> props = aPerf.instance.config.categories.get("Entity-SpawnLimiter");
		if (props == null || props.size() < 1)
			return;
		
		limits.clear();
		for (Property prop : props.values())
		{
			limits.add(SpawnLimiterLimit.deserialize(prop.value));
		}
	}
	
	public void saveConfig()
	{
		ConfigCategory props = aPerf.instance.config.categories.get("Entity-SpawnLimiter");
		if (props == null)
			aPerf.instance.config.categories.put("Entity-SpawnLimiter", props = new ConfigCategory("Entity-SpawnLimiter"));

		props.clear();
		
		int i = 1;
		for (SpawnLimiterLimit limit : limits)
		{
			String name = String.format("Limit-%d", i++);
			Property prop = new Property();
			
			prop.value = limit.serialize();
			prop.setName(name);
			
			props.put(name, prop);
		}
		
		aPerf.instance.config.save();
	}

	@Override
	public void enable()
	{
		super.enable();
		
		loadConfig();
		events = new SpawnLimiterEvents(this);
		MinecraftForge.EVENT_BUS.register(events);
	}
	
	@Override
	public void disable()
	{
		super.disable();
		
		if (events != null)
		{
			MinecraftForge.EVENT_BUS.unregister(events);
			events = null;
		}
	}
	
	public boolean canEntitySpawn(EntityLiving ent, World server)
	{
		return findLimitsAndCheck(ent, server); // inner
	}
	
	protected boolean findLimitsAndCheck(EntityLiving ent, World server)
	{
		String clazz = ent.getClass().getSimpleName();
		String longClazz = ent.getClass().getName();
		String name = EntityHelper.getEntityName(ent);
		String group = EntityHelper.getEntityType(ent);
		
		for (SpawnLimiterLimit limit : limits)
		{
			if (!limit.active)
				continue;
			
			if (limit.type == LimitType.All) ;
			else if (limit.type == LimitType.Class && !limit.limitName.equalsIgnoreCase(clazz))
				continue;
			else if (limit.type == LimitType.LClass && !limit.limitName.equalsIgnoreCase(longClazz))
				continue;
			else if (limit.type == LimitType.Name && !limit.limitName.equalsIgnoreCase(name))
				continue;
			else if (limit.type == LimitType.Group && !limit.limitName.equalsIgnoreCase(group))
				continue;
			
			if (!limit.allDims && limit.dimension != server.provider.dimensionId)
				continue;

			if (limit.filterType == LimitFilterType.Count)
			{
				if (countForLimit(limit, ent, server) >= limit.limitInt) // inner
					return false;
			}
			else if (limit.filterType == LimitFilterType.ClearAbove)
			{
				if (!hasFreeRoomAbove(ent, server, limit.limitInt, limit.limitInt2, 0))
					return false;
			}
			else if (limit.filterType == LimitFilterType.WaterAbove)
			{
				if (!hasFreeRoomAbove(ent, server, limit.limitInt, limit.limitInt2, Block.waterStill.blockID))
					return false;
			}
			else if (limit.filterType == LimitFilterType.BlockBelow)
			{
				if (!isBlockTypeBelow(ent, server, limit.limitInt, limit.limitInt2))
					return false;
			}
		}
		
		return true;
	}
	
	protected boolean isBlockTypeBelow(EntityLiving ent, World server, int type, Integer subId)
	{
		int x = (int)ent.posX;
		int y = (int)ent.posY;
		int z = (int)ent.posZ;

		int i = server.getBlockId(x, y - 1, z);
		
		if (i != type)
			return false;
		else if (subId != null)
		{
			int s = server.getBlockMetadata(x, y - 1, z);
			return s == subId;
		}
		else
			return true;
	}
	
	protected boolean hasFreeRoomAbove(EntityLiving ent, World server, int cnt, Integer cnt2, int wantedType)
	{
		int x = (int)ent.posX;
		int y = (int)ent.posY;
		int z = (int)ent.posZ;

		for (int yadd = 0; yadd < cnt; yadd++)
		{
			if (y + yadd >= 255) // consider over the skyline as true for air
			{
				if (wantedType == 0)
					break;
				else
					return false;
			}
				
			
			int type = server.getBlockId(x, y + yadd, z);
			
			if (type != wantedType)
				return false;
		}
		if (cnt2 != null)
		{
			for (int yadd = cnt; yadd < cnt2; yadd++)
			{
				if (y + yadd >= 255) // consider over the skyline as false
				{
					if (wantedType == 0)
						break;
					else
						return true;
				}
				
				int type = server.getBlockId(x, y + yadd, z);
				
				if (type != wantedType)
					return true;
			}
			return false;
		}
		
		return true;
	}
	
	protected int countForLimit(SpawnLimiterLimit limit, EntityLiving ent, World server)
	{
		int current = 0;
		if (limit.range == LimitRange.Server)
		{
			for(WorldServer w : MinecraftServer.getServer().worldServers)
				current += countEntitys(limit, w.loadedEntityList); // inner
		}
		else if (limit.range == LimitRange.Map)
		{
			current += countEntitys(limit, server.loadedEntityList); // inner
		}
		else if (limit.range == LimitRange.Chunk)
		{
			int cx = (int)ent.posX >> 4;
			int cz = (int)ent.posZ >> 4;
			int range = limit.rangeExt > 0 ? limit.rangeExt : 0;
			IChunkProvider cp = server.getChunkProvider();
			
			//Log.info("Spawning " + ent.getClass().getSimpleName());
			for (int z = cz - range; z <= cz + range; z++)
			{
				for (int x = cx - range; x <= cx + range; x++)
				{
					Chunk chunk = cp.chunkExists(x, z) ? cp.provideChunk(x, z) : null;
					
					if (chunk != null && chunk.entityLists != null)
					{
						for(List subChunk : chunk.entityLists)
						{
							// mc? bug - chunk loaded entities aren't added to the world
							ArrayList<Entity> toRemove = new ArrayList<Entity>();
							for(Object o : subChunk)
							{
								if (!server.loadedEntityList.contains(o))
									toRemove.add((Entity)o);
							}
							
							for (Entity e : toRemove)
								chunk.removeEntity(e);
							
							current += countEntitys(limit, subChunk); // inner
						}
					}
				}
			}
		}
		
		return current;
	}
	
	protected int countEntitys(SpawnLimiterLimit limit, List entityList)
	{ 
		int cnt = 0;
		for (Object o : entityList)
		{
			Entity e = (Entity)o;
			String name = null;
			
			if (!(e instanceof EntityLiving) || e instanceof EntityPlayer)
				continue;
			
			if (limit.type == LimitType.All)
				name = null;
			else if (limit.type == LimitType.Class)
				name = e.getClass().getSimpleName();
			else if (limit.type == LimitType.LClass)
				name = e.getClass().getName();
			else if (limit.type == LimitType.Name)
				name = EntityHelper.getEntityName(e);
			else if (limit.type == LimitType.Group)
				name = EntityHelper.getEntityType(e);
			
			if (name != null && !limit.limitName.equalsIgnoreCase(name))
				continue;
			
			//Log.info("Hit count ++ on " + e.getClass().getSimpleName());
			cnt++;
		}
		
		return cnt;
	}
}
