package ee.lutsu.alpha.mc.aperf.sys.tile.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import com.google.common.base.Optional;

import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;
import ee.lutsu.alpha.mc.aperf.commands.CommandException;
import ee.lutsu.alpha.mc.aperf.sys.tile.TileEntityHelper;

public class TileEntityList extends BaseCommand
{
	@Command(
		name = "aperf",
		syntax = "(?:tile|t)",
		description = "Lists the tile types and counts",
		isPrimary = true,
		permission = "aperf.cmd.tile.list"
	)
	public void entry(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		list(plugin, sender, null);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:tile|t) (?:listhere|lh) [group] [filter] [limit]",
		description = "Lists the tiles at your chunk\n" +
			"Group: group/name/class/lclass/where/pos\n" +
			"Filter: group:s,name:s,class:s,lclass:s,hash:s",
		permission = "aperf.cmd.tile.listhere",
		isPlayerOnly = true
	)
	public void listhere(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		EntityPlayer p = (EntityPlayer)sender;
		
		if (args == null)
			args = new HashMap<String, String>();
		
		args.put("radius", "0");

		listnearhere(plugin, sender, args);
	}
	
	@Command(
			name = "aperf",
			syntax = "(?:tile|t) (?:listnearhere|lnh) <radius> [group] [filter] [limit]",
			description = "Lists the tiles near your chunk\n" +
				"Group: group/name/class/lclass/where/pos\n" +
				"Filter: group:s,name:s,class:s,lclass:s,hash:s",
			permission = "aperf.cmd.tile.listnearhere",
			isPlayerOnly = true
	)
	public void listnearhere(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		EntityPlayer p = (EntityPlayer)sender;
		
		if (args == null)
			args = new HashMap<String, String>();
		
		String filter = args.get("filter");
		filter = filter != null && filter.length() > 0 ? filter + ";" : "";
		int radius = Integer.parseInt(args.get("radius"));
		
		filter = filter + String.format("d:%d,w:%d.%d/%d.%d", p.dimension, p.chunkCoordX - radius, p.chunkCoordZ - radius, p.chunkCoordX + radius, p.chunkCoordZ + radius);
		args.put("filter", filter);
		
		list(plugin, sender, args);
	}
	
	@Command(
		name = "aperf",
		syntax = "(?:tile|t) (?:list|l) [group] [filter] [limit]",
		description = "Lists the tiles\n" +
			"Group: group/name/class/lclass/where/pos\n" +
			"Filter: group:s,name:s,class:s,lclass:s,dimension:n,where:n.n[/n.n],hash:s",
		permission = "aperf.cmd.tile.list"
	)
	public void list(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		String fName = null, fGroup = null, fClass = null, fLClass = null, fHash = null;
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
			else if (group.startsWith("p"))
				grp = 5;
			else
				throw new CommandException("Unknown grouping");
		}
		
		String sGrp = grp == 0 ? "Group type" : grp == 1 ? "Name" : grp == 2 ? "Class name" : grp == 3 ? "Long Class name" : grp == 4 ? "Where (location)" : grp == 5 ? "Position" : "-";
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
					String[] parts = splits[1].split("/");
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
				else if (splits[0].toLowerCase().startsWith("h"))
					fHash = splits[1];
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
		final Optional<String> iHashFilter = Optional.fromNullable(fHash);
		
		sendWorldGroupedList("Tile list grouped by " + sGrp + (sFilter != null ? ", filtered by " + sFilter : "")
				+ (limitCnt != null ? ", limited by " + args.get("limit").trim() : ""), sender, 
			new IListForObject<WorldServer>()
			{
				@Override
				public List list(WorldServer obj)
				{
					return obj.loadedTileEntityList;
				}
			},
			new IGrouper<TileEntity>()
			{
				@Override
				public String group(TileEntity ent)
				{
					// filter
					if ((iGroupFilter.isPresent() && !iGroupFilter.get().equalsIgnoreCase(TileEntityHelper.getEntityType(ent))) ||
							(iClassFilter.isPresent() && !iClassFilter.get().equalsIgnoreCase(ent.getClass().getSimpleName())) ||
							(iLongClassFilter.isPresent() && !iLongClassFilter.get().equalsIgnoreCase(ent.getClass().getName())) ||
							(iNameFilter.isPresent() && !iNameFilter.get().equalsIgnoreCase(TileEntityHelper.getEntityName(ent).replaceAll(" ", "_"))) ||
							(iDim.isPresent() && iDim.get() != ent.worldObj.provider.dimensionId) ||
							(iW1.isPresent() && !iW3.isPresent() && (iW1.get() != (ent.xCoord >> 4) || iW2.get() != (ent.zCoord >> 4))) ||
							(iW1.isPresent() && iW3.isPresent() && (iW1.get() > (ent.xCoord >> 4) || iW2.get() > (ent.zCoord >> 4) || iW3.get() < (ent.xCoord >> 4) || iW4.get() < (ent.zCoord >> 4))) ||
							(iHashFilter.isPresent() && !iHashFilter.get().equalsIgnoreCase(Integer.toHexString(System.identityHashCode(ent)))))
							return null;
					
					// group
					if (iGrp == 1)
						return TileEntityHelper.getEntityName(ent);
					else if (iGrp == 2)
						return ent.getClass().getSimpleName();
					else if (iGrp == 3)
						return ent.getClass().getName();
					else if (iGrp == 4)
						return String.format("%d:%d [%d:%d]", (ent.xCoord >> 4), (ent.zCoord >> 4), ((ent.xCoord >> 4) << 4) + 8, ((ent.zCoord >> 4) << 4) + 8);
					else if (iGrp == 5)
						return String.format("%s/%s @ %d,%d,%d", TileEntityHelper.getEntityName(ent), Integer.toHexString(System.identityHashCode(ent)), (int)ent.xCoord, (int)ent.yCoord, (int)ent.zCoord);
					else
						return TileEntityHelper.getEntityType(ent);
				}
			}, limitStart, limitCnt);
	}
}
