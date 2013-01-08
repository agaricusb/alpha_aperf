package ee.lutsu.alpha.mc.aperf.sys.tile;

import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;

public class TileEntityModule extends ModuleBase
{
	public static TileEntityModule instance = new TileEntityModule();
	
	public TileEntityModule()
	{
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.tile.cmd.TileEntityList());

		visible = false;
	}
}
