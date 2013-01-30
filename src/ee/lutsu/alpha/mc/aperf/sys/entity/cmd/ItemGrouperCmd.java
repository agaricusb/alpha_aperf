package ee.lutsu.alpha.mc.aperf.sys.entity.cmd;

import java.util.Map;

import net.minecraft.command.ICommandSender;

import org.bukkit.ChatColor;

import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.commands.CommandException;
import ee.lutsu.alpha.mc.aperf.sys.entity.ItemGrouperModule;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterModule;
import ee.lutsu.alpha.mc.aperf.sys.objects.SpawnLimit;

public class ItemGrouperCmd extends BaseCommand
{
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:group|g)",
		description = "Shows the status of item grouper",
		permission = "aperf.cmd.entity.group.status"
	)
	public void status(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		ItemGrouperModule m = ItemGrouperModule.instance;

		msg(sender, "%sItem grouper status", ChatColor.DARK_GREEN);
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);

		msg(sender, "%sItem grouping     [%s1%s]: %s%3s%s, XP orb grouping      [%s2%s]: %s%3s", ChatColor.GREEN,
				ChatColor.DARK_PURPLE, ChatColor.GREEN,
				m.groupItems ? ChatColor.DARK_GREEN : ChatColor.RED,
				m.groupItems ? "on" : "off", ChatColor.GREEN,
				ChatColor.DARK_PURPLE, ChatColor.GREEN,
				m.groupExpOrbs ? ChatColor.DARK_GREEN : ChatColor.RED,
				m.groupExpOrbs ? "on" : "off");
		
		msg(sender, "%sMatch range       [%s3%s]: %s%s%s, Move to new location [%s4%s]: %s%3s", ChatColor.GREEN,
				ChatColor.DARK_PURPLE, ChatColor.GREEN,
				ChatColor.DARK_GREEN, m.matchRange, ChatColor.GREEN,
				ChatColor.DARK_PURPLE, ChatColor.GREEN,
				ChatColor.DARK_GREEN, m.moveToNewLocation ? "yes" : "no ");
		
		msg(sender, "%sLived for atleast [%s5%s]: %s%3d%s, Run every x'th tick  [%s6%s]: %s%3d", ChatColor.GREEN,
				ChatColor.DARK_PURPLE, ChatColor.GREEN,
				ChatColor.DARK_GREEN, m.livedAtleast, ChatColor.GREEN,
				ChatColor.DARK_PURPLE, ChatColor.GREEN,
				ChatColor.DARK_GREEN, m.skipForTicks);
		
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:group|g) (?:set|s) <property> <value>",
		description = "Set the grouping property [n] -> int/float/(yes/no)",
		permission = "aperf.cmd.entity.group.set"
	)
	public void set(Object plugin, ICommandSender sender, Map<String, String> args) throws CommandException 
	{
		int property = Integer.valueOf(args.get("property"));
		String value = args.get("value");
		
		ItemGrouperModule m = ItemGrouperModule.instance;
		if (property == 1)
			m.groupItems = value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("on");
		else if (property == 2)
			m.groupExpOrbs = value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("on");
		else if (property == 3)
			m.matchRange = Double.valueOf(value);
		else if (property == 4)
			m.moveToNewLocation = value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("on");
		else if (property == 5)
			m.livedAtleast = Integer.valueOf(value);
		else if (property == 6)
			m.skipForTicks = Integer.valueOf(value);
		else
			throw new CommandException("Unknown ");
		
		m.saveConfig();
		
		msg(sender, "%sProperty set", ChatColor.GREEN);
		status(plugin, sender, null);
	}
}
