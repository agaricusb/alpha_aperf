package ee.lutsu.alpha.mc.aperf.sys.entity.cmd;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityAnimal;
import net.minecraft.src.EntityFireball;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityTNTPrimed;
import net.minecraft.src.IAnimals;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.IMob;
import net.minecraft.src.INpc;
import net.minecraft.src.IProjectile;
import net.minecraft.src.WorldServer;

import org.bukkit.ChatColor;

import com.google.common.base.Optional;

import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.commands.CommandException;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntityHelper;

public class EntityList extends BaseCommand
{
	@Command(
		name = "aperf",
		syntax = "(?:entity|e)",
		description = "Lists the entity types and counts",
		isPrimary = true,
		permission = "aperf.cmd.entity.list"
	)
	public void entry(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		list(plugin, sender, null);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:entity|e) (?:list|l) [group] [filter] [limit]",
		description = "Groups the entities by their class name\n" +
			"Group: group/name/class/lclass/where\n" +
			"Filter: group:s,name:s,class:s,lclass:s,dimension:n,where:n.n[-n.n]",
		permission = "aperf.cmd.entity.list"
	)
	public void list(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		String fName = null, fGroup = null, fClass = null, fLClass = null;
		Integer dim = null, w1 = null, w2 = null, w3 = null, w4 = null, limitCnt = null, limitStart = null;
		int grp = 0;
		
		if (args != null && args.get("group") != null && args.get("group").length() > 0)
		{
			String group = args.get("group").toLowerCase();
			if (group.startsWith("g"))
				grp = 0;
			else if (group.startsWith("n"))
				grp = 1;
			else if (group.startsWith("c"))
				grp = 2;
			else if (group.startsWith("l"))
				grp = 3;
			else if (group.startsWith("w"))
				grp = 4;
			else
				throw new CommandException("Unknown grouping");
		}
		
		String sGrp = grp == 0 ? "Group type" : grp == 1 ? "Name" : grp == 2 ? "Class name" : grp == 3 ? "Long Class name" : grp == 4 ? "Where (location)" : "-";
		String sFilter = null;
		
		if (args != null && args.get("filter") != null && args.get("filter").length() > 0)
		{
			String[] filter = args.get("filter").trim().split(",");
			sFilter = args.get("filter").trim();
			
			for (String f : filter)
			{
				if (f.equals(""))
					continue;
				
				String[] splits = f.split(":");
				
				if (splits[0].toLowerCase().startsWith("d"))
					dim = Integer.parseInt(splits[1]);
				else if (splits[0].toLowerCase().startsWith("c"))
					fClass = splits[1];
				else if (splits[0].toLowerCase().startsWith("l"))
					fLClass = splits[1];
				else if (splits[0].toLowerCase().startsWith("n"))
					fName = splits[1];
				else if (splits[0].toLowerCase().startsWith("g"))
					fGroup = splits[1];
				else if (splits[0].toLowerCase().startsWith("w"))
				{
					String[] parts = splits[1].split("-");
					String[] p1 = parts[0].split("\\.");
					w1 = Integer.valueOf(p1[0]);
					w2 = Integer.valueOf(p1[1]);
					if (parts.length > 1)
					{
						String[] p2 = parts[1].split("\\.");
						w3 = Integer.valueOf(p2[0]);
						w4 = Integer.valueOf(p2[1]);
					}
				}
				else
					throw new Exception("Unknown filter");
			}
		}
		
		if (args != null && args.get("limit") != null && args.get("limit").trim().length() > 0)
		{
			String[] splits = args.get("limit").trim().split("-");
			if (splits.length > 1)
			{
				limitStart = Integer.valueOf(splits[0]);
				limitCnt = Integer.valueOf(splits[1]);
			}
			else
			{
				limitStart = 0;
				limitCnt = Integer.valueOf(splits[0]);
			}
		}
		
		final int iGrp = grp;
		final Optional<Integer> iDim = Optional.fromNullable(dim); 
		final Optional<String> iGroupFilter = Optional.fromNullable(fGroup);
		final Optional<String> iClassFilter = Optional.fromNullable(fClass);
		final Optional<String> iLongClassFilter = Optional.fromNullable(fLClass);
		final Optional<String> iNameFilter = Optional.fromNullable(fName);
		final Optional<Integer> iW1 = Optional.fromNullable(w1);
		final Optional<Integer> iW2 = Optional.fromNullable(w2);
		final Optional<Integer> iW3 = Optional.fromNullable(w3);
		final Optional<Integer> iW4 = Optional.fromNullable(w4);
		
		sendWorldGroupedList("Entity list grouped by " + sGrp + (sFilter != null ? ", filtered by " + sFilter : "")
				+ (limitCnt != null ? ", limited by " + args.get("limit").trim() : ""), sender, 
			new IListForObject<WorldServer>()
			{
				@Override
				public List list(WorldServer obj)
				{
					return obj.loadedEntityList;
				}
			},
			new IGrouper<Entity>()
			{
				@Override
				public String group(Entity ent)
				{
					// filter
					if ((iGroupFilter.isPresent() && !iGroupFilter.get().equalsIgnoreCase(EntityHelper.getEntityType(ent))) ||
							(iClassFilter.isPresent() && !iClassFilter.get().equalsIgnoreCase(ent.getClass().getSimpleName())) ||
							(iLongClassFilter.isPresent() && !iLongClassFilter.get().equalsIgnoreCase(ent.getClass().getName())) ||
							(iNameFilter.isPresent() && !iNameFilter.get().equalsIgnoreCase(EntityHelper.getEntityName(ent))) ||
							(iDim.isPresent() && iDim.get() != ent.dimension) ||
							(iW1.isPresent() && !iW3.isPresent() && (iW1.get() != ent.chunkCoordX || iW2.get() != ent.chunkCoordZ)) ||
							(iW1.isPresent() && iW3.isPresent() && (iW1.get() > ent.chunkCoordX || iW2.get() > ent.chunkCoordZ || iW3.get() < ent.chunkCoordX || iW4.get() < ent.chunkCoordZ)))
							return null;
					
					// group
					if (iGrp == 1)
						return EntityHelper.getEntityName(ent);
					else if (iGrp == 2)
						return ent.getClass().getSimpleName();
					else if (iGrp == 3)
						return ent.getClass().getName();
					else if (iGrp == 4)
						return String.format("%d:%d [%d:%d]", ent.chunkCoordX, ent.chunkCoordZ, (ent.chunkCoordX << 4) + 8, (ent.chunkCoordZ << 4) + 8);
					else
						return EntityHelper.getEntityType(ent);
				}
			}, limitStart, limitCnt);
	}
}
