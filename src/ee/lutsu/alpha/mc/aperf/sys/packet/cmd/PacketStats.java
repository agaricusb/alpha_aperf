package ee.lutsu.alpha.mc.aperf.sys.packet.cmd;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.commands.CommandException;
import ee.lutsu.alpha.mc.aperf.sys.packet.PacketManagerModule;

public class PacketStats extends BaseCommand
{
	
	@Command(
		name = "aperf",
		syntax = "(?:packet|p)",
		description = "Packet command menu",
		permission = "aperf.cmd.packet.commands",
		isPrimary = true
	)
	public void commands(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		msg(sender, "§4 Packet commands: ");
		msg(sender, "§5   limit(l) §7- Shows your status and limits");
		msg(sender, "§3      on  §7- activates your limits");
		msg(sender, "§3      off  §7- deactivates your limits");
		msg(sender, "§3      dup  §7- toggle duplicate packet sending mode");
		msg(sender, "§3      help  §7- Show detailed help on limits");
		msg(sender, "§3      clear §7- Deletes all limits");
		msg(sender, "§3      add(a) §3<channel> <id> <x> §7- Limits packets");
		
		msg(sender, "§5   stat(s) §7- Shows packet info");
		msg(sender, "§5   stat(s) §3<player> §7- Shows packet info for player");
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:packet|p) (?:stat|s)",
		description = "Starts/Shows packet collection info for you",
		permission = "aperf.cmd.packet.stat.self",
		isPlayerOnly = true
	)
	public void statsSelf(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		if (args == null)
			args = new HashMap<String, String>();
		
		args.put("name", sender.getCommandSenderName());
		
		stats(plugin, sender, args);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:packet|p) (?:stat|s) <name>",
		description = "Starts/Shows packet collection info for player",
		permission = "aperf.cmd.packet.stat.other"
	)
	public void stats(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		String name = args.get("name");
		
		
		EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(name);
		if (player == null)
			throw new CommandException("Player not found");
		
		ArrayList<SimpleEntry<Class, Double>> data = PacketManagerModule.Instance.getTestResultsAbstract(player);
		
		if (data != null)
		{
			Collections.sort(data, new Comparator<SimpleEntry<Class, Double>>()
			{
				@Override
				public int compare(SimpleEntry<Class, Double> arg0, SimpleEntry<Class, Double> arg1) 
				{
					return arg0.getValue().compareTo(arg1.getValue());
				}
			});
			
			Date runSince = PacketManagerModule.Instance.getTestRunStartTime(player);
			double runTime = (double)(new Date().getTime() - runSince.getTime()) / 1000;

			msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
			msg(sender, "§4General stats - run time %.2f seconds. Player %s", runTime, name);
			
			for(SimpleEntry<Class, Double> v : data)
			{
				msg(sender, "§3%8.2f b/s §a%s", v.getValue(), v.getKey().getName());
			}
			
			// custom
			ArrayList<SimpleEntry<SimpleEntry<String, Integer>, Double>> data3 = PacketManagerModule.Instance.getTestResults(player);
			
			if (data3 != null)
			{
				Collections.sort(data3, new Comparator<SimpleEntry<SimpleEntry<String, Integer>, Double>>()
				{
					@Override
					public int compare(SimpleEntry<SimpleEntry<String, Integer>, Double> arg0, SimpleEntry<SimpleEntry<String, Integer>, Double> arg1) 
					{
						return arg0.getValue().compareTo(arg1.getValue());
					}
				});

				msg(sender, "§4250 Custom Payload & 211 Tile Desc stats");
				
				for(SimpleEntry<SimpleEntry<String, Integer>, Double> v : data3)
				{
					msg(sender, "§3%8.2f b/s [§4%s-%s§a] %s", v.getValue(), v.getKey().getKey(), v.getKey().getValue(), PacketManagerModule.getPacketDesc(v.getKey().getKey(), v.getKey().getValue()));
				}
			}
			msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
			
			msg(sender, "§4Stopped §acollecting info. Enter the same command again to start again.");
			PacketManagerModule.Instance.endTest(player);
		}
		else
		{
			msg(sender, "§2Started §acollecting info. Enter the same command again to see results.");
			PacketManagerModule.Instance.startTest(player);
		}
	}

}
