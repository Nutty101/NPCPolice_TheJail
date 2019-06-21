package net.livecar.nuttyworks.thejail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import net.citizensnpcs.Citizens;
import net.livecar.nuttyworks.npc_police.NPCPolice_Plugin;
import net.livecar.nuttyworks.thejail.bridges.MCUtils_1_12_R1;
import net.livecar.nuttyworks.thejail.bridges.MCUtils_1_13_R1;
import net.livecar.nuttyworks.thejail.bridges.MCVersionBridge;
import net.livecar.nuttyworks.thejail.citizens.CitizensUtils;
import net.livecar.nuttyworks.thejail.citizens.TheJailQuesterTrait;
import net.livecar.nuttyworks.thejail.citizens.TheJailTargetTrait;
import net.livecar.nuttyworks.thejail.database.DatabaseManager;
import net.livecar.nuttyworks.thejail.listeners.BukkitEvents;
import net.livecar.nuttyworks.thejail.listeners.BungeeCordListener;
import net.livecar.nuttyworks.thejail.listeners.NPCPoliceEvents;
import net.livecar.nuttyworks.thejail.listeners.commands.*;
import net.livecar.nuttyworks.thejail.messages.MessagesManager;
import net.livecar.nuttyworks.thejail.playerdata.PlayerScores;
import net.livecar.nuttyworks.thejail.messages.LanguageManager;
import net.livecar.nuttyworks.thejail.playerdata.PlayerMission;
import net.livecar.nuttyworks.thejail.settings.TheJailSettings;
import net.livecar.nuttyworks.thejail.thirdpartyplugins.holographicdisplays.HolographicDisplaysPlugin;
import net.livecar.nuttyworks.thejail.thirdpartyplugins.placeholder.PlaceHolderPlugin;
import net.livecar.nuttyworks.thejail.utilities.yamltools;

import net.milkbowl.vault.permission.Permission;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

public class TheJail_Plugin extends org.bukkit.plugin.java.JavaPlugin implements org.bukkit.event.Listener
{
	//Configuration file reference
	public FileConfiguration 				getDefaultConfig		= null;

    // variables
    public String                           currentLanguage         = "en_def";
    public Level                            debugLogLevel           = Level.OFF;
    public int                              version                 = 10000;

    // Storage locations
    public File                             storagePath;
    public File                             languagePath;
    public File                             loggingPath;

	//Internal references
    public BungeeCordListener               getBungeeListener       = null;
    public CitizensUtils					getCitizensUtils 		= null;
    public CommandManager                   getCommandManager 		= null;
    public DatabaseManager 					getDatabaseManager 		= null;
    public LanguageManager                  getLanguageManager      = null;
    public MessagesManager                  getMessageManager       = null;
    public PlayerScores                     getPlayerScores         = null;
    public TheJailSettings                  getSettings             = null;
    public MCVersionBridge                  getMCVersionUtils       = null;
    public HolographicDisplaysPlugin        getHolographicDisplays  = null;

    //Plugin References
	public Citizens 						getCitizensPlugin		= null;
	public NPCPolice_Plugin                 getNPCPolicePlugin		= null;
	public Permission 						getPermissionManager 	= null;
    public PlaceHolderPlugin 				getPlaceholderPlugin	= null;

	//Local Variables
	public Boolean                          onlyScores              = false;
	public HashMap<UUID, PlayerMission> 	playerStorage 			= null;

	public TheJail_Plugin()
	{
		getCitizensUtils = new CitizensUtils(this);
        getCommandManager = new CommandManager(this );
        getSettings = new TheJailSettings(this);
        playerStorage = new HashMap<>();
	}

	public void onLoad() {

        // Setup the default paths in the storage folder.
        storagePath = this.getDataFolder();
        languagePath = new File(this.getDataFolder(), "/Languages/");
        loggingPath = new File(this.getDataFolder(), "/Logs/");

        // Generate the default folders and files.
		try {
			getDefaultConfigs();
		} catch (IOException e) {
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			getServer().getLogger().log(Level.SEVERE, "encountered failure while loading the configuration. Plugin Disabled");
			return;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			getServer().getLogger().log(Level.SEVERE, "encountered failure while loading the configuration. Plugin Disabled");
			return;
		}

        if (this.getDefaultConfig.contains("debug")) {
            if ("OFF SEVERE WARNING INFO CONFIG FINE FINER FINEST ALL".contains(this.getDefaultConfig.getString("debug").toUpperCase())) {
                this.debugLogLevel = Level.parse(this.getDefaultConfig.getString("debug").toUpperCase());
            }
        }

        //Validate that NPC_Police is in the plugin list
		if (getServer().getPluginManager().getPlugin("NPC_Police") == null) {
			//if we are a mysql database, then this can function as a bridges for scored.
			if (!getDefaultConfig.getString("database.type", "sqlite").equals("mysql")) {
				getServer().getLogger().log(Level.SEVERE, "NPC_Police not found, this plugin requires NPC_Police, Citizens, and Sentinel to properly function.");
				getServer().getLogger().log(Level.INFO, "Please visit this plugin's page on Spigot for more information.");
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			} else {
				onlyScores = true;
				getServer().getLogger().log(Level.INFO, "NPC_Police not found, plugin operating as a score bridges");
			}
		}

        getLanguageManager = new LanguageManager(this);
        getMessageManager = new MessagesManager(this);

        // Get languages
        getLanguageManager.loadLanguages();

    }

