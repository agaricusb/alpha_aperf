package ee.lutsu.alpha.mc.aperf.sys;

public class GeneralModule extends ModuleBase
{
	public static GeneralModule instance = new GeneralModule();
	
	public GeneralModule()
	{
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.cmd.Help());
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.cmd.Status());
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.cmd.Module());
		addCommand(new ee.lutsu.alpha.mc.aperf.sys.cmd.Mod());

		visible = false;
	}
}
