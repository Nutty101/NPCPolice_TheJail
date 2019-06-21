package net.livecar.nuttyworks.thejail.listeners.commands;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_police.NPCPolice_Plugin;
import net.livecar.nuttyworks.npc_police.NPC_Police;
import net.livecar.nuttyworks.npc_police.utilities.Utilities;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.annotations.CommandInfo;
import net.livecar.nuttyworks.thejail.enumerations.FAILACTION;
import net.livecar.nuttyworks.thejail.enumerations.FAILTRIGGERS;
import net.livecar.nuttyworks.thejail.enumerations.GUIMENUTYPE;
import net.livecar.nuttyworks.thejail.enumerations.JOINACTION;
import net.livecar.nuttyworks.thejail.gui_interface.ItemsGUI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class CommandsOnFail {
    @CommandInfo(
            name = "onfailaction",
            group = "Setting Commands",
            badArgumentsMessage = "command_onfailaction_args",
            helpMessage = "command_onfailaction_help",
            arguments = {"<failaction>","<server>"},
            permission = "thejail.settings.onfail",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 2
    )
    public boolean jailConfig_onfailaction(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {
        if (inargs.length > 1)
        {
            try {
                FAILACTION failAction = FAILACTION.valueOf(inargs[1].toUpperCase());
                if (pluginRef.getSettings.onFailActions.contains(failAction)) {
                    if (failAction == FAILACTION.TELEPORT) {
                        pluginRef.getSettings.onFailLocation = null;
                        pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_location_clear","OnFail");
                    }
                    if (failAction == FAILACTION.SEND) {
                        pluginRef.getSettings.onFailSend = "";
                        pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_server_clear","OnFail");
                    }

                    pluginRef.getSettings.onFailActions.remove(failAction);
                }
                else {
                    if (failAction == FAILACTION.TELEPORT) {
                        pluginRef.getSettings.onFailLocation = ((Player) sender).getLocation();
                        pluginRef.getSettings.onFailSend = "";
                        pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_location_set","OnFail");
                    }
                    if (failAction == FAILACTION.SEND) {
                        if (inargs.length < 3)
                        {
                            pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_server_clear","OnFail");
                            pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
                            return true;
                        } else {
                            pluginRef.getSettings.onFailSend = inargs[2];
                            pluginRef.getSettings.onFailLocation = null;
                        }
                        pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_server_set","OnFail");
                    }
                    pluginRef.getSettings.onFailActions.add(failAction);

                }
            } catch (Exception err)
            {}
        }

        pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
        return true;
    }

    @CommandInfo(
            name = "onfailcommand",
            group = "Setting Commands",
            badArgumentsMessage = "command_onfailcommand_args",
            helpMessage = "command_onfailcommand_help",
            arguments = {""},
            permission = "thejail.settings.onfail",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 999
    )
    public boolean jailConfig_onfailcommand(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {

        if (inargs.length < 2) {
            if (!(sender instanceof Player))
                return false;

            Player plr = (Player) sender;

            if (pluginRef.getMCVersionUtils.isHoldingBook(plr)) {

                BookMeta meta = (BookMeta) pluginRef.getMCVersionUtils.getMainHand(plr).getItemMeta();
                String commandString = "";
                for (int pageNum = 1; pageNum <= meta.getPageCount(); pageNum++)
                    commandString += meta.getPage(1).trim();
                pluginRef.getSettings.onFailCommands.add(commandString);
            } else {
                //Open inventory
                ItemsGUI guiMenu = new ItemsGUI("OnFail Commands", 54, pluginRef, (Player) sender,"general_messages.config_items", GUIMENUTYPE.ONFAILCOMMANDS);
                int chestID = 0;
                ItemStack[] commandInv = pluginRef.getMCVersionUtils.stringListToItemStack(pluginRef.getSettings.onFailCommands);
                for (int slotID = 0; slotID < commandInv.length; slotID++) {
                    if (slotID > 53)
                        break;
                    if (commandInv[slotID] != null && commandInv[slotID].getType() != Material.AIR) {
                        guiMenu.setSlotItem(chestID, commandInv[slotID]);
                        chestID++;
                    }
                }
                guiMenu.open();
            }

            pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
            return true;
        } else if (inargs.length > 2) {
            String commandString = "";

            for (int nCnt = 2; nCnt < inargs.length; nCnt++) {
                commandString += inargs[nCnt] + " ";
            }
            pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
            return true;
        }

        pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
        return true;
    }

    @CommandInfo(
            name = "failtrigger",
            group = "Setting Commands",
            badArgumentsMessage = "command_failtrigger_args",
            helpMessage = "command_failtrigger_help",
            arguments = {"#"},
            permission = "thejail.settings.failtrigger",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 2
    )
    public boolean jailConfig_failtriggers(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {
        if (inargs.length > 1)
        {
            try {
                FAILTRIGGERS failTrigger = FAILTRIGGERS.valueOf(inargs[1].toUpperCase());
                if (pluginRef.getSettings.failTriggers.contains(failTrigger)) {
                    if (failTrigger == FAILTRIGGERS.BOUNTY)
                        pluginRef.getSettings.failBounty = Long.MAX_VALUE;
                    pluginRef.getSettings.failTriggers.remove(failTrigger);
                }
                else {
                    if (failTrigger == FAILTRIGGERS.BOUNTY)
                    {
                        if (inargs.length == 3 && StringUtils.isNumeric(inargs[2]))
                            pluginRef.getSettings.failBounty = Long.parseLong(inargs[2]);
                    }
                    pluginRef.getSettings.failTriggers.add(failTrigger);
                }
            } catch (Exception err)
            {}
        }

        pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
        return true;
    }


}
