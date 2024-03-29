package ee.lutsu.alpha.mc.aperf.sys.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cpw.mods.fml.common.registry.GameRegistry;

import ee.lutsu.alpha.mc.aperf.Deobfuscator;
import ee.lutsu.alpha.mc.aperf.Log;
import ee.lutsu.alpha.mc.aperf.aPerf;
import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;
import ee.lutsu.alpha.mc.aperf.sys.objects.Filter;
import ee.lutsu.alpha.mc.aperf.sys.objects.SpawnLimit;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;

public class SpawnLimiterModule extends ModuleBase
{
	private SpawnLimiterEvents events;
	private Map<Integer, EntityLiving> lastNormalSpawn = new HashMap<Integer, EntityLiving>();
	
	public List<SpawnLimit> limits = new ArrayList<SpawnLimit>();
	public static SpawnLimiterModule instance = new SpawnLimiterModule();
	public List<Long> fullyLoadedChunks = new ArrayList<Long>(); // managed by events
	
	public boolean executeRulesOnChunkLoad = true;
	public boolean executeRulesOnNormalSpawning = true;
	public boolean exetuteRulesOnOtherwiseSpawned = true;
	
	public Map<ICommandSender, Filter> eventLoggers = new HashMap<ICommandSender, Filter>();
	
