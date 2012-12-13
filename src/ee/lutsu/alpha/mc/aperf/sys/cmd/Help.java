package ee.lutsu.alpha.mc.aperf.sys.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICommandSender;
import ee.lutsu.alpha.mc.aperf.ChatColor;
import ee.lutsu.alpha.mc.aperf.Permissions;
import ee.lutsu.alpha.mc.aperf.aPerf;
import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.commands.CommandsManager.CommandBinding;

public class Help extends BaseCommand
{
	private static int helpCommandsPerPage = 4;
	
	@Command(
		name = "aperf",
		syntax = "(?:help|h) [page]",
		description = "Command list",
		isPrimary = true,
		permission = "aperf.cmd.help.list"
	)
	public void help(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		String arg = args.get("page");

		List<CommandBinding> commands = getCommands(sender);
		int pages = (int)Math.ceil((double)commands.size() / (double)helpCommandsPerPage);
		
		int page = arg == null ? 1 : Integer.parseInt(arg);
		if (page < 1) 
			page = 1;
		else if (page > pages)
			page = pages;

		msg(sender, "%saPerf commands. Page %s%s %sof %s%s", ChatColor.DARK_GREEN, 
				ChatColor.DARK_PURPLE, page, ChatColor.DARK_GREEN,
				ChatColor.DARK_PURPLE, pages);
		msg(sender, "%s----------------------------------", ChatColor.GRAY);
		
		boolean showPerm = sender instanceof EntityPlayer ? Permissions.canAccess((EntityPlayer)sender, "aperf.show.cmd.perm") : true;
		
		int i = 0;
		for (CommandBinding cmd : commands)
		{
			Command desc = cmd.getMethodAnnotation();
			i++;
			if (i <= (page - 1) * helpCommandsPerPage || i > page * helpCommandsPerPage)
				continue;
			
			String syntax = desc.syntax().replaceAll("\\(\\?\\:|\\)", "");
			
			msg(sender, "%s/%s %s", ChatColor.GREEN, desc.name(), syntax);
			
			for (String d : desc.description().split("\n"))
				msg(sender, "%s   %s", ChatColor.YELLOW, d);
			
			if (showPerm)
				msg(sender, "%s   Perm: %s%s", ChatColor.GOLD, ChatColor.YELLOW, desc.permission());
		}
		msg(sender, "%s----------------------------------", ChatColor.GRAY);
	}
	
	private List<CommandBinding> getCommands(ICommandSender user)
	{
		List<CommandBinding> commands = aPerf.instance.commandsManager.getCommands();
		ArrayList<CommandBinding> ret = new ArrayList<CommandBinding>();
		
		for(CommandBinding cmd : commands)
		{
			Command desc = cmd.getMethodAnnotation();
			if (desc.isPlayerOnly() && !(user instanceof EntityPlayer))
				continue;
			if (user instanceof EntityPlayer && !Permissions.canAccess((EntityPlayer)user, desc.permission()))
				continue;
			
			ret.add(cmd);
		}
		
		return ret;
	}
}
