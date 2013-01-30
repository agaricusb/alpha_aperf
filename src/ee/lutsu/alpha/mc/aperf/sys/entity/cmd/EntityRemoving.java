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
import ee.lutsu.alpha.mc.aperf.sys.objects.Filter;

public class EntityRemoving extends BaseCommand
{
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:remove|delete|r|d) <filter> [range]",
		description = "Removes entities\n" +
			"Filter: use /ap filterhelp\n" +
			"Ex: /ap e r g:animal,d:0 - removes all animals from overworld\n" +
			"Ex: /ap e r a 20 - removes all entities within 20 blocks spherical around you",
		permission = "aperf.cmd.entity.remove"
	)
	public void remove(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		Filter filter = new Filter(args.get("filter"));

		int range = args.get("range") != null && !args.get("range").equalsIgnoreCase("-a") && !args.get("range").equalsIgnoreCase("all") ? Integer.parseInt(args.get("range")) : -1;
		Entity around = sender instanceof Entity ? (Entity)sender : null;
		
		int killed = EntityModule.instance.removeEntities(filter, around, range);
		
		msg(sender, "%sKilled %d entities", ChatColor.GREEN, killed);
	}
}