	public void onEnable() {
        if (getLanguageManager == null)
            return;

        try {
            this.getSettings.loadSettings();
        } catch (Exception err) {
            getServer().getLogger().log(Level.SEVERE, "Failure loading settings.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getMCVersionUtils = new MCUtils_1_13_R1();

        new BukkitRunnable() {
            @Override
            public void run() {
                startPlugin();
            }
        }.runTask(this);

    }

    public void startPlugin()
    {

        if (!onlyScores) {
            // Register your trait with Citizens.
            net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(TheJailQuesterTrait.class).withName("thejailquester"));
            net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(TheJailTargetTrait.class).withName("thejailtarget"));

            // Register bungee class
            getBungeeListener = new BungeeCordListener(this);

            // Register events
            Bukkit.getPluginManager().registerEvents(this, this);
            Bukkit.getPluginManager().registerEvents(new BukkitEvents(this), this);
            Bukkit.getPluginManager().registerEvents(new NPCPoliceEvents(this), this);

            getCommandManager.registerCommandClass(CommandsAdmin.class);
            getCommandManager.registerCommandClass(CommandsSettings.class);
            getCommandManager.registerCommandClass(CommandsOnJoin.class);
            getCommandManager.registerCommandClass(CommandsOnFail.class);
            getCommandManager.registerCommandClass(CommandsOnComplete.class);
            getCommandManager.registerCommandClass(CommandsPenalties.class);

            // Init links to other plugins
            if (getServer().getPluginManager().getPlugin("Citizens") == null || getServer().getPluginManager().getPlugin("Citizens").isEnabled() == false || !(getServer().getPluginManager().getPlugin("Citizens") instanceof Citizens)) {
                getMessageManager.consoleMessage("console_messages.citizens_notfound", Level.SEVERE);
                getServer().getPluginManager().disablePlugin(this);
                return;
            } else {
                getCitizensPlugin = (Citizens) getServer().getPluginManager().getPlugin("Citizens");
                getMessageManager.consoleMessage("console_messages.citizens_found", getCitizensPlugin.getDescription().getVersion());
            }

        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getMessageManager.consoleMessage("console_messages.placeholder_notfound");
        } else {
            getMessageManager.consoleMessage("console_messages.placeholder_found", getServer().getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion());
            getPlaceholderPlugin = new PlaceHolderPlugin(this);
            getPlaceholderPlugin.register();
        }

        if (getServer().getPluginManager().getPlugin("HolographicDisplays") == null) {
            getMessageManager.consoleMessage("console_messages.holographicdisplays_notfound");
        } else {
            getMessageManager.consoleMessage("console_messages.holographicdisplays_found", getServer().getPluginManager().getPlugin("HolographicDisplays").getDescription().getVersion());
            getHolographicDisplays = new HolographicDisplaysPlugin(this);
            if (onlyScores)
                getHolographicDisplays.scoresOnlyRegister();
            else
                getHolographicDisplays.fullRegister();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                onStart();
            }
        }.runTask(this);
	}

	public void onStart()
	{
        //Start the database connection
        getDatabaseManager = new DatabaseManager(this);
        if (!getDatabaseManager.startDatabase())
            return;

        getPlayerScores = new PlayerScores(this);
        getPlayerScores.startMonitor();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] inargs) {

	    if (!this.isEnabled())
        {
            if (sender.isOp())
                sender.sendMessage("This plugin has been disabled. Please check the startup logs of your server for a reason as to why.");
            return true;
        }

		if (cmd.getName().equalsIgnoreCase("nptj") | cmd.getName().equalsIgnoreCase("thejail")) {
			return getCommandManager.onCommand(sender, inargs);
		}
		return true;
	}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String cmdLabel, String[] inargs) {
        if (cmd.getName().equalsIgnoreCase("nptj") | cmd.getName().equalsIgnoreCase("thejail")) {
            return getCommandManager.onTabComplete(sender, inargs);
        }
        return new ArrayList<String>();
    }

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = this.getServer().getServicesManager()
				.getRegistration(Permission.class);
		this.getPermissionManager = rsp.getProvider();
		return this.getPermissionManager != null;
	}

	public Boolean hasPermissions(CommandSender player, String permission) {
		if (player instanceof Player) {
			if (player.isOp())
				return true;

			if (permission.toLowerCase().startsWith("npcpolice.stats.") && player.hasPermission("npcpolice.stats.*"))
				return true;

			return player.hasPermission(permission);
		}
		return true;
	}


	private void getDefaultConfigs() throws IOException, InvalidConfigurationException {
        // Create the default folders
        if (!languagePath.exists())
            languagePath.mkdirs();
        if (!loggingPath.exists())
            loggingPath.mkdirs();

		if (!(new File(getDataFolder(), "config.yml").exists()))
			exportConfig(getDataFolder(), "config.yml");
        exportConfig(languagePath, "en_def-thejail.yml");

		this.getDefaultConfig = yamltools.loadConfiguration(new File(this.getDataFolder(), "config.yml"));

	}

	private void exportConfig(File path, String filename) {
		File fileConfig = new File(path, filename);
		if (!fileConfig.isDirectory()) {
			// Reader defConfigStream = null;
			try {
				FileUtils.copyURLToFile(getClass().getResource("/" + filename), fileConfig);
				// defConfigStream = new
				// InputStreamReader(this.getResource(filename), "UTF8");
			} catch (IOException e1) {
				Bukkit.getServer().getConsoleSender().sendMessage(" Failed to extract default file (" + filename + ")");
				return;
			}
		}
	}

}
