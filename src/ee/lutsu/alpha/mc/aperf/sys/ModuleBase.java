package ee.lutsu.alpha.mc.aperf.sys;

import java.util.ArrayList;
import java.util.List;

import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;

public abstract class ModuleBase
{
	protected boolean enabled;
	protected boolean visible = true;
	protected List<BaseCommand> commands = new ArrayList<BaseCommand>();
	
	public String getName() { return getClass().getSimpleName(); }
	public boolean isEnabled() { return enabled; }
	public boolean isVisible() { return visible; }
	
	public void enable()
	{
		disable();
		
		enabled = true;
	}
	
	public void disable()
	{
		enabled = false;
	}

	protected void addCommand(BaseCommand cmd)
	{
		commands.add(cmd);
	}
	
	public List<BaseCommand> getCommands() { return commands; }
	public boolean getDefaultEnabled() { return false; }
	public void loadConfig() { }
}
