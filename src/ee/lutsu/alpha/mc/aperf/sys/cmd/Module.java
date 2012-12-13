package ee.lutsu.alpha.mc.aperf.sys.cmd;

import java.util.Map;

import org.bukkit.ChatColor;

import net.minecraft.src.ICommandSender;
import ee.lutsu.alpha.mc.aperf.aPerf;
import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;

public class Module extends BaseCommand
{
	@Command(
		name = "aperf",
		syntax = "(?:module|m)",
		description = "Module list",
		isPrimary = true,
		permission = "aperf.cmd.module.list"
	)
	public void list(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		String format = "%s%-6s%s | %s%-7s%s | %s";
		
		msg(sender, format, ChatColor.DARK_GREEN, "Active", "", "", "Enabled", "", "Name");
		msg(sender, "%s---------------------------------------------", ChatColor.GRAY);
		for (ModuleBase m : aPerf.instance.modules)
		{
			if (!m.isVisible())
				continue;
			
			msg(sender, format, m.isEnabled() ? ChatColor.DARK_GREEN : ChatColor.RED, 
				m.isEnabled() ? "yes" : "no", ChatColor.GREEN,
				aPerf.instance.isEnabled(m) ? ChatColor.DARK_GREEN : ChatColor.RED, 
				aPerf.instance.isEnabled(m) ? "yes" : "no", ChatColor.GREEN,
				m.getName());
		}
		
		msg(sender, "%s---------------------------------------------", ChatColor.GRAY);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:module|m) (?:set|switch|s) <name> <on> [enabled]",
		description = "Turn module on/off, if <enabled> is set, sets the parameters seperately",
		permission = "aperf.cmd.module.switch"
	)
	public void moduleSwitch(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		String name = args.get("name");
		String on = args.get("on");
		String enabled = args.get("enabled");
		
		boolean turnOn = on.equalsIgnoreCase("on") || on.equalsIgnoreCase("active") || on.equalsIgnoreCase("1") || on.equalsIgnoreCase("yes");
		boolean turnEnabled = enabled == null ? turnOn : enabled.equalsIgnoreCase("on") || enabled.equalsIgnoreCase("active") || enabled.equalsIgnoreCase("1") || enabled.equalsIgnoreCase("yes");
		
		ModuleBase module = null;
		for (ModuleBase m : aPerf.instance.modules)
		{
			if (m.isVisible() && m.getName().equalsIgnoreCase(name))
			{
				module = m;
				break;
			}
		}
		
		if (module == null)
			msg(sender, "Module not found", ChatColor.RED);
		else
		{
			aPerf.instance.setAutoLoad(module, turnEnabled);
			
			if (turnOn)
				module.enable();
			else
				module.disable();
			
			list(plugin, sender, null);
		}
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:module|m) (?:reload|r) <name>",
		description = "Reloads the specified module, including it's config",
		permission = "aperf.cmd.module.reload"
	)
	public void reload(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		String name = args.get("name");

		ModuleBase module = null;
		for (ModuleBase m : aPerf.instance.modules)
		{
			if (m.isVisible() && m.getName().equalsIgnoreCase(name))
			{
				module = m;
				break;
			}
		}
		
		if (module == null)
			msg(sender, "Module not found", ChatColor.RED);
		else
		{
			module.disable();
			module.enable();
			
			msg(sender, "Module reloaded", ChatColor.GREEN);
		}
	}
}
