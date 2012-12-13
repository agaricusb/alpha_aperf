package ee.lutsu.alpha.mc.aperf.sys.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.registry.TickRegistry;

import ee.lutsu.alpha.mc.aperf.aPerf;
import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterLimit.LimitRange;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterLimit.LimitType;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Chunk;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityXPOrb;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.Item;
import net.minecraft.src.MathHelper;
import net.minecraft.src.World;
import net.minecraft.src.WorldServer;
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
	
	protected void loadConfig()
	{

		groupItems = Boolean.valueOf(aPerf.instance.config.get("Entity-ItemGrouper", "GroupItems", Boolean.toString(groupItems)).value);
		groupExpOrbs = Boolean.valueOf(aPerf.instance.config.get("Entity-ItemGrouper", "GroupExpOrbs", Boolean.toString(groupExpOrbs)).value);
		
		matchRange = Double.valueOf(aPerf.instance.config.get("Entity-ItemGrouper", "MatchRange", Double.toString(matchRange)).value);
		moveToNewLocation = Boolean.valueOf(aPerf.instance.config.get("Entity-ItemGrouper", "MoveToNewLocation", Boolean.toString(moveToNewLocation)).value);

		livedAtleast = Integer.valueOf(aPerf.instance.config.get("Entity-ItemGrouper", "LivedAtleast", Integer.toString(livedAtleast)).value);
		skipForTicks = Integer.valueOf(aPerf.instance.config.get("Entity-ItemGrouper", "SkipForTicks", Integer.toString(skipForTicks)).value);
	}
	
	public void saveConfig()
	{
		aPerf.instance.config.get("Entity-ItemGrouper", "GroupItems", Boolean.toString(groupItems)).value = Boolean.toString(groupItems);
		aPerf.instance.config.get("Entity-ItemGrouper", "GroupExpOrbs", Boolean.toString(groupExpOrbs)).value = Boolean.toString(groupExpOrbs);
		
		aPerf.instance.config.get("Entity-ItemGrouper", "MatchRange", Double.toString(matchRange)).value = Double.toString(matchRange);
		aPerf.instance.config.get("Entity-ItemGrouper", "MoveToNewLocation", Boolean.toString(moveToNewLocation)).value = Boolean.toString(moveToNewLocation);

		aPerf.instance.config.get("Entity-ItemGrouper", "LivedAtleast", Integer.toString(livedAtleast)).value = Integer.toString(livedAtleast);
		aPerf.instance.config.get("Entity-ItemGrouper", "SkipForTicks", Integer.toString(skipForTicks)).value = Integer.toString(skipForTicks);
		
		aPerf.instance.config.save();
	}
	
	public boolean groupExpOrb(EntityXPOrb item, World world)
	{
		if (!this.enabled)
			return false;
		
		if (item.isDead || item.xpOrbAge < livedAtleast)
			return false;

		List entities = world.getEntitiesWithinAABB(EntityXPOrb.class, item.boundingBox.expand(matchRange, matchRange, matchRange));
		for(Object o : entities)
		{
			EntityXPOrb e = (EntityXPOrb)o;
			if (e.isDead || e == item)
				continue;
			
			if (e.xpOrbAge < livedAtleast)
				continue;
			
			try
			{
				Field xpField = EntityXPOrb.class.getDeclaredField("xpValue");
				xpField.setAccessible(true);
				
				xpField.set(item, item.getXpValue() + e.getXpValue());
			}
			catch(Exception er)
			{
				throw new RuntimeException(er);
			}
			
			item.xpOrbAge = Math.min(item.xpOrbAge, e.xpOrbAge);
			e.setDead();

			if (moveToNewLocation)
			{
				item.setPosition(item.posX + (e.posX - item.posX) / 2, 
						item.posY + (e.posY - item.posY) / 2, 
						item.posZ + (e.posZ - item.posZ) / 2);
				item.velocityChanged = true;
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean groupItem(EntityItem item, World world)
	{
		if (!this.enabled)
			return false;
		
		if (item.item == null || !item.item.isStackable() || item.isDead || item.age < livedAtleast)
			return false;

		List entities = world.getEntitiesWithinAABB(EntityItem.class, item.boundingBox.expand(matchRange, matchRange, matchRange));
		for(Object o : entities)
		{
			EntityItem e = (EntityItem)o;
			if (e.isDead || e == item)
				continue;
			
			if (e.age < livedAtleast || !item.func_70289_a(e))
				continue;
			
			EntityItem ret = item.isDead ? e : item;

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
