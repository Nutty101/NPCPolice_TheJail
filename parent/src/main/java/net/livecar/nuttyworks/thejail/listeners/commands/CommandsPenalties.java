package net.livecar.nuttyworks.thejail.listeners.commands;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.annotations.CommandInfo;
import net.livecar.nuttyworks.thejail.enumerations.COMPLETEDACTION;
import net.livecar.nuttyworks.thejail.enumerations.GUIMENUTYPE;
import net.livecar.nuttyworks.thejail.gui_interface.ItemsGUI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class CommandsPenalties {
    @CommandInfo(
            name = "penalty",
            group = "Setting Commands",
            badArgumentsMessage = "command_penalty_args",
            helpMessage = "command_penalty_help",
            arguments = {""},
            permission = "thejail.settings.penalty",
            allowConsole = true,
            minArguments = 2,
            maxArguments = 2
    )
    public boolean theJail_penalty(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {

        if (!StringUtils.isNumeric(inargs[2]))
        {
            pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
            return true;
        }
        Integer value  = Integer.parseInt(inargs[2]);

        if (inargs[1].toLowerCase().equals("escape"))
            pluginRef.getSettings.penaltyStatusEscaped = value;
        if (inargs[1].toLowerCase().equals("wanted"))
            pluginRef.getSettings.penaltyStatusWanted = value;
        if (inargs[1].toLowerCase().equals("jailed"))
            pluginRef.getSettings.penaltyStatusJailed = value;

        pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
        return true;
    }

}
