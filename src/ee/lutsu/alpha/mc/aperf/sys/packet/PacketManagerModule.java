package ee.lutsu.alpha.mc.aperf.sys.packet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.registry.GameRegistry;

import ee.lutsu.alpha.mc.aperf.Log;
import ee.lutsu.alpha.mc.aperf.aPerf;
import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterEvents;
import ee.lutsu.alpha.mc.aperf.sys.objects.SpawnLimit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.IPacketHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.common.Property.Type;

public class PacketManagerModule extends ModuleBase implements IPacketHandler
{
	public static PacketManagerModule Instance = new PacketManagerModule();
	public int subTypesCustom = 25;
	
	private HashMap<EntityPlayer, HashMap<String, long[]>> stats = new HashMap<EntityPlayer, HashMap<String, long[]>>();
	
	private HashMap<EntityPlayer, Date> statTimes = new HashMap<EntityPlayer, Date>();
	private HashMap<EntityPlayer, HashMap<Class, Long>> abstractStats = new HashMap<EntityPlayer, HashMap<Class, Long>>();

	private HashMap<String, byte[]> defaultLimits = new HashMap<String, byte[]>();
	private HashMap<String, Boolean> limitLoaderToggles = new HashMap<String, Boolean>();
	private HashMap<String, HashMap<String, byte[]>> limitLoader = new HashMap<String, HashMap<String, byte[]>>();
	private HashMap<EntityPlayer, HashMap<String, byte[]>> limits = new HashMap<EntityPlayer, HashMap<String, byte[]>>();
	private HashMap<EntityPlayer, HashMap<String, byte[]>> limitsCounter = new HashMap<EntityPlayer, HashMap<String, byte[]>>();
	private PacketManagerEvents playerEvents = new PacketManagerEvents();
	
	public boolean hooked = false;
	private boolean firstStartDone = false;
	
	private MessageDigest diggest = null;
	private ByteArrayOutputStream diggestByteStream = new ByteArrayOutputStream();
	private DataOutputStream diggestStream  = new DataOutputStream(diggestByteStream);
	private HashMap<String, Integer> diggestUsersToggles = new HashMap<String, Integer>();
	private HashMap<EntityPlayer, Integer> diggestUsers = new HashMap<EntityPlayer, Integer>();
	private HashMap<EntityPlayer, ArrayList<BigInteger>> sentPackets = new HashMap<EntityPlayer, ArrayList<BigInteger>>();
	private long lastReset = 0;
	private static int diggestAlive = 5000; // 5 sec
	
	private static Class rpPacket;
	private static Field rpPacketHead, rpPacketBody, rpPacketSubId;
	
