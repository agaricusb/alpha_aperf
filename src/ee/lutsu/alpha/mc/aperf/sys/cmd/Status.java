package ee.lutsu.alpha.mc.aperf.sys.cmd;

import java.util.Map;

import net.minecraft.src.ICommandSender;
import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;

public class Status extends BaseCommand 
{
	@Command(
		name = "aperf",
		syntax = "(?:status|s)",
		description = "Shows the general server status",
		isPrimary = true,
		permission = "aperf.cmd.status"
	)
	public void status(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
	}
}
