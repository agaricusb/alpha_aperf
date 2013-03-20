package ee.lutsu.alpha.mc.aperf.sys.entity;

import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Property;
import ee.lutsu.alpha.mc.aperf.aPerf;
import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;
import ee.lutsu.alpha.mc.aperf.sys.objects.Filter;
import ee.lutsu.alpha.mc.aperf.sys.objects.FilterCollection;

public class EntitySafeListModule extends ModuleBase
{
	public static EntitySafeListModule instance = new EntitySafeListModule();
	public FilterCollection safeList = new FilterCollection();
	
	@Override
	public String getName() { return "EntitySafeList"; }
	@Override
	public boolean getDefaultEnabled() { return true; }
	
	public EntitySafeListModule()
	{
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.entity.cmd.EntitySafeList());
	}
	
	public static boolean isEntitySafe(Entity e)
	{
		return instance.enabled && instance.safeList.hitsAny(e);
	}
	
	@Override
	public void loadConfig()
	{
		if (!aPerf.instance.config.hasCategory("Entity-SafeList"))
		{
			populateDefault();
			saveConfig();
		}
		else
		{
			ConfigCategory props = aPerf.instance.config.getCategory("Entity-SafeList");
			if (props.values().size() < 1)
				return;
			
			try
			{
				safeList.clear();
				for (Property prop : props.values())
					safeList.add(new Filter(prop.getString()));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public void saveConfig()
	{
		ConfigCategory props = aPerf.instance.config.getCategory("Entity-SafeList");
		props.clear();
		
		int i = 1;
		for (Filter filter : safeList)
		{
			String name = String.format("Filter-%d", i++);
			Property prop = new Property();
			
			prop.set(filter.serialize());
			prop.setName(name);
			
			props.put(name, prop);
		}
		
		aPerf.instance.config.save();
	}
	
	public void populateDefault()
	{
		safeList.clear();
		
		try
		{
			safeList.add(new Filter("g:NPC"));
			safeList.add(new Filter("g:Golem"));
			safeList.add(new Filter("g:Boss"));
			safeList.add(new Filter("i:net.minecraft.entity.item.EntityBoat"));
			safeList.add(new Filter("i:net.minecraft.entity.item.EntityMinecart"));
			safeList.add(new Filter("i:net.minecraft.entity.player.EntityPlayer"));
		}
		catch (Exception e)
		{
			throw new RuntimeException("Default entity-safe-list population error. This is a programming error.", e);
		}
	}

	@Override
	public void enable()
	{
		super.enable();
		loadConfig();
	}
}
