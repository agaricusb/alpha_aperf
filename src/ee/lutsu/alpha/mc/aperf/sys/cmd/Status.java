package ee.lutsu.alpha.mc.aperf.sys.cmd;

import java.util.Map;

import org.bukkit.ChatColor;

import com.google.common.collect.ImmutableSetMultimap;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

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
		String format = "%s%3s | %s%5s%s | %7s | %8s | %5s | %6s | %6s";
		
		msg(sender, format, ChatColor.DARK_GREEN, " #", "", "Tick", "", "Players", "Entities", "Tiles", "Chunks", "Forced");
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
		
		
		
		double norm_tick_ms = 50; // 20 tps
		for (WorldServer w : MinecraftServer.getServer().worldServers)
		{
			int dim = w.provider.dimensionId;
			long[] ticks = MinecraftServer.getServer().worldTickTimes.get(dim);
			double average_ms = MathHelper.average(ticks) * 1.0E-6;
			int prc_tick = (int)(norm_tick_ms * 100 / average_ms);
			
			ImmutableSetMultimap<ChunkCoordIntPair, Ticket> forced = w.getPersistentChunks();

			msg(sender, format, ChatColor.GREEN,
					dim,
					prc_tick >= 100 ? ChatColor.DARK_GREEN : prc_tick >= 50 ? ChatColor.GOLD : ChatColor.RED,
					(prc_tick > 999 ? ">" : "") + String.valueOf(prc_tick > 999 ? 999 : prc_tick) + "%", ChatColor.GREEN,
					w.playerEntities.size(),
					w.loadedEntityList.size(),
					w.loadedTileEntityList.size(),
					w.getChunkProvider().getLoadedChunkCount(),
					forced.keySet().size()
					);
		}
		
		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
		
	}
}
