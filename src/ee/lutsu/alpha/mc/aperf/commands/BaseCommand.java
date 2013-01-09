package ee.lutsu.alpha.mc.aperf.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.bukkit.ChatColor;

import ee.lutsu.alpha.mc.aperf.Log;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public abstract class BaseCommand 
{
	public void onRegistered(CommandsManager manager)
	{
	}

	protected void msg(ICommandSender sender, String msg)
	{
		if (sender == MinecraftServer.getServer())
			Log.direct(msg); // supports colors
		else
			sender.sendChatToPlayer(msg);
	}

	protected void msg(ICommandSender sender, String format, Object ... args)
	{
		msg(sender, String.format(format, args));
	}

	public boolean isInteger(String input)   
	{   
		try  
		{   
			Integer.parseInt(input);   
			return true;   
		}   
		catch(Exception e)   
		{   
			return false;   
		}   
	}  
	
	public static <T> List<Entry<String, Integer>> getGroupedCounts(List list, IGrouper<T> grouper)
	{
		HashMap<String, Integer> grouped = new HashMap<String, Integer>();
		
		for (Object o : list)
		{
			String group = grouper.group((T)o);
			
			if (group == null)
				continue;
			
			Integer prev = grouped.get(group);
			grouped.put(group, prev == null ? 1 : prev + 1);
		}
		
		ArrayList<Entry<String, Integer>> sorted = new ArrayList<Entry<String, Integer>>(grouped.entrySet());
		
		Collections.sort(sorted, new Comparator<Entry<String, Integer>>()
		{
			@Override
			public int compare(Entry<String, Integer> arg0, Entry<String, Integer> arg1)
			{
				return Integer.compare(arg1.getValue(), arg0.getValue());
			}
		});

		return sorted;
	}
	
	public interface IGrouper<T>
	{
		public String group(T t);
	}
	
	public interface IListForObject<T>
	{
		public List list(T obj);
	}
	
	protected void sendCountedList(ICommandSender sender, String prefix, List<Entry<String, Integer>> counts, Integer from, Integer cnt)
	{
		String maxCntLen = null;
		int i = -1;
		int iFrom = from == null ? 0 : from.intValue();
		int limit = cnt == null ? counts.size() : cnt.intValue() + iFrom;
		for (Entry<String, Integer> entry : counts)
		{
			i++;
			if (i < iFrom || i >= limit)
				continue;
			
			if (maxCntLen == null)
				maxCntLen = String.valueOf(entry.getValue().toString().length());
			
			msg(sender, "%s%s%" + maxCntLen + "d %s| %s%s", 
					prefix, ChatColor.RED, entry.getValue(), 
					ChatColor.GREEN, ChatColor.YELLOW, entry.getKey());
			
			
		}
	}
	
	public <T> void sendWorldGroupedList(String reportName, ICommandSender sender, IListForObject<WorldServer> list, IGrouper<T> grouper) 
	{
		sendWorldGroupedList(reportName, sender, list, grouper, null, null);
	}
	
	public <T> void sendWorldGroupedList(String reportName, ICommandSender sender, IListForObject<WorldServer> list, IGrouper<T> grouper, Integer start, Integer count) 
	{
		msg(sender, "%s%s", ChatColor.DARK_GREEN, reportName);
		msg(sender, "%s----------------------------------", ChatColor.GRAY);
		
		for (WorldServer serv : MinecraftServer.getServer().worldServers)
		{
			List l = list.list(serv);
			if (l.size() < 1)
				continue;
			
			int cnt = 0;
			List<Entry<String, Integer>> counts = getGroupedCounts(l, grouper);
			for (Entry<String, Integer> e : counts)
				cnt += e.getValue();

			msg(sender, "%s%s [%d], %s %s", ChatColor.GREEN, serv.provider.getDimensionName(),
					serv.provider.dimensionId, cnt, cnt == 1 ? "entity" : "entities");
			
			sendCountedList(sender, "   ", counts, start, count);
		}
		
		msg(sender, "%s----------------------------------", ChatColor.GRAY);
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
}
