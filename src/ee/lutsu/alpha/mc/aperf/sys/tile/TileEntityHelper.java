package ee.lutsu.alpha.mc.aperf.sys.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.tileentity.TileEntity;

public class TileEntityHelper
{
	public static List<Class> getAllInterfaces(Class cls)
	{
		List<Class> lst = new ArrayList<Class>();
		
		lst.addAll(Arrays.asList(cls.getInterfaces()));
		
		Class s = cls.getSuperclass();
		if (s != null)
			lst.addAll(getAllInterfaces(s));
		
		return lst;
	}
	
	public static String getEntityName(TileEntity e)
	{
		String n = e.getBlockType().getLocalizedName();
		if (n.startsWith("tile.") && n.endsWith(".name")) // no translation
			return e.getBlockType().getUnlocalizedName().substring("tile.".length());
		else
			return n;
	}
	
	public static String getEntityType(TileEntity e)
	{
		String n = e.getClass().getName();
		
		if (!n.contains(".") || n.startsWith("net.minecraft."))
			return "vanilla";
		else
			return n.substring(0, n.lastIndexOf("."));
	}
}
