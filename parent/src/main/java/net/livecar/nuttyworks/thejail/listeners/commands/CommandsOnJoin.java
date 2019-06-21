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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class CommandsOnJoin {
    @CommandInfo(
            name = "onjoinaction",
            group = "Setting Commands",
            badArgumentsMessage = "command_onjoinaction_args",
            helpMessage = "command_onjoinaction_help",
            arguments = {"<joinaction>"},
            permission = "thejail.settings.onjoin",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 1
    )
    public boolean jailConfig_onjoinaction(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {
        if (inargs.length > 1)
        {
            try {
                JOINACTION joinAction = JOINACTION.valueOf(inargs[1].toUpperCase());
                if (pluginRef.getSettings.onJoinActions.contains(joinAction)) {
                    if (joinAction == JOINACTION.TELEPORT) {
                        pluginRef.getSettings.onJoinLocation = null;
                        pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_location_clear","OnJoin");
                    }
                    pluginRef.getSettings.onJoinActions.remove(joinAction);
                }
                else {
                    if (joinAction == JOINACTION.TELEPORT) {
                        pluginRef.getSettings.onJoinLocation = ((Player) sender).getLocation();
                        pluginRef.getMessageManager.sendMessage(sender, "general_messages.config_location_set","OnJoin");
                    }
                    pluginRef.getSettings.onJoinActions.add(joinAction);

                }
            } catch (Exception err)
            {}
        }

        pluginRef.getCommandManager.onCommand(sender, new String[] { "settings"});
        return true;
    }

    @CommandInfo(
            name = "onjoincommand",
            group = "Setting Commands",
            badArgumentsMessage = "command_onjoincommand_args",
            helpMessage = "command_onjoincommand_help",
            arguments = {"view|add"},
            permission = "thejail.settings.onjoin",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 999
    )
    public boolean jailConfig_onjoincommand(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {

        if (inargs.length < 2) {
            if (!(sender instanceof Player))
                return false;

            Player plr = (Player) sender;

            if (pluginRef.getMCVersionUtils.isHoldingBook(plr)) {

                BookMeta meta = (BookMeta) pluginRef.getMCVersionUtils.getMainHand(plr).getItemMeta();
                String commandString = "";
                for (int pageNum = 1; pageNum <= meta.getPageCount(); pageNum++)
                    commandString += meta.getPage(1).trim();
                pluginRef.getSettings.onJoinCommands.add(commandString);
            } else {
                //Open inventory
                ItemsGUI guiMenu = new ItemsGUI("OnJoin Commands", 54, pluginRef, (Player) sender,"general_messages.config_items", GUIMENUTYPE.ONJOINCOMMANDS);
                int chestID = 0;
                ItemStack[] commandInv = pluginRef.getMCVersionUtils.stringListToItemStack(pluginRef.getSettings.onJoinCommands);
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
