package ee.lutsu.alpha.mc.aperf.sys.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerPacketLimitConfig 
{
	public String player;
	public HashMap<String, byte[]> limits;
	public boolean on;
	public int diggestFlag;
	
	public static String[] updatePlayerConfig(String[] config, String player, HashMap<String, byte[]> limits, boolean on, int diggestFlag)
	{
		List<PlayerPacketLimitConfig> parsedConf = parseConfig(config);
		
		PlayerPacketLimitConfig found = null;
		for(PlayerPacketLimitConfig conf : parsedConf)
		{
			if (conf.player.equalsIgnoreCase(player))
			{
				found = conf;
				break;
			}
		}
		if (found == null)
		{
			found = new PlayerPacketLimitConfig();
			parsedConf.add(found);
		}
		
		found.player = player;
		found.limits = limits;
		found.on = on;
		found.diggestFlag = diggestFlag;
		
		return compileConfig(parsedConf);
	}
	
	public static List<PlayerPacketLimitConfig> parseConfig(String[] config)
	{
		ArrayList<PlayerPacketLimitConfig> ret = new ArrayList<PlayerPacketLimitConfig>();
		
		if (config == null)
			return ret;
		
		for(String l : config)
		{
			PlayerPacketLimitConfig c = new PlayerPacketLimitConfig();
			c.loadPlayerLine(l);
			ret.add(c);
		}
		
		return ret;
	}
	
	public static String[] compileConfig(List<PlayerPacketLimitConfig> config)
	{
		ArrayList<String> ret = new ArrayList<String>();
		
		for(PlayerPacketLimitConfig l : config)
		{
			ret.add(l.getPlayerLine());
		}
		
		return ret.toArray(new String[ret.size()]);
	}
	
	private void loadPlayerLine(String line)
	{
		String[] playerSep = line.trim().split("/");
		
		player = playerSep[0];
		on = Integer.parseInt(playerSep[1]) > 0;
		diggestFlag = Integer.parseInt(playerSep[2]);

		if (playerSep.length > 3 && playerSep[3].length() > 0)
		{
			limits = new HashMap<String, byte[]>();
			String[] sLimits = playerSep[3].split(";");
			for(String sL : sLimits)
			{
				String[] opSep = sL.split("\\?");

				if (opSep.length == 2)
				{
					String[] vals = opSep[1].split(",");
					byte[] data = new byte[PacketManagerModule.Instance.subTypesCustom];
					for(String val : vals)
					{
						String[] valSep = val.split(":");
						int pos = Integer.parseInt(valSep[0]);
						data[pos] = Byte.parseByte(valSep[1]);
					}
					
					limits.put(opSep[0], data);
				}
			}
		}
	}
	
	private String getPlayerLine()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(player);
		sb.append("/");
		sb.append(on ? 1 : 0);
		sb.append("/");
		sb.append(diggestFlag);
		sb.append("/");
		
		if (limits != null && limits.size() > 0)
		{
			for(String k : limits.keySet())
			{
				byte[] bb = limits.get(k);
				
				if (bb != null && bb.length > 0)
				{
					sb.append(k);
					sb.append("?");
					for(int i = 0; i < bb.length; i++)
					{
						byte b = bb[i];
						if (b != 0)
						{
							sb.append(i);
							sb.append(":");
							sb.append(b);
							sb.append(",");
						}
					}
					sb.deleteCharAt(sb.length() - 1);
					sb.append(";");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}
}
