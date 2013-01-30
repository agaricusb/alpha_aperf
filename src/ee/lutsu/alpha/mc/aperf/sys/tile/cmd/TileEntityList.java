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
import ee.lutsu.alpha.mc.aperf.sys.objects.Filter;
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
			"Filter: use /ap filterhelp\n",
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
				"Filter: use /ap filterhelp\n",
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
		syntax = "(?:tile|t) (?:listaround|la) <radius> [group] [filter] [limit]",
		description = "Lists the tiles around you\n" +
			"Radius: number of blocks around you\n" +
			"Group: group/name/class/lclass/where/pos\n" +
			"Filter: use /ap filterhelp\n",
		permission = "aperf.cmd.tile.listaround",
		isPlayerOnly = true
	)
	public void listaround(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		EntityPlayer p = (EntityPlayer)sender;
		
		if (args == null)
			args = new HashMap<String, String>();
		
		String filter = args.get("filter");
		filter = filter != null && filter.length() > 0 ? filter + ";" : "";
		int radius = Integer.parseInt(args.get("radius"));
		
		filter = filter + String.format("d:%d,p:%d.%d.%d/%d.%d.%d", p.dimension, 
				(int)p.posX - radius, (int)p.posY - radius, (int)p.posZ - radius,
				(int)p.posX + radius, (int)p.posY + radius, (int)p.posZ + radius);
		args.put("filter", filter);
		
		list(plugin, sender, args);
	}

	@Command(
		name = "aperf",
		syntax = "(?:tile|t) (?:list|l) [group] [filter] [limit]",
		description = "Lists the tiles\n" +
			"Group: group/name/class/lclass/where/pos\n" +
			"Filter: use /ap filterhelp\n",
		permission = "aperf.cmd.tile.list"
	)
	public void list(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception 
	{
		Integer limitCnt = null, limitStart = null;
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
		Filter filter = null;
		
		if (args != null && args.get("filter") != null && args.get("filter").length() > 0)
			filter = new Filter(args.get("filter"));
		
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
		final Filter iFilter = filter;
		
		sendWorldGroupedList("Tile list grouped by " + sGrp + (iFilter != null ? ", filtered by " + iFilter.serializeDisplay() : "")
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
					if (iFilter != null && !iFilter.hitsAll(ent))
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
