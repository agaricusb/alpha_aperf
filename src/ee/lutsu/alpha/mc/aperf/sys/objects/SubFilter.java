package ee.lutsu.alpha.mc.aperf.sys.objects;

import java.util.ArrayList;
import java.util.List;

import ee.lutsu.alpha.mc.aperf.sys.entity.EntityClassMap;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntityHelper;
import ee.lutsu.alpha.mc.aperf.sys.tile.TileEntityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

public class SubFilter 
{
	public enum Type
	{
		All("Hits everything", "No value"),
		Name("Compares the entity name", "String"),
		Group("Compares the entity group. This is made-up. Use entity list to see what comes up", "String"),

		Class("Compares the entity class", "Class name. Ex: EntityPig"),
		LClass("Compares the package and entity name", "Full class name. Ex: net.minecraft.passive.EntityPig"),
		Instance("Checks if the entity class/superclass is the one specified", "Full class name. Ex: net.minecraft.entity.item.EntityItem"),
		
		Dimension("Checks the entity dimension", "Integer list, <n>[.<n>[.<n>...]]. Ex: -1.0.1"),
		Where("Checks the entity chunk coordinates to be between", "Chunk coords, <x>.<z>[/<x2>.<z2>]. Ex:-10.30"), // chunk location
		Pos("Checks the entity position", "Coords, <x>.<y>.<z>[/<x2>.<y2>.<z2>]. Ex:-500.0.-500/500.255.500"),
		Hash("Entity java given hashcode", "Hash. Ex: 4d4a2b4d");
		
		public String description;
		public String valueDesc;
		
		Type(String desc, String vdesc)
		{
			description = desc;
			valueDesc = vdesc;
		}
		
		public static Type get(String s)
		{
			for (Type l : values())
			{
				if (l.name().toLowerCase().startsWith(s.toLowerCase()))
					return l;
			}
			throw new RuntimeException("Unknown Type value '" + s + "'");
		}
	}
	
	private Type type;
	private String value;
	private int iX1, iX2, iY1, iY2, iZ1, iZ2, hash;
	private List<Integer> dims;
	private Class clazz;
	
	// public properties
	public Type getType() { return type;  }
	public String getValue() { return value; }
	
	// checks
	public boolean hits(int hash, String name, String group, Class c, String clas, String lclas, int dim, int x, int y, int z)
	{
		return generalHits(hash, name, group) && classHits(c, clas, lclas) && locationHits(dim, x, y, z);
	}
	
	private boolean generalHits(int hash, String sName, String sGroup)
	{
		if (type == Type.All)
			return true;
		else if (type == Type.Hash)
			return this.hash == hash;
		else if (type == Type.Name)
			return valueStringEquals(sName);
		else if (type == Type.Group)
			return valueStringEquals(sGroup);
		
		return true;
	}
	
	private boolean classHits(Class c, String claz, String lclas)
	{
		if (type == Type.Instance)
			return clazz.isAssignableFrom(c);
		else if (type == Type.Class)
			return valueStringEquals(claz);
		else if (type == Type.LClass)
			return valueStringEquals(lclas);
		
		return true;
	}
	
	private boolean locationHits(int dim, int x, int y, int z)
	{
		if (type == Type.Dimension)
			return dims.contains(dim);
		else if (type == Type.Where)
		{
	        int cx = MathHelper.floor_double(x / 16.0D);
	        int cz = MathHelper.floor_double(z / 16.0D);
	        
	        return cx >= iX1 && cx <= iX2 && cz >= iZ1 && cz <= iZ2;
		}
		else if (type == Type.Pos)
			return x >= iX1 && x <= iX2 && y >= iY1 && y <= iY2 && z >= iZ1 && z <= iZ2;

		return true;
	}
	
	private boolean valueStringEquals(String other)
	{
		return value.equalsIgnoreCase(other.replaceAll(" ", "_")); // TODO: implement some regex
	}
	
	// string <-> values
	public SubFilter(String val) throws Exception
	{
		String[] sp = val.split(":");
		type = Type.get(sp[0]);
		
		if (sp.length > 1)
			value = sp[1];
		
		if (type != Type.All && (value == null || value.equals("")))
			throw new Exception("Value can be empty only on the type All");
		
		setInternals();
	}
	
	private void setInternals() throws ClassNotFoundException
	{
		switch (type)
		{
		case All:
			value = ""; // resets value if its set to something
			break;
			
		case Dimension:
			dims = new ArrayList<Integer>();
			for (String v : value.split("\\."))
				dims.add(new Integer(v));
			break;
			
		case Where:
			String[] sp = value.split("/");
			String[] sp2 = sp[0].split("\\.");
			iX1 = iX2 = Integer.parseInt(sp2[0]);
			iZ1 = iZ2 = Integer.parseInt(sp2[1]);
			
			if (sp.length > 1)
			{
				sp2 = sp[1].split("\\.");
				iX2 = Integer.parseInt(sp2[0]);
				iZ2 = Integer.parseInt(sp2[1]);
			}
			break;
			
		case Pos:
			String[] spo = value.split("/");
			String[] spo2 = spo[0].split("\\.");
			iX1 = iX2 = Integer.parseInt(spo2[0]);
			iY1 = iY2 = Integer.parseInt(spo2[1]);
			iZ1 = iZ2 = Integer.parseInt(spo2[2]);
			
			if (spo.length > 1)
			{
				spo2 = spo[1].split("\\.");
				iX2 = Integer.parseInt(spo2[0]);
				iY2 = Integer.parseInt(spo2[1]);
				iZ2 = Integer.parseInt(spo2[2]);
			}
			break;
			
		case Instance:
			clazz = EntityClassMap.instance.classForType(value);
			if (clazz == null)
				clazz = Class.forName(value);
			break;
			
		case Hash:
			hash = Integer.parseInt(value, 16);
			break;
			
		default:
			break;
		}
	}
	
	public String serialize()
	{
		if (type == Type.All)
			return type.toString().substring(0, 1).toLowerCase();
		
		return String.format("%s:%s", type.toString().substring(0, 1).toLowerCase(), value);
	}
	
	public String serializeDisplay()
	{
		if (type == Type.All)
			return type.toString();
		
		return String.format("%s: %s", type.toString(), value);
	}

	// others
	
	public boolean typeEquals(SubFilter other)
	{
		return type == other.type;
	}
	
	public boolean equals(SubFilter other)
	{
		return typeEquals(other) && value.equals(other.value);
	}
	
	@Override
	public boolean equals(Object val)
	{
		if (val instanceof SubFilter)
			return equals((SubFilter)val);
		else
			return false;
	}
}
