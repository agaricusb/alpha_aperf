package ee.lutsu.alpha.mc.aperf.sys.entity.cmd;

import java.util.Map;

import org.bukkit.ChatColor;

import net.minecraft.command.ICommandSender;
import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.commands.CommandException;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntitySafeListModule;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterModule;
import ee.lutsu.alpha.mc.aperf.sys.objects.Filter;
import ee.lutsu.alpha.mc.aperf.sys.objects.SpawnLimit;
import ee.lutsu.alpha.mc.aperf.sys.objects.SubFilter;

public class EntitySafeList extends BaseCommand
{
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) safe",
		description = "Shows the entity safelist",
		permission = "aperf.cmd.entity.safelist.show"
	)
	public void list(Object plugin, ICommandSender sender, Map<String, String> args)
	{
		String format = "%s%s | %s";
		
		msg(sender, format, ChatColor.DARK_GREEN, "#", "Filter");
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
		
		int i = 1;
		for (Filter f : EntitySafeListModule.instance.safeList)
		{
			msg(sender, format, ChatColor.GREEN,
				i++,
				f.serializeDisplay());
		}
		
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) safe (?:remove|r|delete|d) <index>",
		description = "Removes an entry from the safelist",
		permission = "aperf.cmd.entity.safelist.remove"
	)
	public void remove(Object plugin, ICommandSender sender, Map<String, String> args)
	{
		int index = Integer.parseInt(args.get("index")) - 1;

		Filter f = EntitySafeListModule.instance.safeList.remove(index);
		EntitySafeListModule.instance.saveConfig();
		
		if (f != null)
			msg(sender, "%sFilter removed", ChatColor.GREEN);
		else
			msg(sender, "%sFilter not found", ChatColor.YELLOW);
		
		list(plugin, sender, null);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) safe (?:add|a) <filter>",
		description = "Adds an entry to the safelist\n" +
			"Filter: use /ap filterhelp",
		permission = "aperf.cmd.entity.safelist.add"
	)
	public void add(Object plugin, ICommandSender sender, Map<String, String> args) throws CommandException
	{
		Filter f = null;
		try
		{
			f = new Filter(args.get("filter"));
		}
		catch (Exception e)
		{
			throw new CommandException(e.getMessage(), e);
		}

		EntitySafeListModule.instance.safeList.add(f);
		EntitySafeListModule.instance.saveConfig();
		
		msg(sender, "%sFilter added", ChatColor.GREEN);
		
		list(plugin, sender, null);
	}
}
