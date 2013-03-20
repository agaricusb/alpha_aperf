package ee.lutsu.alpha.mc.aperf.sys.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

import ee.lutsu.alpha.mc.aperf.aPerf;
import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;

public class ItemGrouperModule extends ModuleBase
{
	public static ItemGrouperModule instance = new ItemGrouperModule();
	
	private ItemGrouperEvents events;
	
	public boolean groupItems = true;
	public boolean groupExpOrbs = true;
	public double matchRange = 1.5;
	public boolean moveToNewLocation = true;
	public int livedAtleast = 40; // ticks
	public int skipForTicks = 20;
	
	public ItemGrouperModule()
	{
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.entity.cmd.ItemGrouperCmd());
	}
	
	@Override
	public String getName() { return "ItemGrouper"; }
	
	public void enable()
	{
		super.enable();
		
		loadConfig();
		if (events == null)
		{
			events = new ItemGrouperEvents(this);
			TickRegistry.registerTickHandler(events, Side.SERVER);
		}
		//MinecraftForge.EVENT_BUS.register(events);
	}
	
	public void disable()
	{
		super.disable();
		
		if (events != null) // don't unregister because we can't
		{
			//MinecraftForge.EVENT_BUS.unregister(events);
			//events = null;
		}
	}
	
	@Override
	public void loadConfig()
	{
		groupItems = aPerf.instance.config.get("Entity-ItemGrouper", "GroupItems", groupItems).getBoolean(groupItems);
		groupExpOrbs = aPerf.instance.config.get("Entity-ItemGrouper", "GroupExpOrbs", groupExpOrbs).getBoolean(groupExpOrbs);
		
		matchRange = aPerf.instance.config.get("Entity-ItemGrouper", "MatchRange", matchRange).getDouble(matchRange);
		moveToNewLocation = aPerf.instance.config.get("Entity-ItemGrouper", "MoveToNewLocation", moveToNewLocation).getBoolean(moveToNewLocation);

		livedAtleast = aPerf.instance.config.get("Entity-ItemGrouper", "LivedAtleast", livedAtleast).getInt(livedAtleast);
		skipForTicks = aPerf.instance.config.get("Entity-ItemGrouper", "SkipForTicks", skipForTicks).getInt(skipForTicks);
	}
	
	public void saveConfig()
	{
		aPerf.instance.config.get("Entity-ItemGrouper", "GroupItems", groupItems).set(groupItems);
		aPerf.instance.config.get("Entity-ItemGrouper", "GroupExpOrbs", groupExpOrbs).set(groupExpOrbs);
		
		aPerf.instance.config.get("Entity-ItemGrouper", "MatchRange", matchRange).set(matchRange);
		aPerf.instance.config.get("Entity-ItemGrouper", "MoveToNewLocation", moveToNewLocation).set(moveToNewLocation);

		aPerf.instance.config.get("Entity-ItemGrouper", "LivedAtleast", livedAtleast).set(livedAtleast);
		aPerf.instance.config.get("Entity-ItemGrouper", "SkipForTicks", skipForTicks).set(skipForTicks);
		
		aPerf.instance.config.save();
	}
	
	public boolean groupExpOrb(EntityXPOrb item, World world, ArrayList<Entity> toAdd, ArrayList<Entity> toRemove)
	{
		if (!this.enabled)
			return false;
		
		if (item.isDead || item.xpOrbAge < livedAtleast)
			return false;

		List entities = world.getEntitiesWithinAABB(EntityXPOrb.class, item.boundingBox.expand(matchRange, matchRange, matchRange));
		if (entities.size() < 2)
			return false; // only self
		
		List<EntityXPOrb> close = new ArrayList<EntityXPOrb>();
		for(Object o : entities)
		{
			EntityXPOrb e = (EntityXPOrb)o;
			if (e.isDead || e.xpOrbAge < livedAtleast)
				continue;
			
			close.add(e);
		}
		
		if (close.size() < 2)
			return false; // none valid, contains self
		
		double x = 0, y = 0, z = 0;
		int val = 0, cval = 0, age = Integer.MAX_VALUE;
		
		for (EntityXPOrb orb : close)
		{
			cval = orb.getXpValue();
			if (val >= Integer.MAX_VALUE - cval) // don't group at all if we run over max integer
				return false;
			
			val += cval;
			age = Math.min(orb.xpOrbAge, age);
			
			if (moveToNewLocation)
			{
				x += orb.posX;
				y += orb.posY;
				z += orb.posZ;
			}
		}

		if (!moveToNewLocation)
		{
			x = item.posX;
			y = item.posY;
			z = item.posZ;
		}
		else
		{
			x /= close.size();
			y /= close.size();
			z /= close.size();
		}
		
		EntityXPOrb nOrb = new EntityXPOrb(world, x, y, z, val);
		nOrb.xpOrbAge = age;
		
		toAdd.add(nOrb);
		for (EntityXPOrb orb : close) // remove all old ones
		{
			orb.setDead();
			toRemove.add(orb);
		}
		
		return true;
	}
	
	public boolean groupItem(EntityItem item, World world, ArrayList<Entity> toAdd, ArrayList<Entity> toRemove)
	{
		if (!this.enabled)
			return false;
		
		if (item.getEntityItem() == null || !item.getEntityItem().isStackable() || item.isDead || item.age < livedAtleast)
			return false;

		List entities = world.getEntitiesWithinAABB(EntityItem.class, item.boundingBox.expand(matchRange, matchRange, matchRange));
		for(Object o : entities)
		{
			EntityItem e = (EntityItem)o;
			if (e.isDead || e == item || e.age < livedAtleast)
				continue;
			
			if (!item.combineItems(e))
				continue; // can't put together
			
			EntityItem ret = item.isDead ? e : item;
			toRemove.add(item.isDead ? item : e);

			if (moveToNewLocation)
			{
				ret.setPosition(item.posX + (e.posX - item.posX) / 2, 
						item.posY + (e.posY - item.posY) / 2, 
						item.posZ + (e.posZ - item.posZ) / 2);
				ret.velocityChanged = true;
			}
			
			return true;
		}
		
		return false;
	}
}
