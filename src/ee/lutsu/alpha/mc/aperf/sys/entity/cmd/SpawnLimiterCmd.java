package ee.lutsu.alpha.mc.aperf.sys.entity.cmd;

import java.util.Map;

import org.bukkit.ChatColor;

import net.minecraft.src.ICommandSender;
import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterModule;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterLimit;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterLimit.LimitFilterType;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterLimit.LimitRange;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterLimit.LimitType;

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
		String format = "%s%s | %s%-6s%s | %-6s | %-3s | %3s | %-4s | %6s | %s";
		
		msg(sender, format, ChatColor.DARK_GREEN, "#", "", "Active", "", "Range", "Ext", "Dim", "Type", "Filter", "Limit");
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
		
		int i = 1;
		for (SpawnLimiterLimit limit : SpawnLimiterModule.instance.limits)
		{
			msg(sender, format, ChatColor.GREEN,
				i++,
				limit.active ? ChatColor.DARK_GREEN : ChatColor.RED,
				limit.active ? "on" : "off", ChatColor.GREEN,
				limit.range.name(),
				limit.rangeExt,
				limit.allDims ? "all" : limit.dimension,
				limit.type.name(),
				limit.limitName,
				limit.filterStr());
		}
		
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:spawn|s) (?:add|a) <range> <ext> <dim> <type> <filter> <limit> [active]",
		description = "Add a new spawn limit\n" +
				"Range: Server|Map|Chunk, Ext: chunk radius, Dim: dimension/all\n" +
				"Type: All|Group|Class|LClass|Name\n" +
				"Filter: Filter appropriate for the type, Active: on|off\n" +
				"Limit: Count-n/ClearAbove-n[:n]/WaterAbove-n[:n]/BlockBelow-n[:n]",
		permission = "aperf.cmd.entity.spawn.add"
	)
	public void add(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		String active = args.get("active");
		SpawnLimiterLimit limit = new SpawnLimiterLimit();
		
		limit.range = LimitRange.get(args.get("range"));
		limit.rangeExt = Integer.valueOf(args.get("ext"));
		limit.allDims = args.get("dim").equalsIgnoreCase("all");
		limit.dimension = limit.allDims ? 0 : Integer.valueOf(args.get("dim"));
		limit.type = LimitType.get(args.get("type"));
		limit.limitName = args.get("filter");
		limit.active = active == null ? true : active.equalsIgnoreCase("active") || active.equalsIgnoreCase("yes") || active.equalsIgnoreCase("on") || active.equalsIgnoreCase("1");
		
		String[] sp = args.get("limit").split("-");
		limit.filterType = LimitFilterType.get(sp[0]);
		
		if (limit.filterType == LimitFilterType.BlockBelow || limit.filterType == LimitFilterType.ClearAbove || limit.filterType == LimitFilterType.WaterAbove)
		{
			String[] sp2 = sp[1].split(":");
			limit.limitInt = Integer.parseInt(sp2[0]);
			
			if (sp2.length > 1)
				limit.limitInt2 = Integer.parseInt(sp2[1]);
		}
		else
		{
			limit.limitInt = Integer.parseInt(sp[1]);
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

		SpawnLimiterLimit limit = SpawnLimiterModule.instance.limits.remove(index);
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

		SpawnLimiterLimit limit = SpawnLimiterModule.instance.limits.get(index);
		if (limit == null)
		{
			msg(sender, "%sLimit not found", ChatColor.YELLOW);
			return;
		}
		
		limit.active = !limit.active;
		
		SpawnLimiterModule.instance.saveConfig();
		msg(sender, "%sLimit toggled", ChatColor.GREEN);

		list(plugin, sender, null);
	}
}
