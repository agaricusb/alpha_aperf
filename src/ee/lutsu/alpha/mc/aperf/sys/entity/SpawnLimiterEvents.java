package ee.lutsu.alpha.mc.aperf.sys.entity;

import cpw.mods.fml.common.IPlayerTracker;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.ChunkEvent;

public class SpawnLimiterEvents implements IPlayerTracker
{
	SpawnLimiterModule parent;
	
	public SpawnLimiterEvents(SpawnLimiterModule parent)
	{
		this.parent = parent;
	}
	
	@ForgeSubscribe
	public void entityJoinWorld(EntityJoinWorldEvent ev)
	{
		if (!(ev.entity instanceof EntityLiving) || ev.entity instanceof EntityPlayer)
			return;
		
		if (!parent.canEntityJoinWorld((EntityLiving)ev.entity, ev.world))
			ev.setCanceled(true);
	}
	
	@ForgeSubscribe
	public void livingSpawnEventCheckSpawn(LivingSpawnEvent.CheckSpawn ev)
	{
		if (!parent.canEntitySpawnNaturally(ev.entityLiving, ev.world))
			ev.setResult(Result.DENY);
	}
	
	@ForgeSubscribe
	public void chunkLoaded(ChunkEvent.Load ev)
	{
		synchronized (parent.fullyLoadedChunks)
		{
			parent.fullyLoadedChunks.add(EntityHelper.getChunkHash(ev.getChunk()));
		}
		parent.checkChunkEntities(ev.getChunk());
	}
	
	@ForgeSubscribe
	public void chunkUnloaded(ChunkEvent.Unload ev)
	{
		synchronized (parent.fullyLoadedChunks)
		{
			parent.fullyLoadedChunks.remove(EntityHelper.getChunkHash(ev.getChunk()));
		}
	}

	@Override
	public void onPlayerLogin(EntityPlayer player) 
	{
	}

	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
		SpawnLimiterModule.instance.eventLoggers.remove(player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player)
	{
	}
}
