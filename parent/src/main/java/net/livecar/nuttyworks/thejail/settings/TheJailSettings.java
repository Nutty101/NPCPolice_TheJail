package net.livecar.nuttyworks.thejail.settings;

import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.COMPLETEDACTION;
import net.livecar.nuttyworks.thejail.enumerations.FAILACTION;
import net.livecar.nuttyworks.thejail.enumerations.FAILTRIGGERS;
import net.livecar.nuttyworks.thejail.enumerations.JOINACTION;
import net.livecar.nuttyworks.thejail.utilities.yamltools;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TheJailSettings {
    public List<JOINACTION>         onJoinActions = new ArrayList();
    public Location                 onJoinLocation = null;
    public List<String>             onJoinCommands = new ArrayList();

    public List<COMPLETEDACTION>    onCompletedActions = new ArrayList();
    public List<String>             onCompletedCommands = new ArrayList<>();
    public String                   onCompletedSend = "";
    public Location                 onCompletedLocation = null;

    public List<FAILACTION>         onFailActions = new ArrayList();
    public List<String>             onFailCommands = new ArrayList();
    public String                   onFailSend = "";
    public Location                 onFailLocation = null;

    public List<FAILTRIGGERS>       failTriggers = new ArrayList();
    public Long                     failBounty = Long.MAX_VALUE;

    public ItemStack[]              missionItems = new ItemStack[60];

    public Integer                  penaltyStatusEscaped = -5;
    public Integer                  penaltyStatusWanted = 25;
    public Integer                  penaltyStatusJailed = 60;

    private TheJail_Plugin          pluginRef = null;

    public TheJailSettings(TheJail_Plugin plugin) {
        this.pluginRef = plugin;
    }

    public void loadSettings() {
        File settings = new File(pluginRef.getDataFolder(), "thejail_settings.yml");
        YamlConfiguration theJailSettings = null;

        try {
            theJailSettings = yamltools.loadConfiguration(settings);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if (theJailSettings == null) {
            //Default
            return;
        }

        if (theJailSettings.contains("onjoin.actions")) {
            onJoinActions.clear();
            for (String action:theJailSettings.getStringList("onjoin.actions"))
            {
                try {
                    onJoinActions.add(JOINACTION.valueOf(action.toUpperCase()));
                } catch (Exception err)
                {}
            }
        }
        if (theJailSettings.contains("onjoin.location"))
            onJoinLocation = (Location) theJailSettings.get("onjoin.location");
        if (theJailSettings.contains("onjoin.commands"))
            onJoinCommands = theJailSettings.getStringList("onjoin.commands");



        if (theJailSettings.contains("oncompleted.actions")) {
            onJoinActions.clear();
            for (String action:theJailSettings.getStringList("oncompleted.actions"))
            {
                try {
                    onCompletedActions.add(COMPLETEDACTION.valueOf(action.toUpperCase()));
                } catch (Exception err)
                {}
            }
        }
        if (theJailSettings.contains("oncompleted.location"))
            onCompletedLocation = (Location) theJailSettings.get("oncompleted.location");
        if (theJailSettings.contains("oncompleted.commands"))
            onCompletedCommands = theJailSettings.getStringList("oncompleted.commands");
        onCompletedSend = theJailSettings.getString("oncompleted.send", "");



        if (theJailSettings.contains("onfail.action"))
        {
            onFailActions.clear();
            for (String action:(String[])theJailSettings.get("onfail.actions"))
            {
                try {
                    onFailActions.add(FAILACTION.valueOf(action.toUpperCase()));
                } catch (Exception err)
                {}
            }
        }
        if (theJailSettings.contains("onfail.location"))
            onFailLocation = (Location) theJailSettings.get("onfail.location");
        if (theJailSettings.contains("onfail.commands"))
            onFailCommands = theJailSettings.getStringList("onfail.commands");
        onFailSend = theJailSettings.getString("onfail.send", "");



        if (theJailSettings.contains("fail.triggers"))
        {
            failTriggers.clear();
            for (String action:theJailSettings.getStringList("fail.triggers"))
            {
                try {
                    failTriggers.add(FAILTRIGGERS.valueOf(action.toUpperCase()));
                } catch (Exception err)
                {}
            }
        }
        failBounty = theJailSettings.getLong("fail.bounty", Long.MAX_VALUE);

        penaltyStatusEscaped = theJailSettings.getInt("penalty.escaped", -5);
        penaltyStatusJailed = theJailSettings.getInt("penalty.jailed", 60);
        penaltyStatusWanted = theJailSettings.getInt("penalty.wanted", 25);

        if (theJailSettings.contains("missionitems")) {
            for (int cnt = 0; cnt < missionItems.length; cnt++) {
                if (theJailSettings.contains("missionitems." + cnt)) {
                    missionItems[cnt] = theJailSettings.getItemStack("missionitems." + cnt);
                }
            }
        }
    }

    public void saveSettings() {
        if (!pluginRef.getDataFolder().exists())
            pluginRef.getDataFolder().mkdirs();
        File settings = new File(pluginRef.getDataFolder(), "thejail_settings.yml");
        YamlConfiguration theJailSettings = new YamlConfiguration();

        if (onJoinActions.size()>0) {
            List<String> actions = new ArrayList<>();
            for (JOINACTION action : onJoinActions)
                actions.add(action.toString().toLowerCase());
            theJailSettings.set("onjoin.actions", actions);
        }
        if (onJoinLocation != null)
            theJailSettings.set("onjoin.location", onJoinLocation);
        if (onJoinCommands != null)
            theJailSettings.set("onjoin.commands", onJoinCommands);


        if (onCompletedActions.size()>0) {
            List<String> actions = new ArrayList<>();
            for (COMPLETEDACTION action : onCompletedActions)
                actions.add(action.toString().toLowerCase());
            theJailSettings.set("oncompleted.actions", actions);
        }
        if (onCompletedLocation != null)
            theJailSettings.set("oncompleted.location", onCompletedLocation);
        if (onCompletedCommands != null)
            theJailSettings.set("oncompleted.commands", onCompletedCommands);
        if (!onCompletedSend.equals(""))
            theJailSettings.set("oncompleted.send", onCompletedSend);


        if (onFailActions.size()>0) {
            List<String> actions = new ArrayList<>();
            for (FAILACTION action : onFailActions)
                actions.add(action.toString().toLowerCase());
            theJailSettings.set("onfail.actions", actions);
        }
        if (onFailLocation != null)
            theJailSettings.set("onfail.location", onFailLocation);
        if (onFailCommands != null)
            theJailSettings.set("onfail.commands", onFailCommands);
        if (!onFailSend.equals(""))
            theJailSettings.set("onfail.send", onFailSend);


        if (failTriggers.size()>0) {
            List<String> actions = new ArrayList<>();
            for (FAILTRIGGERS action : failTriggers)
                actions.add(action.toString().toLowerCase());
            theJailSettings.set("fail.triggers", actions);
        }
        if (failBounty != Long.MAX_VALUE)
            theJailSettings.set("fail.bounty", failBounty);

        theJailSettings.set("penalty.escaped", penaltyStatusEscaped);
        theJailSettings.set("penalty.jailed", penaltyStatusJailed);
        theJailSettings.set("penalty.wanted", penaltyStatusWanted);

        int itemCounter = 0;
        for (int cnt = 0; cnt < missionItems.length; cnt++) {
            if (missionItems[cnt] != null && missionItems[cnt].getType() != Material.AIR) {
                theJailSettings.set("missionitems." + itemCounter, missionItems[cnt]);
                itemCounter++;
            }
        }

        try {
            theJailSettings.save(settings);
        } catch (IOException e) {
            // Problem return and don't save (Not right)
            return;
        }
    }
}
