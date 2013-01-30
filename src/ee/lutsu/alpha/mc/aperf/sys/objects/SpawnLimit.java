package ee.lutsu.alpha.mc.aperf.sys.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import ee.lutsu.alpha.mc.aperf.commands.CommandException;
import ee.lutsu.alpha.mc.aperf.sys.entity.limits.*;

import com.google.common.base.Joiner;

public abstract class SpawnLimit 
{
	public enum Type
	{
		Disabled(Disabled.class, "The mob that hits this filter cannot spawn, at all"),
		CountServer(Count.class, "Limit by counting entities by the same filter in the whole server"),
		CountMap(Count.class, "Limit by counting entities by the same filter over the whole map"),
		CountChunk(CountChunk.class, "Limit by counting entities by the same filter on the chunk and around it"),
		
		ClearAbove(ClearAbove.class, "Spawn only when theres a number of blocks above the mob empty"),
		WaterAbove(WaterAbove.class, "Spawn only when theres a number of blocks above the mob water"),
		BlocksBelow(BlocksBelow.class, "Spawn only when theres specific blocks under the mob");
		
		public Class<? extends SpawnLimit> handler;
		public String description;
		
		Type(Class<? extends SpawnLimit> handler, String desc)
		{
			this.handler = handler;
			this.description = desc;
		}
		
		public static Type get(String s)
		{
			for (Type l : values())
			{
				if (l.name().toLowerCase().startsWith(s.toLowerCase()))
					return l;
			}
			throw new RuntimeException("Unknown LimitFilterType value '" + s + "'");
		}
	}
	
	public Filter filter;
	public Type type;
	public boolean on;
	
	public abstract boolean canSpawn(Entity e, World world);
	
	protected void load(Map<String, String> args) throws Exception
	{
		on = getBoolean(args, "on", true);
		filter = new Filter(getString(args, "filter"));
	}
	
	protected void save(Map<String, String> args) 
	{ 
		if (!on) args.put("on", String.valueOf(on));
		args.put("filter", filter.serialize());
	}
	
	protected void getArguments(Map<String, String> list)
	{
		list.put("on?", "Boolean. Is filter active or not. Defaults to Yes");
		list.put("filter", "String. Use /ap filterhelp for more info");
	}
	
	public Map<String, String> getDefinedArguments()
	{
		HashMap<String, String> args = new HashMap<String, String>();
		getArguments(args);
		return args;
	}
	
	public String getDisplayOptions()
	{
		HashMap<String, String> args = new HashMap<String, String>();
		save(args);
		
		args.remove("on");
		args.remove("filter");
		
		ArrayList<String> vals = new ArrayList<String>();
		for (Entry<String, String> kv : args.entrySet())
			vals.add(kv.getKey() + ": " + kv.getValue());
		
		return Joiner.on(", ").join(vals);
	}
	
	public static SpawnLimit fromUserInput(String type, String filter, String options, String active) throws Exception
	{
		Type t = Type.get(type);
		SpawnLimit limit = t.handler.newInstance();
		limit.type = t;
		
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("filter", filter);
		if (active != null && !active.equals("")) args.put("on", active);
		
		if (options != null && options.length() > 0)
		{
			for (String kv : options.split(","))
			{
				String[] sp = kv.trim().split(":");
				args.put(sp[0], sp[1]);
			}
		}
		
		limit.load(args);
		
		return limit;
	}
	
	public void updateOption(String key, String val) throws Exception
	{
		HashMap<String, String> args = new HashMap<String, String>();
		
		save(args);
		args.put(key, val);
		load(args);
	}

	public static SpawnLimit deserialize(String line) throws Exception
	{
		String[] sp = line.split("\\|");

		Type type = Type.get(sp[0]);

		SpawnLimit limit = type.handler.newInstance();
		limit.type = type;
		
		HashMap<String, String> args = new HashMap<String, String>();
		
		if (sp.length > 1)
		{
			for (int i = 1; i < sp.length; i += 2)
			{
				String spo1 = sp[i].trim();
				String spo2 = sp[i + 1].trim();
				args.put(spo1, spo2);
			}
		}
		
		limit.load(args);
		
		return limit;
	}
	
	public String serialize()
	{
		HashMap<String, String> args = new HashMap<String, String>();
		save(args);
		
		ArrayList<String> vals = new ArrayList<String>();
		for (Entry<String, String> kv : args.entrySet())
			vals.add(kv.getKey() + "|" + kv.getValue());

		return type.name() + "|" + Joiner.on("|").join(vals);
	}
	
	protected String getString(Map<String, String> args, String name) throws CommandException
	{
		String v = args.get(name.toLowerCase());
		
		if (v == null || v.equals(""))
			throw new CommandException("Argument " + name + " is missing");
		
		return v;
	}
	
	protected String getString(Map<String, String> args, String name, String def) throws CommandException
	{
		String v = args.get(name.toLowerCase());
		
		if (v == null || v.equals(""))
			return def;
		
		return v;
	}
	
	protected int getInt(Map<String, String> args, String name) throws CommandException
	{
		String s = getString(args, name);
		
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			throw new CommandException(String.format("Value '%s' for argument %s is not a number", s, name), e);
		}
	}
	
	protected int getInt(Map<String, String> args, String name, int def) throws CommandException
	{
		String s = getString(args, name, String.valueOf(def));
		
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			throw new CommandException(String.format("Value '%s' for argument %s is not a number", s, name), e);
		}
	}
	
	protected boolean getBoolean(Map<String, String> args, String name, boolean def) throws CommandException
	{
		String s = getString(args, name, String.valueOf(def));
		
		return s.equalsIgnoreCase("1") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("active");
	}
}
