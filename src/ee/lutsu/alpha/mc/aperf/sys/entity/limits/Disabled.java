package ee.lutsu.alpha.mc.aperf.sys.entity.limits;

import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ee.lutsu.alpha.mc.aperf.commands.CommandException;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntityHelper;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntitySafeListModule;
import ee.lutsu.alpha.mc.aperf.sys.objects.Filter;
import ee.lutsu.alpha.mc.aperf.sys.objects.SpawnLimit;

public class Disabled extends SpawnLimit
{
	@Override
	public boolean canSpawn(Entity e, World world) 
	{
		return false;
	}
}
