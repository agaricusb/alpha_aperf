package ee.lutsu.alpha.mc.aperf.sys.packet.cmd;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.sys.packet.PacketManagerModule;

public class PacketLimits extends BaseCommand
{
	@Command(
		name = "aperf",
		syntax = "(?:packet|p) (?:limit|l) (?:add|a) <channel> <id> <x>",
		description = "Adds a packet limit\n" +
				"neg - skip every x'th. pos - allow every x'th. 0 - off",
		permission = "aperf.cmd.packet.limit.add",
		isPlayerOnly = true
	)
	public void add(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		EntityPlayer player = (EntityPlayer)sender;
		
		String tag = args.get("channel");
		int subid = Integer.parseInt(args.get("id"));
		byte occurence = Byte.parseByte(args.get("x"));
		
		if (subid < 0 || subid >= PacketManagerModule.Instance.subTypesCustom)
			throw new Exception(String.format("Id has to be between 0 and %s", PacketManagerModule.Instance.subTypesCustom));
		
		boolean on = PacketManagerModule.Instance.autoLoadLimits(player.username);
		PacketManagerModule.Instance.setLimit(player, tag, subid, occurence);
		
		msg(sender, "Packet limit §2added");

		if (!on)
			msg(sender, "Packet limits §4OFF");
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:packet|p) (?:limit|l) clear",
		description = "Removes all your limits",
		permission = "aperf.cmd.packet.limit.clear",
		isPlayerOnly = true
	)
	public void clear(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		EntityPlayer player = (EntityPlayer)sender;

		PacketManagerModule.Instance.removeLimits(player);
		msg(sender, "Packet limits are now §4removed§a and §4off");
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:packet|p) (?:limit|l) on",
		description = "Enable your packet limits",
		permission = "aperf.cmd.packet.limit.on",
		isPlayerOnly = true
	)
	public void switchOn(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		EntityPlayer player = (EntityPlayer)sender;

		PacketManagerModule.Instance.toggleLimits(player, true);
		msg(sender, "Packet limits are now §2ON");
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:packet|p) (?:limit|l) off",
		description = "Disable your packet limits",
		permission = "aperf.cmd.packet.limit.off",
		isPlayerOnly = true
	)
	public void switchOff(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		EntityPlayer player = (EntityPlayer)sender;

		PacketManagerModule.Instance.toggleLimits(player, false);
		msg(sender, "Packet limits are now §4OFF");
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:packet|p) (?:limit|l) dup",
		description = "Toggle duplicate packet sending mode",
		permission = "aperf.cmd.packet.limit.dup",
		isPlayerOnly = true
	)
	public void switchDuplicate(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		EntityPlayer player = (EntityPlayer)sender;
	
		int diggestActiveFlag = PacketManagerModule.Instance.getActiveDiggestFlag(player) + 1;
		if (diggestActiveFlag > 3)
			diggestActiveFlag = 0;
		
		PacketManagerModule.Instance.setDiggestLimit(player, diggestActiveFlag);
		msg(sender, "Diggest packet limits are now %s",
				diggestActiveFlag == 0 ? "§4OFF§a" : diggestActiveFlag == 1 ? "§ePLUGINS only§a" : diggestActiveFlag == 2 ? "§eMC only§a" : "§2ALL§a"
				);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:packet|p) (?:limit|l) help",
		description = "Show detailed help on limits",
		permission = "aperf.cmd.packet.limit.help"
	)
	public void help(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
		msg(sender, "§4Limit help");
		msg(sender, "§5Display syntax: [tag-id-x] desc");
		msg(sender, "§3    tag §7- The plugin channel, Case SeNsItIvE");
		msg(sender, "§3     id §7- The packet sub id");
		msg(sender, "§3      x §7- Skip setting. neg - skip every x'th. pos - allow every x'th. 0 - off");
		msg(sender, "§3   desc §7- Some description on which kind of packet it is");
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:packet|p) (?:limit|l)",
		description = "Shows your packet limits",
		permission = "aperf.cmd.packet.limit.list",
		isPlayerOnly = true
	)
	public void status(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		EntityPlayer player = (EntityPlayer)sender;

		int diggestActiveFlag = PacketManagerModule.Instance.getActiveDiggestFlag(player);
		
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
		msg(sender, "§aPacket limits are set to %s and are %s", 
				PacketManagerModule.Instance.autoLoadLimits(player.username) ? "§2ON§a" : "§4OFF§a",
						PacketManagerModule.Instance.areLimitsActive(player) ? "§2ACTIVE§a" : "§4INACTIVE§a"
				);
		msg(sender, "§aDiggest packet limits are %s", 
				diggestActiveFlag == 0 ? "§4OFF§a" : diggestActiveFlag == 1 ? "§ePLUGINS only§a" : diggestActiveFlag == 2 ? "§eMC only§a" : "§2ALL§a"
				);
		
		HashMap<String, byte[]> data = PacketManagerModule.Instance.getLimits(player);

		if (data == null)
		{
			msg(sender, "§4None defined");
		}
		else
		{
			for (String s : data.keySet())
			{
				byte[] dd = data.get(s);
				for (int i = 0; i < dd.length; i++)
				{
					byte d = dd[i];
					
					if (d != 0)
					{
						msg(sender, "§a[§4%s§a-§4%s§a-§4%s§a] %s", s, i, d, PacketManagerModule.getPacketDesc(s, i));
					}
				}
			}
		}
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
	}
}
