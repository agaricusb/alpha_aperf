package ee.lutsu.alpha.mc.aperf.sys.entity;

import java.util.ArrayList;

import com.google.common.base.Optional;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;

public class EntityModule extends ModuleBase
{
	public static EntityModule instance = new EntityModule();
	
	public EntityModule()
	{
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.entity.cmd.EntityList());
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.entity.cmd.EntityRemoving());
		
		visible = false;
	}

	public int removeEntities(Optional<Integer> dim, Optional<String> groupFilter, Optional<String> classFilter, Optional<String> longClassFilter, Optional<String> nameFilter, Entity centered, int range)
	{
		int removed = 0;
		int distSq = range * range;
		for (World w : MinecraftServer.getServer().worldServers)
		{
			if (dim.isPresent() && w.provider.dimensionId != dim.get())
				continue;
			
			ArrayList<Entity> toRemove = new ArrayList<Entity>();
			for (Object o : w.loadedEntityList)
			{
				Entity ent = (Entity)o;
				if (ent instanceof EntityPlayer)
					continue;
				
				if (centered != null && range >= 0 && ent.getDistanceSqToEntity(centered) < distSq)
					continue;

				if ((!groupFilter.isPresent() || groupFilter.get().equalsIgnoreCase(EntityHelper.getEntityType(ent))) &&
					(!classFilter.isPresent() || classFilter.get().equalsIgnoreCase(ent.getClass().getSimpleName())) &&
					(!longClassFilter.isPresent() || longClassFilter.get().equalsIgnoreCase(ent.getClass().getName())) &&
					(!nameFilter.isPresent() || nameFilter.get().equalsIgnoreCase(EntityHelper.getEntityName(ent))))
					toRemove.add(ent);
			}
			
			for (Entity e : toRemove)
				EntityHelper.removeEntity(e);
			
			removed += toRemove.size();
		}
		
		return removed;
	}
}
