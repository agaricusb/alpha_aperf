package ee.lutsu.alpha.mc.aperf.sys.entity;

import java.util.ArrayList;

import com.google.common.base.Joiner;

public class SpawnLimiterLimit 
{
	public enum LimitType
	{
		All,
		Group,
		Class,
		LClass,
		Name;
		
		public static LimitType get(String s)
		{
			for (LimitType l : values())
			{
				if (l.name().toLowerCase().startsWith(s.toLowerCase()))
					return l;
			}
			throw new RuntimeException("Unknown LimitType value '" + s + "'");
		}
	}
	
	public enum LimitFilterType
	{
		Count,
		ClearAbove,
		WaterAbove,
		BlockBelow;
		
		public static LimitFilterType get(String s)
		{
			for (LimitFilterType l : values())
			{
				if (l.name().toLowerCase().startsWith(s.toLowerCase()))
					return l;
			}
			throw new RuntimeException("Unknown LimitFilterType value '" + s + "'");
		}
		
		public String toString(SpawnLimiterLimit limit)
		{
			return this.toString() + "-" + String.valueOf(limit.limitInt) + (limit.limitInt2 != null ? ":" + String.valueOf(limit.limitInt2) : "");
		}
	}
	
	public enum LimitRange
	{
		Server,
		Map,
		Chunk;
		
		public static LimitRange get(String s)
		{
			for (LimitRange l : values())
			{
				if (l.name().toLowerCase().startsWith(s.toLowerCase()))
					return l;
			}
			throw new RuntimeException("Unknown LimitRange value '" + s + "'");
		}
	}

	public boolean active = true;
	public LimitType type;
	
	public LimitFilterType filterType;
	public int limitInt = 0;
	public Integer limitInt2 = null;
	
	public String limitName;
	
	public LimitRange range;
	public int rangeExt = 0;
	
	public boolean allDims = true;
	public int dimension;
	
	public static SpawnLimiterLimit deserialize(String data_s)
	{
		SpawnLimiterLimit limit = new SpawnLimiterLimit();
		
		String[] data = data_s.split(", ");
		
		limit.active = data[0].equalsIgnoreCase("on");
		limit.range = LimitRange.valueOf(data[1]);
		limit.rangeExt = Integer.parseInt(data[2]);
		limit.type = LimitType.valueOf(data[3]);
		limit.limitName = data[4];
		limit.allDims = data[5].equalsIgnoreCase("yes");
		limit.dimension = Integer.parseInt(data[6]);
		
		limit.filterType = LimitFilterType.valueOf(data[7]);
		
		if (data.length > 8) // else value is "0"
		{
			String[] vals = data[8].split("\\|");
			
			if (vals.length > 0 && vals[0].length() > 0)
			{
				for (String v : vals)
				{
					String[] vv = v.split(":");
					
					if (vv[0].equals("i"))
						limit.limitInt = Integer.parseInt(vv[1]);
					else if (vv[0].equals("i2"))
						limit.limitInt2 = Integer.parseInt(vv[1]);
				}
			}
		}
		
		return limit;
	}
	
	public String serialize()
	{
		String[] data = new String[9];
		
		data[0] = active ? "on" : "off";
		data[1] = range.toString();
		data[2] = String.valueOf(rangeExt);
		data[3] = type.toString();
		data[4] = limitName;
		data[5] = allDims ? "yes" : "no";
		data[6] = String.valueOf(dimension);
		
		data[7] = filterType.toString();
		
		String v = "";
		
		ArrayList<String> vals = new ArrayList<String>();
		if (limitInt != 0)
			vals.add("i:" + String.valueOf(limitInt));

		if (limitInt2 != null)
			vals.add("i2:" + String.valueOf(limitInt2));
		
		data[8] = Joiner.on('|').join(vals);
		
		return Joiner.on(", ").join(data);
	}
	
	public String filterStr()
	{
		return this.filterType.toString(this);
	}
}
