package ee.lutsu.alpha.mc.aperf.sys.entity.cmd;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

import org.bukkit.ChatColor;

import com.google.common.base.Optional;

import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntityHelper;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntityModule;

public class EntityRemoving extends BaseCommand
{
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:remove|delete|r|d) <filter> [range]",
		description = "Removes entities by: Dimension/Group/Class/LClass/Name/All\n" +
			"Ex: /ap entity r G:animal,D:0 -1 - removes all animals from overworld\n" +
			"Ex: /ap entity r A -1 - removes all entities except players",
		permission = "aperf.cmd.entity.remove"
	)
	public void remove(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		String fName = null, fGroup = null, fClass = null, fLClass = null;
		Integer dim = null;
		String filter = args.get("filter");

		String[] sfilter = args.get("filter").trim().split(",");
		int range = args.get("range") != null && !args.get("range").equalsIgnoreCase("-a") && !args.get("range").equalsIgnoreCase("all") ? Integer.parseInt(args.get("range")) : -1;
		
		for (String f : sfilter)
		{
			if (f.equals(""))
				continue;
			
			String[] splits = f.split(":");
			
			if (splits[0].toLowerCase().startsWith("a")) ;
			else if (splits[0].toLowerCase().startsWith("d"))
				dim = Integer.parseInt(splits[1]);
			else if (splits[0].toLowerCase().startsWith("c"))
				fClass = splits[1];
			else if (splits[0].toLowerCase().startsWith("l"))
				fLClass = splits[1];
			else if (splits[0].toLowerCase().startsWith("n"))
				fName = splits[1];
			else if (splits[0].toLowerCase().startsWith("g"))
				fGroup = splits[1];
			else
				throw new Exception("Unknown filter");
		}
		
		Entity around = sender instanceof Entity ? (Entity)sender : null;
				
		int killed = EntityModule.instance.removeEntities(Optional.fromNullable(dim), Optional.fromNullable(fGroup), Optional.fromNullable(fClass), Optional.fromNullable(fLClass), Optional.fromNullable(fName), around, range);
		
		msg(sender, "%sKilled %d entities", ChatColor.GREEN, killed);
	}
}
