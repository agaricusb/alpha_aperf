package ee.lutsu.alpha.mc.aperf.sys.cmd;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSetMultimap;

import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import ee.lutsu.alpha.mc.aperf.aPerf;
import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.Command;

public class Mod extends BaseCommand 
{
	@Command(
		name = "aperf",
		syntax = "(?:reload|r)",
		description = "Reloads the whole mod",
		isPrimary = true,
		permission = "aperf.cmd.reload"
	)
	public void reload(Object plugin, ICommandSender sender, Map<String, String> args) 
	{
		aPerf.instance.reload();
		msg(sender, "%sReloaded", ChatColor.GREEN);
	}
}
