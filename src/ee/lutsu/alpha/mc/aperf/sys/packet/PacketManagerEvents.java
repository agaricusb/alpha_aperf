package ee.lutsu.alpha.mc.aperf.sys.packet;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.IPlayerTracker;

public class PacketManagerEvents implements IPlayerTracker
{

	@Override
	public void onPlayerLogin(EntityPlayer player)
	{
		PacketManagerModule.Instance.playerLogin(player);
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) 
	{
		PacketManagerModule.Instance.playerQuit(player);
	}

	
	
	@Override
	public void onPlayerChangedDimension(EntityPlayer player) 
	{
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) 
	{
	}

}
