package net.livecar.nuttyworks.thejail.listeners.commands;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.annotations.CommandInfo;
import net.livecar.nuttyworks.thejail.enumerations.GUIMENUTYPE;
import net.livecar.nuttyworks.thejail.enumerations.JOINACTION;
import net.livecar.nuttyworks.thejail.gui_interface.ItemsGUI;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandsSettings {

    @CommandInfo(
            name = "settings",
            group = "Setting Commands",
            badArgumentsMessage = "command_settings_args",
            helpMessage = "command_settings_help",
            arguments = {""},
            permission = "thejail.settings",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
    )
    public boolean theJail_Settings(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {
        pluginRef.getMessageManager.sendMessage(sender, "settings_menu");
        return true;
    }

    @CommandInfo(
            name = "missionitem",
            group = "Setting Commands",
            badArgumentsMessage = "command_missionitem_args",
            helpMessage = "command_missionitem_help",
            arguments = {"<joinaction>"},
            permission = "thejail.settings.missionitems",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
    )
    public boolean jailConfig_missionitem(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {
        ItemsGUI guiMenu = new ItemsGUI("Mission Items", 54, pluginRef, (Player) sender,"general_messages.config_items", GUIMENUTYPE.MISSIONITEMS);
        int chestID = 0;
        for (int slotID = 0; slotID < pluginRef.getSettings.missionItems.length; slotID++) {
            if (slotID > 53)
                break;
            if (pluginRef.getSettings.missionItems[slotID] != null && pluginRef.getSettings.missionItems[slotID].getType() != Material.AIR) {
                guiMenu.setSlotItem(chestID, pluginRef.getSettings.missionItems[slotID]);
                chestID++;
            }
        }
        guiMenu.open();
        return true;
    }

}
