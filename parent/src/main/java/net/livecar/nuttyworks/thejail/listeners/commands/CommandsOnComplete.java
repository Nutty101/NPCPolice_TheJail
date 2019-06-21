package net.livecar.nuttyworks.thejail.listeners.commands;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.annotations.CommandInfo;
import net.livecar.nuttyworks.thejail.enumerations.COMPLETEDACTION;
import net.livecar.nuttyworks.thejail.enumerations.FAILACTION;
import net.livecar.nuttyworks.thejail.enumerations.GUIMENUTYPE;
import net.livecar.nuttyworks.thejail.gui_interface.ItemsGUI;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class CommandsOnComplete {
    @CommandInfo(
            name = "oncompleteaction",
            group = "Setting Commands",
            badArgumentsMessage = "command_oncompleteaction_args",
            helpMessage = "command_oncompleteaction_help",
            arguments = {"<completeaction>","<server>"},
            permission = "thejail.settings.oncomplete",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 2
    )
    public boolean jailConfig_oncompleteaction(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {
        if (inargs.length > 1)
        {
            try {
                COMPLETEDACTION completeAction = COMPLETEDACTION.valueOf(inargs[1].toUpperCase());
                if (pluginRef.getSettings.onCompletedActions.contains(completeAction)) {
                    if (completeAction == COMPLETEDACTION.TELEPORT) {
                        pluginRef.getSettings.onCompletedLocation = null;
                        pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_location_clear","OnComplete");
                    }
                    if (completeAction == COMPLETEDACTION.SEND) {
                        pluginRef.getSettings.onCompletedSend = "";
                        pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_server_clear","OnComplete");
                    }

                    pluginRef.getSettings.onCompletedActions.remove(completeAction);
                }
                else {
                    if (completeAction == COMPLETEDACTION.TELEPORT) {
                        pluginRef.getSettings.onCompletedLocation = ((Player) sender).getLocation();
                        pluginRef.getSettings.onCompletedSend = "";
                        pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_location_set","OnComplete");
                    }
                    if (completeAction == COMPLETEDACTION.SEND) {
                        if (inargs.length < 3)
                        {
                            pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_server_clear","OnComplete");
                            pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
                            return true;
                        } else {
                            pluginRef.getSettings.onCompletedSend = inargs[2];
                            pluginRef.getSettings.onCompletedLocation = null;
                        }
                        pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_server_set","OnComplete");
                    }
                    pluginRef.getSettings.onCompletedActions.add(completeAction);

                }
            } catch (Exception err)
            {}
        }

        pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
        return true;
    }

    @CommandInfo(
            name = "oncompletecommand",
            group = "Setting Commands",
            badArgumentsMessage = "command_oncompletecommand_args",
            helpMessage = "command_oncompletecommand_help",
            arguments = {""},
            permission = "thejail.settings.oncomplete",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 999
    )
    public boolean jailConfig_oncompletecommand(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {

        if (inargs.length < 2) {
            if (!(sender instanceof Player))
                return false;

            Player plr = (Player) sender;

            if (pluginRef.getMCVersionUtils.isHoldingBook(plr)) {

                BookMeta meta = (BookMeta) pluginRef.getMCVersionUtils.getMainHand(plr).getItemMeta();
                String commandString = "";
                for (int pageNum = 1; pageNum <= meta.getPageCount(); pageNum++)
                    commandString += meta.getPage(1).trim();
                pluginRef.getSettings.onCompletedCommands.add(commandString);
            } else {
                //Open inventory
                ItemsGUI guiMenu = new ItemsGUI("OnCompleted Commands", 54, pluginRef, (Player) sender,"general_messages.config_items", GUIMENUTYPE.ONCOMPLETECOMMANDS);
                int chestID = 0;
                ItemStack[] commandInv = pluginRef.getMCVersionUtils.stringListToItemStack(pluginRef.getSettings.onCompletedCommands);
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

}
