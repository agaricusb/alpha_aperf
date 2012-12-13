package ee.lutsu.alpha.mc.aperf.sys.entity;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

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
}
