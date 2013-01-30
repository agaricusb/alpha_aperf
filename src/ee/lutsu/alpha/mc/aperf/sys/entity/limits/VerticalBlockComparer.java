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

public abstract class VerticalBlockComparer extends SpawnLimit
{
	public int count;
	public int max;
	
	public boolean upwards = true;
	public int blockToFind;
	public Integer blockToFindSub = null;
	
	public boolean isSkyThisType = false;
	public boolean isVoidThisType = false;
	
	@Override
	public boolean canSpawn(Entity e, World world) 
	{
		int x = (int)e.posX;
		int y = (int)e.posY - (upwards ? 0 : 1);
		int z = (int)e.posZ;

		for (int nyadd = 0; nyadd < count; nyadd++)
		{
			int yadd = upwards ? nyadd : -nyadd;
			
			if (y + yadd >= 255)
			{
				if (isSkyThisType)
					break;
				else
					return false;
			}
			else if (y + yadd < 0)
			{
				if (isVoidThisType)
					break;
				else
					return false;
			}
			
			if (!isBlockCorrect(world, x, y + yadd, z))
				return false;
		}
		
		if (max != 0)
		{
			for (int nyadd = count; nyadd < count + max; nyadd++)
			{
				int yadd = upwards ? nyadd : -nyadd;
				
				if (y + yadd >= 255)
				{
					if (isSkyThisType)
						break;
					else
						return false;
				}
				else if (y + yadd < 0)
				{
					if (isVoidThisType)
						break;
					else
						return false;
				}
				
				if (!isBlockCorrect(world, x, y + yadd, z))
					return true;
			}
			return false;
		}
		
		return true;
	}
	
	protected boolean isBlockCorrect(World world, int x, int y, int z)
	{
		int type = world.getBlockId(x, y, z);
		
		if (type != blockToFind)
			return false;
		
		if (blockToFindSub != null)
		{
			int sub = world.getBlockMetadata(x, y, z);
			if (sub != blockToFindSub)
				return false;
		}
		
		return true;
	}
}
