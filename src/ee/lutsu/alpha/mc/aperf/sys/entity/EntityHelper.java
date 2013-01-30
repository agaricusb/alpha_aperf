package ee.lutsu.alpha.mc.aperf.sys.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ee.lutsu.alpha.mc.aperf.sys.objects.EntityClassMapEntry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.INpc;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.world.chunk.Chunk;

public class EntityHelper 
{
	private static Map<Class, String> typeCache = new HashMap<Class, String>();
	
	public static void removeEntity(Entity e)
	{
		e.worldObj.removeEntity(e);
	}
	
	public static List<Class> getAllInterfaces(Class cls)
	{
		List<Class> lst = new ArrayList<Class>();
		
		lst.addAll(Arrays.asList(cls.getInterfaces()));
		
		Class s = cls.getSuperclass();
		if (s != null)
			lst.addAll(getAllInterfaces(s));
		
		return lst;
	}
	
	public static String getEntityName(Entity e)
	{
		String n = e.getEntityName();
		if (n.startsWith("entity.") && n.endsWith(".name")) // no translation
			return EntityList.getEntityString(e);
		else
			return n;
	}
	
	public static String getEntityClass(Entity e)
	{
		if (e.getClass().getPackage() != null)
			return e.getClass().getSimpleName();
		
		EntityClassMapEntry<Class<? extends Entity>, String, String> n = EntityClassMap.instance.get(e.getClass());
		if (n == null)
			return e.getClass().getSimpleName();
		
		return n.value;
	}
	
	public static String getEntityLClass(Entity e)
	{
		if (e.getClass().getPackage() != null)
			return e.getClass().getName();
		
		EntityClassMapEntry<Class<? extends Entity>, String, String> n = EntityClassMap.instance.get(e.getClass());
		if (n == null)
			return e.getClass().getName();
		
		return n.mid + "." + n.value;
	}

	public static String getEntityType(Entity e)
	{
		String n = typeCache.get(e.getClass());
		if (n != null)
			return n;
		
		if (e instanceof EntityGolem) // also a IAnimal
			n = "Golem";
		
		if (n == null)
		{
			for (Class t : getAllInterfaces(e.getClass()))
			{
				if (t.equals(IBossDisplayData.class))
					n = "Boss";
				else if (t.equals(IAnimals.class))
					n = "Animal";
				else if (t.equals(IMob.class))
					n = "Monster";
				else if (t.equals(IProjectile.class))
					n = "Projectile";
				else if (t.equals(INpc.class))
					n = "NPC";
				
				if (n != null)
					break;
			}
		}
		
		if (n == null)
		{
			if (e instanceof EntityItem)
				n = "Item";
			else if (e instanceof EntityMob)
				n = "Monster";
			else if (e instanceof EntityPlayer)
				n = "Player";
			else if (e instanceof EntityFireball)
				n = "Projectile";
			else if (e instanceof EntityTNTPrimed)
				n = "TNT";
			else
				n = "Unknown"; //e.getClass().getName();
		}
		
		typeCache.put(e.getClass(), n);
		return n;
	}
	
	public static Long getChunkHash(int dim, int x, int z)
	{
		return ((long)dim << 32) | (x << 16) | z;
	}
	
	public static Long getChunkHash(Chunk c)
	{
		return getChunkHash(c.worldObj.provider.dimensionId, c.xPosition, c.zPosition);
	}
}
