package ee.lutsu.alpha.mc.aperf;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
import ee.lutsu.alpha.mc.aperf.commands.CmdPerf;
import ee.lutsu.alpha.mc.aperf.commands.CommandsManager;
import ee.lutsu.alpha.mc.aperf.sys.GeneralModule;
import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;
import ee.lutsu.alpha.mc.aperf.sys.entity.EntityModule;
import ee.lutsu.alpha.mc.aperf.sys.entity.ItemGrouperModule;
import ee.lutsu.alpha.mc.aperf.sys.entity.SpawnLimiterModule;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.ServerCommandManager;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;

@Mod(
        modid = "aPerf",
        name = "aPerf",
        dependencies = "required-after:Forge@[6.0,);required-after:PermissionsEx@[1.0,)",
        version = "1.0.0"
)
@NetworkMod(
        clientSideRequired = false,
        serverSideRequired = true
)
public class aPerf
{
	public static String MOD_NAME = "aPerf";
	public File configFile;
	public Configuration config;
	public CommandsManager commandsManager = new CommandsManager(this);
	public ModuleBase[] modules = new ModuleBase[]
	{
		GeneralModule.instance,
		EntityModule.instance,
		SpawnLimiterModule.instance,
		ItemGrouperModule.instance
	};
	
    @Mod.Instance("aPerf")
    public static aPerf instance;

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent ev)
    {
        configFile = ev.getSuggestedConfigurationFile();
        
        reload();
    }

    @Mod.Init
    public void load(FMLInitializationEvent var1)
    {
    }

    @Mod.ServerStarted
    public void modsLoaded(FMLServerStartedEvent var1)
    {
    	for (ModuleBase m : modules)
    	{
    		for(BaseCommand cmd : m.getCommands())
    			commandsManager.register(cmd);
    	}

    	ServerCommandManager mgr = (ServerCommandManager)MinecraftServer.getServer().getCommandManager();
    	mgr.registerCommand(new CmdPerf());
    }
    
    public void loadConfig()
    {
    	config = new Configuration(configFile);

        try
        {
            config.load();
        }
        catch (Exception var8)
        {
            FMLLog.log(Level.SEVERE, var8, MOD_NAME + " was unable to load it's configuration successfully", new Object[0]);
            throw new RuntimeException(var8);
        }
        finally
        {
            config.save(); // re-save to add the missing configuration variables
        }
    }
    
    public boolean isEnabled(ModuleBase module)
    {
    	Property prop = config.get("Modules", "Enable-" + module.getClass().getSimpleName(), "false");
    	return prop.getBoolean(false);
    }
    
    public void setAutoLoad(ModuleBase module, boolean load)
    {
    	Property prop = config.get("Modules", "Enable-" + module.getClass().getSimpleName(), "false");
    	prop.value = String.valueOf(load);
    	config.save();
    }
    
    protected void enableModules()
    {
    	try
    	{
	    	for (ModuleBase m : modules)
	    	{
	            if (isEnabled(m))
	            	m.enable();
	    	}
    	}
    	finally
    	{
    		config.save();
    	}
    }
    
    protected void disableModules()
    {
    	for (ModuleBase m : modules)
    	{
    		if (m.isEnabled())
    			m.disable();
    	}
    }
    
    public void reload()
    {
    	loadConfig();
    	
    	try
    	{
    		disableModules();
    		enableModules();
    	}
    	catch(Exception ex)
    	{
    		Log.severe("Load failed");
    		throw new RuntimeException(ex.getMessage(), ex);
    	}
    	Log.info("Loaded");
    }
}
