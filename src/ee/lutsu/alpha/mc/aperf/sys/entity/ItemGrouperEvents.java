package ee.lutsu.alpha.mc.aperf.sys.entity;

import java.util.ArrayList;
import java.util.EnumSet;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityXPOrb;
import net.minecraft.src.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class ItemGrouperEvents implements ITickHandler
{
	ItemGrouperModule parent;
	int ticks = 0;
	
	public ItemGrouperEvents(ItemGrouperModule parent)
	{
		this.parent = parent;
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if (!parent.isEnabled())
			return;
		
		if (ticks > 0)
		{
			ticks--;
			return;
		}
		
		World w = (World)tickData[0];
		ArrayList<Entity> toRemove = new ArrayList<Entity>();
		ArrayList<Entity> toAdd = new ArrayList<Entity>();
		
		for (Object o : w.loadedEntityList)
		{
			if (parent.groupItems && o instanceof EntityItem)
			{
				parent.groupItem((EntityItem)o, w, toAdd, toRemove);
			}
			else if (parent.groupExpOrbs && o instanceof EntityXPOrb)
			{
				parent.groupExpOrb((EntityXPOrb)o, w, toAdd, toRemove);
			}
		}
		
		for (Entity e : toRemove)
			EntityHelper.removeEntity(e);
		for (Entity e : toAdd)
			w.spawnEntityInWorld(e);
		
		ticks = MinecraftServer.getServer().worldServers.length * parent.skipForTicks;
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() 
	{
		return "aPerf ItemGrouper Module event handler";
	}
}
