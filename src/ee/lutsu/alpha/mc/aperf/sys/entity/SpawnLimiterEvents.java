package ee.lutsu.alpha.mc.aperf.sys.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.ChunkEvent;

public class SpawnLimiterEvents 
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
		
		if (!parent.canEntitySpawn((EntityLiving)ev.entity, ev.world))
			ev.setCanceled(true);
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
}
