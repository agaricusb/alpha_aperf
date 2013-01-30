package ee.lutsu.alpha.mc.aperf.sys.objects;

import java.util.ArrayList;

import com.google.common.base.Joiner;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntityHelper;
import ee.lutsu.alpha.mc.aperf.sys.tile.TileEntityHelper;

public class Filter extends ArrayList<SubFilter>
{
	// serialization
	public Filter(String filter) throws Exception
	{
		String[] sp = filter.split(",");
		for (String v : sp)
		{
			add(new SubFilter(v.trim()));
		}
	}
	
	public String serialize()
	{
		ArrayList<String> values = new ArrayList<String>();
		for (SubFilter f : this)
			values.add(f.serialize());
		
		return Joiner.on(",").join(values);
	}
	
	public String serializeDisplay()
	{
		ArrayList<String> values = new ArrayList<String>();
		for (SubFilter f : this)
			values.add(f.serializeDisplay());
		
		return Joiner.on(", ").join(values);
	}
	
	// checks - all
	public boolean hitsAll(Entity e)
	{
		return hits(e, true);
	}
	
	public boolean hitsAll(TileEntity t)
	{
		return hits(t, true);
	}
	
	public boolean hitsAll(int hash, String name, String group, Class c, String clas, String lclas, int dim, int x, int y, int z)
	{
		return hits(hash, name, group, c, clas, lclas, dim, x, y, z, true);
	}
	
	// checks - any
	public boolean hitsAny(Entity e)
	{
		return hits(e, false);
	}
	
	public boolean hitsAny(TileEntity t)
	{
		return hits(t, false);
	}
	
	public boolean hitsAny(int hash, String name, String group, Class c, String clas, String lclas, int dim, int x, int y, int z)
	{
		return hits(hash, name, group, c, clas, lclas, dim, x, y, z, false);
	}
	
	// checks - specific
	public boolean hits(Entity e, boolean all)
	{
		return hits(
				System.identityHashCode(e),
				EntityHelper.getEntityName(e),
				EntityHelper.getEntityType(e),
				e.getClass(),
				EntityHelper.getEntityClass(e),
				EntityHelper.getEntityLClass(e),
				e.dimension,
				(int)e.posX,
				(int)e.posY,
				(int)e.posZ,
				all);
	}
	
	public boolean hits(TileEntity t, boolean all)
	{
		return hits(
				System.identityHashCode(t),
				TileEntityHelper.getEntityName(t),
				TileEntityHelper.getEntityType(t),
				t.getClass(),
				t.getClass().getSimpleName(),
				t.getClass().getName(),
				t.worldObj.provider.dimensionId,
				t.xCoord,
				t.yCoord,
				t.zCoord,
				all);
	}
	
	public boolean hits(int hash, String name, String group, Class c, String clas, String lclas, int dim, int x, int y, int z, boolean all)
	{
		for (SubFilter f : this)
		{
			if (f.hits(hash, name, group, c, clas, lclas, dim, x, y, z))
			{
				if (!all)
					return !all;
			}
			else
			{
				if (all)
					return !all;
			}
		}
		
		return all;
	}
}