	public SpawnLimiterModule()
	{
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.entity.cmd.SpawnLimiterCmd());
	}
	
	@Override
	public String getName() { return "SpawnLimiter"; }
	
	@Override
	public void loadConfig()
	{
		executeRulesOnChunkLoad = aPerf.instance.config.get("Entity-SpawnLimiter", "ExecuteRulesOnChunkLoad", false).getBoolean(false);
		executeRulesOnNormalSpawning = aPerf.instance.config.get("Entity-SpawnLimiter", "ExecuteRulesOnNormalSpawning", true).getBoolean(true);
		exetuteRulesOnOtherwiseSpawned = aPerf.instance.config.get("Entity-SpawnLimiter", "ExecuteRulesOnOtherwiseSpawned", false).getBoolean(false);
		
		limits.clear();
		
		ConfigCategory props = aPerf.instance.config.getCategory("Entity-SpawnLimiter.Limits");
		if (props.values().size() < 1)
			return;

		try
		{
			for (Property prop : props.values())
			{
				limits.add(SpawnLimit.deserialize(prop.getString()));
			}
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Failed to load config", ex);
		}
	}
	
	public void saveConfig()
	{
		aPerf.instance.config.get("Entity-SpawnLimiter", "ExecuteRulesOnChunkLoad", executeRulesOnChunkLoad).set(executeRulesOnChunkLoad);
		aPerf.instance.config.get("Entity-SpawnLimiter", "ExecuteRulesOnNormalSpawning", executeRulesOnNormalSpawning).set(executeRulesOnNormalSpawning);
		aPerf.instance.config.get("Entity-SpawnLimiter", "ExecuteRulesOnOtherwiseSpawned", exetuteRulesOnOtherwiseSpawned).set(exetuteRulesOnOtherwiseSpawned);
		
		ConfigCategory props = aPerf.instance.config.getCategory("Entity-SpawnLimiter.Limits");

		props.clear();
		
		int i = 1;
		for (SpawnLimit limit : limits)
		{
			String name = String.format("Limit-%d", i++);
			Property prop = new Property();
			
			prop.set(limit.serialize());
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
		GameRegistry.registerPlayerTracker(events);
		
		indexLoadedChunks();
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
		
		synchronized (fullyLoadedChunks)
		{
			fullyLoadedChunks.clear();
		}
	}
	
	private void indexLoadedChunks()
	{
		try
		{
			synchronized (fullyLoadedChunks)
			{
				fullyLoadedChunks.clear();
				
				Field f = ChunkProviderServer.class.getDeclaredField(Deobfuscator.getFieldName(ChunkProviderServer.class, "loadedChunks"));
				f.setAccessible(true); // private List loadedChunks = new ArrayList();
				
				// Server is null at server startup
				if (MinecraftServer.getServer() == null || MinecraftServer.getServer().worldServers == null)
					return;
				
				for (WorldServer w : MinecraftServer.getServer().worldServers)
				{
					 List chunks = (List)f.get(w.theChunkProviderServer);
					 
					 for (Object c : chunks)
					 {
						 fullyLoadedChunks.add(EntityHelper.getChunkHash((Chunk)c));
						 
						 checkChunkEntities((Chunk)c);
					 }
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Loaded chunk indexer failed", e);
		}
	}
	
	public boolean isChunkFullyLoaded(Long chunkHash)
	{
		synchronized (fullyLoadedChunks)
		{
			return fullyLoadedChunks.contains(chunkHash);
		}
	}
	
	public void checkChunkEntities(Chunk c)
	{
		if (!enabled || !executeRulesOnChunkLoad)
			return;
		
		for (int ci = 0; ci < c.entityLists.length; ci++)
		{
			if (c.entityLists[ci] == null)
				continue;
			
			for (int i = c.entityLists[ci].size() - 1; i >= 0; i--)
			{
				Object o = c.entityLists[ci].get(i);
				if (o == null || !(o instanceof EntityLiving) || o instanceof EntityPlayer)
					continue;

				EntityLiving e = (EntityLiving)o;
				
				if (!canEntitySpawn(e, c.worldObj))
				{
					sendLog(e, "§f[§aaPerf§f][§aload§f] %s §f%s", "§4blocked", e);
					EntityHelper.removeEntity(e);
				}
				else
					sendLog(e, "§f[§aaPerf§f][§aload§f] %s §f%s", "§2allowed", e);
			}
		}
	}
	
	public boolean canEntitySpawnNaturally(EntityLiving ent, World server)
	{
		lastNormalSpawn.put(server.provider.dimensionId, ent);

		boolean result = !executeRulesOnNormalSpawning || canEntitySpawn(ent, server);
		
		if (executeRulesOnNormalSpawning)
			sendLog(ent, "§f[§aaPerf§f][§anormal§f] %s §f%s", result ? "§2allowed" : "§4blocked", ent);
		
		return result;
	}
	
	public boolean canEntityJoinWorld(EntityLiving ent, World server)
	{
		boolean normalSpawn = lastNormalSpawn.get(server.provider.dimensionId) == ent;
		
		// already checked
		if (normalSpawn)
			return true;
		
		boolean result = !exetuteRulesOnOtherwiseSpawned || canEntitySpawn(ent, server);
		
		if (exetuteRulesOnOtherwiseSpawned)
			sendLog(ent, "§f[§aaPerf§f][§aother§f] %s §f%s", result ? "§2allowed" : "§4blocked", ent);
		
		return result;
	}
	
	private void sendLog(Entity e, String format, Object ... args)
	{
		if (eventLoggers.size() < 1)
			return;
		
		String msg = String.format(format, args);
		
		synchronized (eventLoggers)
		{
			for (Entry<ICommandSender, Filter> kv : eventLoggers.entrySet())
			{
				if (kv.getValue() == null || kv.getValue().hitsAll(e))
					kv.getKey().sendChatToPlayer(msg);
			}
		}
	}
	
	public boolean canEntitySpawn(EntityLiving ent, World server)
	{
		if (!enabled)
			return true;
		
        int x = MathHelper.floor_double(ent.posX / 16.0D);
        int z = MathHelper.floor_double(ent.posZ / 16.0D);
        
        if (isChunkFullyLoaded(EntityHelper.getChunkHash(server.provider.dimensionId, x, z)) && !EntitySafeListModule.isEntitySafe(ent))
        	return findLimitsAndCheck(ent, server); // inner
        
        return true;
	}
	
	protected boolean findLimitsAndCheck(EntityLiving ent, World server)
	{
		for (SpawnLimit limit : limits)
		{
			if (!limit.on)
				continue;
			
			if (!limit.filter.hitsAll(ent))
				continue;
			
			if (!limit.canSpawn(ent, server))
				return false;
		}
		
		return true;
	}
}
