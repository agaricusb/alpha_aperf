package ee.lutsu.alpha.mc.aperf.sys.entity.cmd;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.command.ICommandSender;

import org.bukkit.ChatColor;

import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.commands.CommandException;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterModule;
import ee.lutsu.alpha.mc.aperf.sys.objects.Filter;
import ee.lutsu.alpha.mc.aperf.sys.objects.SpawnLimit;

public class SpawnLimiterCmd extends BaseCommand
{
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:spawn|s)",
		description = "Lists the spawn limits",
		permission = "aperf.cmd.entity.spawn.list"
	)
	public void list(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		String format = "%s%s | %s%-6s%s | %-8s | %-20s | %s";
		
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
		if (!SpawnLimiterModule.instance.isEnabled())
			msg(sender, "%s%s is currently disabled. Use /ap m for more info", ChatColor.LIGHT_PURPLE, SpawnLimiterModule.instance.getName());
		
		msg(sender, "%sExecute onChunkLoad: %s%s%s, normalSpawn: %s%s%s, otherSpawn: %s%s%s", ChatColor.GREEN,
				SpawnLimiterModule.instance.executeRulesOnChunkLoad ? ChatColor.DARK_GREEN : ChatColor.RED,
				SpawnLimiterModule.instance.executeRulesOnChunkLoad ? "on" : "off", ChatColor.GREEN,
						
				SpawnLimiterModule.instance.executeRulesOnNormalSpawning ? ChatColor.DARK_GREEN : ChatColor.RED,
				SpawnLimiterModule.instance.executeRulesOnNormalSpawning ? "on" : "off", ChatColor.GREEN,
										
				SpawnLimiterModule.instance.exetuteRulesOnOtherwiseSpawned ? ChatColor.DARK_GREEN : ChatColor.RED,
				SpawnLimiterModule.instance.exetuteRulesOnOtherwiseSpawned ? "on" : "off", ChatColor.GREEN
				);
		
		msg(sender, format, ChatColor.DARK_GREEN, "#", "", "Active", "", "Type", "Filter", "Options");
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
		
		int i = 1;
		for (SpawnLimit limit : SpawnLimiterModule.instance.limits)
		{
			msg(sender, format, ChatColor.GREEN,
				i++,
				limit.on ? ChatColor.DARK_GREEN : ChatColor.RED,
				limit.on ? "on" : "off", ChatColor.GREEN,
				limit.type.name(),
				limit.filter.serializeDisplay(),
				limit.getDisplayOptions());
		}
		
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:spawn|s) (?:add|a) <type> <filter> [options] [active]",
		description = "Add a new spawn limit\n" +
				"Type: '/ap e s types' for more info\n" +
				"Filter: '/ap filterhelp' for more info\n" +
				"Options: Limit type specific options. '/ap e s types' for more info\n" +
				"Active: on|off\n",
		permission = "aperf.cmd.entity.spawn.add"
	)
	public void add(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		SpawnLimit limit = null;
		
		try
		{
			limit = SpawnLimit.fromUserInput(
				args.get("type"), 
				args.get("filter"), 
				args.get("options"), 
				args.get("active"));
		}
		catch (Exception e)
		{
			throw new CommandException(e.toString(), e);
		}
		
		SpawnLimiterModule.instance.limits.add(limit);
		SpawnLimiterModule.instance.saveConfig();
		
		msg(sender, "%sLimit added", ChatColor.GREEN);
		
		list(plugin, sender, null);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:spawn|s) (?:remove|delete|r|d) <index>",
		description = "Removes the specified entity limit",
		permission = "aperf.cmd.entity.spawn.remove"
	)
	public void remove(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		int index = Integer.parseInt(args.get("index")) - 1;

		SpawnLimit limit = SpawnLimiterModule.instance.limits.remove(index);
		SpawnLimiterModule.instance.saveConfig();
		
		if (limit != null)
			msg(sender, "%sLimit removed", ChatColor.GREEN);
		else
			msg(sender, "%sLimit not found", ChatColor.YELLOW);
		
		list(plugin, sender, null);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:spawn|s) (?:toggle|t) <index>",
		description = "Toggle the specified entity limit on/off",
		permission = "aperf.cmd.entity.spawn.toggle"
	)
	public void toggle(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		int index = Integer.parseInt(args.get("index")) - 1;

		SpawnLimit limit = SpawnLimiterModule.instance.limits.get(index);
		if (limit == null)
		{
			msg(sender, "%sLimit not found", ChatColor.YELLOW);
			return;
		}
		
		limit.on = !limit.on;
		
		SpawnLimiterModule.instance.saveConfig();
		msg(sender, "%sLimit toggled", ChatColor.GREEN);

		list(plugin, sender, null);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:spawn|s) (?:togglegeneral|tg) <key>",
		description = "Toggle 'chunkload', 'normal' or 'other' ruleset on/off",
		permission = "aperf.cmd.entity.spawn.togglegeneral"
	)
	public void toggleGeneral(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		String key = args.get("key");
		boolean done = false;
		
		if (key.equalsIgnoreCase("chunkload"))
		{
			SpawnLimiterModule.instance.executeRulesOnChunkLoad = !SpawnLimiterModule.instance.executeRulesOnChunkLoad;
			done = true;
		}
		else if (key.equalsIgnoreCase("normal"))
		{
			SpawnLimiterModule.instance.executeRulesOnNormalSpawning = !SpawnLimiterModule.instance.executeRulesOnNormalSpawning;
			done = true;
		}
		else if (key.equalsIgnoreCase("other"))
		{
			SpawnLimiterModule.instance.exetuteRulesOnOtherwiseSpawned = !SpawnLimiterModule.instance.exetuteRulesOnOtherwiseSpawned;
			done = true;
		}
		
		if (!done)
			msg(sender, "%sConfig not found. Use: 'chunkload', 'normal' or 'other'", ChatColor.YELLOW);
		else
		{
			msg(sender, "%sConfig toggled and saved", ChatColor.GREEN);
			SpawnLimiterModule.instance.saveConfig();
			list(plugin, sender, null);
		}
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:spawn|s) (?:set|s) <index> <key> [value]",
		description = "Sets the option for the limit",
		permission = "aperf.cmd.entity.spawn.set"
	)
	public void setOption(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		int index = Integer.parseInt(args.get("index")) - 1;

		SpawnLimit limit = SpawnLimiterModule.instance.limits.get(index);
		if (limit == null)
		{
			msg(sender, "%sLimit not found", ChatColor.YELLOW);
			return;
		}

		limit.updateOption(args.get("key"), args.get("value"));
		
		SpawnLimiterModule.instance.saveConfig();
		msg(sender, "%sLimit option set", ChatColor.GREEN);

		list(plugin, sender, null);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:spawn|s) types",
		description = "Displays the limiter types",
		permission = "aperf.cmd.entity.spawn.types"
	)
	public void types(Object plugin, ICommandSender sender, Map<String, String> args) throws InstantiationException, IllegalAccessException 
	{
		msg(sender, "%s---------------- %sTypes%s -------------------------", ChatColor.GRAY, ChatColor.GOLD, ChatColor.GRAY);
		for (SpawnLimit.Type t : SpawnLimit.Type.values())
		{
			msg(sender, "%s%s", ChatColor.DARK_PURPLE, t.name());
			msg(sender, "%s%s", ChatColor.GRAY, t.description);
			
			Map<String, String> targs = t.handler.newInstance().getDefinedArguments();
			
			for (Entry<String, String> kv : targs.entrySet())
			{
				boolean optional = kv.getKey().endsWith("?");
				String key = optional ? kv.getKey().substring(0, kv.getKey().length() - 1) : kv.getKey();
				
				msg(sender, "%s   %s%s - %s", 
						optional ? ChatColor.YELLOW : ChatColor.GOLD, key, ChatColor.GRAY, 
						kv.getValue() + (optional ? "(optional)" : ""));
			}
		}
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:spawn|s) debug [filter]",
		description = "Sends you info about the specific filtered entity spawning or despawning.\nLeave filter empty to cancel logging.",
		permission = "aperf.cmd.entity.spawn.debug"
	)
	public void debug(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		String filter = args.get("filter");
		
		if (filter == null || filter.equals(""))
		{
			Filter f = SpawnLimiterModule.instance.eventLoggers.remove(sender);
			
			if (f == null)
				msg(sender, "%sYou aren't logging debug messages.", ChatColor.GREEN);
			else
				msg(sender, "%sStopped.", ChatColor.GREEN);
		}
		else
		{
			Filter f = new Filter(args.get("filter"));
	
			SpawnLimiterModule.instance.eventLoggers.put(sender, f);
			msg(sender, "%sDebug logging started. Enter '/ap e s debug' to stop.", ChatColor.GREEN);
		}
	}
}