	public PacketManagerModule()
	{
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.packet.cmd.PacketStats());
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.packet.cmd.PacketLimits());
		
		try
		{
			diggest = MessageDigest.getInstance("MD5");
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getName() { return "PacketLimiter"; }
	
	@Override
	public void enable()
	{
		super.enable();
		
		loadConfig();
		hook();
	}
	
	@Override
	public void disable()
	{
		super.disable();
		unhook();
	}
	
	// called by the debug client
	public void startTest(EntityPlayer player)
	{
		if (!stats.containsKey(player))
		{
			stats.put(player, new HashMap<String, long[]>());
			abstractStats.put(player, new HashMap<Class, Long>());
			statTimes.put(player, new Date());
		}
	}
	
	public void endTest(EntityPlayer player)
	{
		if (stats.containsKey(player))
		{
			stats.remove(player);
			abstractStats.remove(player);
			statTimes.remove(player);
		}
	}
	
	public Date getTestRunStartTime(EntityPlayer player)
	{
		if (!statTimes.containsKey(player))
			return null;
		
		return statTimes.get(player);
	}
	
	public ArrayList<SimpleEntry<SimpleEntry<String, Integer>, Double>> getTestResults(EntityPlayer player)
	{
		if (!stats.containsKey(player))
			return null;
		
		HashMap<String, long[]> stat = stats.get(player);
		Date statStart = statTimes.get(player);
		
		double time = (double)(new Date().getTime() - statStart.getTime()) / 1000; // seconds
		
		ArrayList<SimpleEntry<SimpleEntry<String, Integer>, Double>> ret = new ArrayList<SimpleEntry<SimpleEntry<String, Integer>, Double>>();

		for (String tag : stat.keySet())
		{
			for(int i = 0; i < stat.get(tag).length; i++)
			{
				if (stat.get(tag)[i] > 0)
					ret.add(new SimpleEntry<SimpleEntry<String, Integer>, Double>(new SimpleEntry<String, Integer>(tag, i), (double)stat.get(tag)[i] / time));
			}
		}
		
		return ret;
	}
	
	public ArrayList<SimpleEntry<Class, Double>> getTestResultsAbstract(EntityPlayer player)
	{
		if (!abstractStats.containsKey(player))
			return null;
		
		ArrayList<SimpleEntry<Class, Double>> ret = new ArrayList<SimpleEntry<Class, Double>>();
		
		HashMap<Class, Long> stat = abstractStats.get(player);
		Date statStart = statTimes.get(player);
		
		double time = (double)(new Date().getTime() - statStart.getTime()) / 1000; // seconds
		
		for(Class key : stat.keySet())
		{
			long used = stat.get(key);
			if (used > 0)
				ret.add(new SimpleEntry<Class, Double>(key, (double)used / time));
		}
		
		return ret;
	}
	
	public void setLimit(EntityPlayer player, String tag, int subId, byte occurence)
	{
		if (!limits.containsKey(player))
		{
			limits.put(player, new HashMap<String, byte[]>());
			limitsCounter.put(player, new HashMap<String, byte[]>());
		}
		
		HashMap<String, byte[]> map = limits.get(player);
		
		if (!map.containsKey(tag))
		{
			map.put(tag, new byte[subTypesCustom]);
			limitsCounter.get(player).put(tag, new byte[subTypesCustom]);
		}
		
		map.get(tag)[subId] = occurence;
		
		updatePersonalPacketLimits(player.username, map, true, diggestUsersToggles.get(player.username.toLowerCase()));
	}

	public void removeLimits(EntityPlayer player)
	{
		if (limits.containsKey(player))
		{
			limits.remove(player);
			limitsCounter.remove(player);
			
			updatePersonalPacketLimits(player.username, null, false, diggestUsersToggles.get(player.username.toLowerCase()));
		}
	}
	
	public HashMap<String, byte[]> getLimits(EntityPlayer player)
	{
		if (!limits.containsKey(player))
			return null;
		
		return limits.get(player);
	}
	
	// called by MC
	public boolean checkDiggest(EntityPlayer player, Packet p) throws IOException
	{
		if (p instanceof Packet3Chat) // never block a chat packet
			return true;
		
		Integer flag = diggestUsers.get(player);
		
		if (flag == null)
			return true;
		
		if ((flag & 1) == 0) // don't do plugins
		{
			if ((rpPacket != null && rpPacket.isInstance(p)) || p instanceof Packet250CustomPayload)
				return true;
		}
		if ((flag & 2) == 0) // don't do mc's
		{
			if (!(rpPacket != null && rpPacket.isInstance(p)) && !(p instanceof Packet250CustomPayload))
				return true;
		}
		
		long time = System.currentTimeMillis();
		
		if (time > diggestAlive + lastReset)
		{
			sentPackets.clear();
			lastReset = time;
		}
		
		diggest.reset();
		diggestByteStream.reset();
		
		p.writePacketData(diggestStream);
		
		diggestStream.flush();
		diggest.update(diggestByteStream.toByteArray());
		byte[] dig = diggest.digest();
		BigInteger i = new BigInteger(1, dig);
		
		ArrayList<BigInteger> packets = sentPackets.get(player);
		
		if (packets != null)
		{
			for(BigInteger bi : packets)
			{
				if (bi.equals(i))
					return false;
			}
		}
		else
			sentPackets.put(player, packets = new ArrayList<BigInteger>());
		
		packets.add(i);
		return true;
	}
	
	public boolean check(EntityPlayer player, String tag, int subId, int packetSize)
	{
		if (limits.containsKey(player) && limits.get(player).containsKey(tag))
		{
			byte limit = limits.get(player).get(tag)[subId];
			byte[] cnt = limitsCounter.get(player).get(tag);
			
			if (cnt == null)
				limitsCounter.get(player).put(tag, cnt = new byte[subTypesCustom]);
			
			if (limit != 0)
			{
				if (limit == Byte.MAX_VALUE) // -128 -> 127
					return false;
				else if (limit == Byte.MIN_VALUE) ;
					// return true
				else if (limit > 0 && (cnt[subId] = (byte)((cnt[subId] + 1) % limit)) != 0)
					return false;
				else if (limit < 0 && (cnt[subId] = (byte)((cnt[subId] + 1) % (limit * -1))) == 0)
					return false;
			}
		}
		
		if (stats.containsKey(player))
		{
			HashMap<String, long[]> m = stats.get(player);
			long[] n = m.get(tag);
			
			if (n == null)
				m.put(tag, n = new long[subTypesCustom]);
			
			n[subId] += packetSize;
		}
		
		return true;
	}
	
	public boolean reportAndCheck(EntityPlayer player, Packet p) throws Exception
	{
		if (p == null || player == null)
			return true;
		
		if (!checkDiggest(player, p))
			return false;
		
		boolean status = true;
		int packetLen = p.getPacketSize();
		
		if (rpPacket != null && rpPacket.isInstance(p))
		{
			ByteArrayOutputStream body = (ByteArrayOutputStream)rpPacketBody.get(p);
			ByteArrayOutputStream head = (ByteArrayOutputStream)rpPacketHead.get(p);
			int subId = rpPacketSubId.getInt(p);
			
			if (body != null && head != null)
			{
				packetLen = body.size() + head.size();
				int b = subId;
				
				if (subId < 0 || subId >= subTypesCustom)
					b = subTypesCustom - 1;
				
				status = check(player, "rp2", b, packetLen);
			}
		}
		else if (p instanceof Packet250CustomPayload)
		{
			Packet250CustomPayload p2 = (Packet250CustomPayload)p;
			packetLen = p2.length;
			int b = p2.data != null && p2.data.length > 0 ? p2.data[0] : 0;
			
			if (p2.channel.equals("IronChest"))
			{
				if (p2.data != null && p2.data.length > 3 * 4)
					b = p2.data[3 * 4];
			}
			
			if (b < 0 || b >= subTypesCustom)
				b = subTypesCustom - 1;
			
			if (p2.channel != null)
				status = check(player, p2.channel, b, packetLen);
		}
		
		if (status && abstractStats.containsKey(player))
		{
			HashMap<Class, Long> classMap = abstractStats.get(player);
			
			Class c = p.getClass();
			Long prevVal = classMap.get(c);
			
			classMap.put(c, (prevVal != null ? prevVal.longValue() : 0) + packetLen);
		}
		
		return status;
	}

	public void toggleLimits(EntityPlayer player, boolean on)
	{
		if (on)
		{
	    	HashMap<String, byte[]> l = limitLoader.get(player.username.toLowerCase());
	    	if (l == null)
	    		l = defaultLimits;
	    	
	    	if (l != null)
	    	{
	    		limits.put(player, l);
	    		limitsCounter.put(player, new HashMap<String, byte[]>());
	    	}
		}
		else
		{
    		limits.remove(player);
    		limitsCounter.remove(player);
		}
		
		limitLoaderToggles.put(player.username.toLowerCase(), on);
		updatePersonalPacketLimits(player.username, limitLoader.get(player.username.toLowerCase()), on, diggestUsersToggles.get(player.username.toLowerCase()));
	}
	
	public boolean areLimitsActive(EntityPlayer player)
	{
		return limits.containsKey(player);
	}
	
	public boolean autoLoadLimits(String player)
	{
		Boolean b = limitLoaderToggles.get(player.toLowerCase());
		
		return b == null ? false : b.booleanValue();
	}
	
	public void loadPersonalPacketLimits(String name, HashMap<String, byte[]> limits, boolean on, int diggestFlag)
	{
		String n = name.toLowerCase();
		if (limits != null)
			limitLoader.put(n, limits);
		else
			limitLoader.remove(n);
		
		diggestUsersToggles.put(n, diggestFlag);
		limitLoaderToggles.put(n, on);
	}
    
    public void loadDefaultLimits(HashMap<String, byte[]> limits)
    {
    	defaultLimits = limits;
    }
    
    public void playerLogin(EntityPlayer pl)
    {
    	if (autoLoadLimits(pl.username))
    		toggleLimits(pl, true);
    	
    	if (diggestUsersToggles.get(pl.username.toLowerCase()) != null)
    		diggestUsers.put(pl, diggestUsersToggles.get(pl.username.toLowerCase()));
    }
    
    public void setDiggestLimit(EntityPlayer player, int flag)
    {
    	if (flag != 0)
    		diggestUsers.put(player, flag);
    	else
    		diggestUsers.remove(player);
    	
    	updatePersonalPacketLimits(player.username, limitLoader.get(player.username.toLowerCase()), autoLoadLimits(player.username), flag);
    }
    
    public void playerQuit(EntityPlayer ent)
    {
    	stats.remove(ent);
    	statTimes.remove(ent);
    	abstractStats.remove(ent);
    	limits.remove(ent);
    	limitsCounter.remove(ent);
    }
    
    public int getActiveDiggestFlag(EntityPlayer player)
    {
    	return diggestUsers.get(player) == null ? 0 : diggestUsers.get(player).intValue();
    }
    
    public interface PersonalLimitUpdateHandler
    {
    	public void updatePersonalPacketLimits(String name, HashMap<String, byte[]> limits, boolean on, int diggestFlag);
    }
    
    // -- publics -- //
    
	public void hook()
	{
		stats = new HashMap<EntityPlayer, HashMap<String, long[]>>();
		
		statTimes = new HashMap<EntityPlayer, Date>();
		abstractStats = new HashMap<EntityPlayer, HashMap<Class, Long>>();

		limits = new HashMap<EntityPlayer, HashMap<String, byte[]>>();
		limitsCounter = new HashMap<EntityPlayer, HashMap<String, byte[]>>();
		
		if (!hooked)
		{
			try
			{
				if (!firstStartDone)
				{
					GameRegistry.registerPlayerTracker(playerEvents);
					
					rpPacket = Class.forName("com.eloraam.redpower.core.Packet211TileDesc");
					Class clVLC = Class.forName("com.eloraam.redpower.core.PacketVLC");
					rpPacketHead = clVLC.getDeclaredField("headout");
					rpPacketBody = clVLC.getDeclaredField("bodyout");
					rpPacketSubId = rpPacket.getDeclaredField("subId");
				}
				
			}
			catch (Throwable t)
			{
				Log.severe("Could not get RP's packet class. Is RP loaded? Will ignore RP packets.", t);
			}
			firstStartDone = true;

			try
			{
				List<IPacketHandler> handlers = (List<IPacketHandler>)net.minecraft.network.TcpConnection.class.getMethod("getPacketHandlers").invoke(null);
				handlers.add(this);
			}
			catch (Throwable t)
			{
				throw new RuntimeException("Could not attach the packet listener", t);
			}
			
			hooked = true;
		}
	}
	
	public void unhook()
	{
		if (!hooked)
			return;
		
		try
		{
			List<IPacketHandler> handlers = (List<IPacketHandler>)net.minecraft.network.TcpConnection.class.getMethod("getPacketHandlers").invoke(null);
			handlers.remove(this);
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Could not detach the packet listener", t);
		}
		
		hooked = false;
	}
	
	public static String getPacketDesc(String tag, int subId)
	{
		if (tag.equals("rp2"))
		{
			if (subId == 1)
				return "RP Simple Logic block update (and, or gate etc)";
			else if (subId == 2)
				return "RP Pointer Logic update (timer, seq, state)";
			else if (subId == 5)
				return "RP MultiPart block update [x136] (covers,panels, wires)";
			else if (subId == 6)
				return "RP Array Logic update (cells)";
			else if (subId == 7)
				return "RP Machines update [x133,x150] (pc,filter,motor,etc)";
			else if (subId == 8)
				return "RP Machine panel update [x151] (solar,pump,accel,grate)";
			else if (subId == 9)
				return "RP Frame update";
			else if (subId == 10)
				return "RP Tube flow contents";
			else if (subId == 11)
				return "RP Fluid pipe content update";
		}
		else if (tag.equals("ic2"))
		{
			if (subId == 0)
				return "IC Block prop update (coords, name, val)";
			else if (subId == 1)
				return "IC Block event (coords, event)";
			else if (subId == 2)
				return "IC Item event (item data, event)";
			else if (subId == 3)
				return "IC Block update (coords)";
			else if (subId == 4)
				return "IC GUI Init (coords or item index, event)";
		}
		else if (tag.equals("EE2"))
		{
			if (subId == 0)
				return "EE Key Press (key code)"; // client -> server
			else if (subId == 1)
				return "EE Block update (coord, dir, player name)"; // server -> client
			else if (subId == 2)
				return "EE Pedestal update (coord, item, active)";
		}
		else if (tag.equals("BC"))
		{
			if (subId == 0)
				return "BC Block Update";
			else if (subId == 1)
				return "BC Pipe Description";
			else if (subId == 2)
				return "BC Pipe Contents";
			else if (subId == 20)
				return "BC Selected Assembly Get";
			else if (subId == 21)
				return "BC Selected Assembly";
			else if (subId == 30)
				return "BC Diamond Pipe Contents";
		}
		else if (tag.equals("IronChest"))
		{
			if (subId == 0)
				return "Iron chest";
			else if (subId == 1)
				return "Gold chest";
			else if (subId == 2)
				return "Diamond chest";
			else if (subId == 3)
				return "Copper chest";
			else if (subId == 4)
				return "Silver chest";
			else if (subId == 5)
				return "Crystal chest (with content - 24 ints)";
			else if (subId == 6)
				return "Wood chest";
		}
		else if (tag.equals("Railcraft"))
		{
			if (subId == 0)
				return "RC Block event";
			else if (subId == 1)
				return "RC GUI Event";
			else if (subId == 2)
				return "RC Block update";
		}
		
		return null;
	}

	@Override
	public void loadConfig()
	{
		ConfigCategory cat = aPerf.instance.config.getCategory("Packet-Limiter.DefaultLimit");

    	if (cat != null && cat.values().size() > 0)
    	{
    		HashMap<String, byte[]> limits = new HashMap<String, byte[]>();
    		for (Property l : cat.values())
    		{
    			String[] vals = l.getString().split(",");
    			byte[] d = new byte[subTypesCustom];
    			for(String val : vals)
    			{
    				String[] v = val.trim().split(":");
    				d[Integer.parseInt(v[0])] = Byte.parseByte(v[1]);
    			}
    			
    			limits.put(l.getName(), d);
    		}
    		
    		loadDefaultLimits(limits);
    	}
    	
    	List<String> values = new ArrayList<String>();
    	cat = aPerf.instance.config.getCategory("Packet-Limiter.PersonalLimits");
    	for (Property p : cat.values())
    	{
    		values.add(p.getString());
    	}

    	String[] cnf = values.toArray(new String[values.size()]);
    	List<PlayerPacketLimitConfig> cnf2 = PlayerPacketLimitConfig.parseConfig(cnf);
    	
    	for (PlayerPacketLimitConfig c : cnf2)
    		loadPersonalPacketLimits(c.player, c.limits, c.on, c.diggestFlag);
	}
	
    private void updatePersonalPacketLimits(String name, HashMap<String, byte[]> limits, boolean on, Integer diggestFlag)
    {
    	int dig = diggestFlag == null ? 0 : diggestFlag.intValue();
    	
    	loadPersonalPacketLimits(name, limits, on, dig);

    	List<String> values = new ArrayList<String>();
    	ConfigCategory cat = aPerf.instance.config.getCategory("Packet-Limiter.PersonalLimits");
    	for (Property p : cat.values())
    	{
    		values.add(p.getString());
    	}

    	String[] cnf = values.toArray(new String[values.size()]);
    	
    	cnf = PlayerPacketLimitConfig.updatePlayerConfig(cnf, name, limits, on, dig);
    	
    	cat.clear();
    	int i = 1;
    	for (String s : cnf)
    	{
    		String key = "Def" + String.valueOf(i++);
    		cat.put(key, new Property(key, s, Type.STRING));
    	}

		aPerf.instance.config.save();
    }
    
    @Override
	public boolean onOutgoingPacket(NetHandler network, int packetID, Packet packet) 
	{
		if (network == null || packet == null)
			return true;
		
		EntityPlayer ply = network.getPlayer();
		if (ply == null)
			return true;
		
		try
		{
			return reportAndCheck(ply, packet);
		}
		catch(Exception e)
		{
			return true;
		}
	}
}

