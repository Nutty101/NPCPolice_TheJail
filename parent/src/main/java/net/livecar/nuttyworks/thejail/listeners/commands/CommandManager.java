package net.livecar.nuttyworks.thejail.listeners.commands;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.annotations.CommandInfo;
import net.livecar.nuttyworks.thejail.enumerations.COMPLETEDACTION;
import net.livecar.nuttyworks.thejail.enumerations.FAILACTION;
import net.livecar.nuttyworks.thejail.enumerations.JOINACTION;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class CommandManager {
    HashMap<String, CommandRecord> registeredCommands;
    private List<String> commandGroups;
    private TheJail_Plugin pluginRef;

    public CommandManager(TheJail_Plugin pluginRef) {
        this.pluginRef = pluginRef;
        registeredCommands = new HashMap<>();
        commandGroups = new ArrayList<>();
    }


    public boolean onCommand(CommandSender sender, String[] inargs) {

        int npcid = -1;
        String selectedWorld = null;

        Player player = null;
        // Commands only avail to players
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        List<String> sList = new ArrayList<>();
        for (int nCnt = 0; nCnt < inargs.length; nCnt++) {
            if (inargs[nCnt].equalsIgnoreCase("--npc")) {
                // Npc ID should be the next one
                if (inargs.length >= nCnt + 2) {
                    npcid = Integer.parseInt(inargs[nCnt + 1]);
                    nCnt++;
                }
            } else {
                sList.add(inargs[nCnt]);
            }
        }

        inargs = sList.toArray(new String[sList.size()]);
        NPC npc;
        if (npcid == -1) {
            // Now lets find the NPC this should run on.
            npc = pluginRef.getCitizensPlugin.getNPCSelector().getSelected(sender);
        } else {
            npc = CitizensAPI.getNPCRegistry().getById(npcid);
        }


        if (inargs.length == 0) {
            inargs = new String[]{"help"};
        }

        if (inargs[0].equalsIgnoreCase("help")) {

            for (String groupName : commandGroups) {
                StringBuilder response = new StringBuilder();

                for (CommandRecord cmdRecord : registeredCommands.values()) {
                    if (cmdRecord.groupName.equals(groupName)) {
                        if (pluginRef.hasPermissions(sender, cmdRecord.commandPermission) && isPlayer(sender)) {
                            String messageValue = pluginRef.getMessageManager.buildMessage(sender, "command_jsonhelp." + cmdRecord.helpMessage, npc, null,0)[0];
                            if (messageValue.trim().equals("")) {
                                pluginRef.getMessageManager.logToConsole("Language Message Missing (" + cmdRecord.helpMessage + ")");
                            } else {
                                response.append(messageValue.replaceAll("<permission>", cmdRecord.commandPermission).replaceAll("<commandname>", cmdRecord.commandName) + ",{\"text\":\" \"},");
                            }
                        } else if (pluginRef.hasPermissions(sender, cmdRecord.commandPermission) && (!isPlayer(sender) && cmdRecord.allowConsole)) {
                            response.append(cmdRecord.commandName + " ");
                        }
                    }
                }
                if (isPlayer(sender) && !response.toString().trim().equals("")) {
                    pluginRef.getMessageManager.sendMessage(sender, "command_jsonhelp.command_help_group", groupName);
                    pluginRef.getMessageManager.sendJsonRaw((Player) sender, "[" + response.toString() + "{\"text\":\"\"}]");
                } else if (!isPlayer(sender) && !response.toString().trim().equals("")) {
                    sender.sendMessage("---[" + groupName + "]--------------------");
                    sender.sendMessage(response.toString());
                }
            }
            return true;
        } else if (registeredCommands.containsKey(inargs[0])) {
            CommandRecord cmdRecord = registeredCommands.get(inargs[0].toLowerCase());
            if (!cmdRecord.allowConsole & !isPlayer(sender)) {
                pluginRef.getMessageManager.sendMessage(sender, "console_messages.command_noconsole");
                return true;
            }

            if (!pluginRef.hasPermissions(sender, cmdRecord.commandPermission))
            {
                pluginRef.getMessageManager.sendMessage(sender, "messages.no_permissions");
                return true;
            }

            if (pluginRef.hasPermissions(sender, cmdRecord.commandPermission) && (inargs.length - 1 >= cmdRecord.minArguments && inargs.length - 1 <= cmdRecord.maxArguments))
                if (registeredCommands.get(inargs[0].toLowerCase()).invokeCommand(pluginRef, sender, npc, inargs))
                    return true;

            if (isPlayer(sender)) {
                String messageValue = pluginRef.getMessageManager.buildMessage(sender,"command_jsonhelp." + cmdRecord.helpMessage,null,null,0)[0];
                if (messageValue.trim().equals("")) {
                    pluginRef.getMessageManager.logToConsole("Language Message Missing (" + cmdRecord.helpMessage + ")", Level.WARNING);
                } else {
                    messageValue = messageValue.replaceAll("<permission>", cmdRecord.commandPermission).replaceAll("<commandname>", cmdRecord.commandName);
                }
                pluginRef.getMessageManager.sendMessage(sender, "general_messages.commands_invalidarguments", messageValue);
                return true;
            } else
                pluginRef.getMessageManager.sendMessage(sender, "console_messages." + cmdRecord.helpMessage);

        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, String[] arguments) {
        List<String> results = new ArrayList<>();
        Boolean isPlayer = (sender instanceof Player);
        if (arguments.length == 1) {
            for (CommandRecord cmdSetting : this.registeredCommands.values()) {
                if ((!isPlayer && cmdSetting.allowConsole) || pluginRef.hasPermissions(sender, cmdSetting.commandPermission)) {
                    if ((arguments[0].trim().length() > 0 && cmdSetting.commandName.startsWith(arguments[0].trim().toLowerCase())) || arguments[0].trim().equals(""))
                        results.add(cmdSetting.commandName);
                }
            }
        } else {
            for (CommandRecord cmdSetting : this.registeredCommands.values()) {
                if ((!isPlayer && cmdSetting.allowConsole) || pluginRef.hasPermissions(sender, cmdSetting.commandPermission)) {
                    if (arguments[0].trim().equalsIgnoreCase(cmdSetting.commandName)) {
                        if (arguments.length - 1 <= cmdSetting.arguments.length) {
                            String argumentLine = cmdSetting.arguments[arguments.length - 2];
                            String currentArg = arguments[arguments.length - 1].trim();
                            String priorArg = "";
                            if (arguments.length - 2 > -1)
                                priorArg = arguments[arguments.length - 2].trim();

                            if (argumentLine.contains("|")) {
                                if (currentArg.equals("")) {
                                    for (String itemDesc : argumentLine.split("\\|")) {
                                        results.addAll(parseTabItem(itemDesc, priorArg,currentArg));
                                    }

                                    return results;
                                } else {
                                    for (String argValue : argumentLine.split("\\|")) {
                                        if (argValue.toLowerCase().startsWith(currentArg.toLowerCase())) {
                                            results.addAll(parseTabItem(argValue, priorArg,currentArg));
                                        }
                                    }
                                    return results;
                                }
                            } else if (argumentLine.equalsIgnoreCase("<PLAYERNAME>")) {
                                return null;
                            } else {
                                results.addAll(parseTabItem(argumentLine, priorArg,currentArg));
                                return results;
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    public void registerCommandClass(Class<?> commandClass) {
        for (Method commandMethod : commandClass.getMethods()) {
            if (commandMethod.isAnnotationPresent(CommandInfo.class)) {
                CommandInfo methodAnnotation = commandMethod.getAnnotation(CommandInfo.class);
                if (!commandGroups.contains(methodAnnotation.group()))
                    commandGroups.add(methodAnnotation.group());
                CommandRecord cmdRecord = new CommandRecord(methodAnnotation.name(), methodAnnotation.group(), methodAnnotation.permission(), methodAnnotation.badArgumentsMessage(), methodAnnotation.helpMessage(), methodAnnotation.allowConsole(), methodAnnotation.minArguments(), methodAnnotation.maxArguments(), methodAnnotation.arguments(), commandClass, commandMethod.getName());
                registeredCommands.put(methodAnnotation.name(), cmdRecord);
            }
        }
    }

    private List<String> parseTabItem(String item, String priorArg, String currentValue) {
        List<String> results = new ArrayList<String>();

        if (item.equalsIgnoreCase("<player>")) {
            for (Player plr : pluginRef.getServer().getOnlinePlayers()) {
                if (currentValue.length() > 0)
                {
                    if (String.valueOf(plr.getName()).toLowerCase().startsWith(currentValue.toLowerCase()))
                        results.add(plr.getName());
                } else {
                    results.add(plr.getName());
                }
            }
        } else if (item.equalsIgnoreCase("<world>")) {
            for (World world : pluginRef.getServer().getWorlds()) {
                if (currentValue.length() > 0)
                {
                    if (world.getName().toLowerCase().startsWith(currentValue.toLowerCase()))
                        results.add(world.getName());
                } else {
                    results.add(world.getName());
                }
            }
        } else if (item.equalsIgnoreCase("<npc>")) {
            for (NPC npc : pluginRef.getCitizensPlugin.getNPCRegistry()) {
                if (currentValue.length() > 0)
                {
                    if (String.valueOf(npc.getId()).toLowerCase().startsWith(currentValue.toLowerCase()))
                        results.add(String.valueOf(npc.getId()));
                } else {
                    results.add(String.valueOf(npc.getId()));
                }
            }
        } else if (item.equalsIgnoreCase("<groups>") ) {
            if (pluginRef.getPermissionManager != null)
            {
                for (String groupName : Arrays.asList(pluginRef.getPermissionManager.getGroups())) {
                    if (currentValue.length() > 0)
                    {
                        if (groupName.toLowerCase().startsWith(currentValue.toLowerCase()))
                            results.add(groupName);
                    } else {
                        results.add(groupName);
                    }
                }
            }
        } else if (item.equalsIgnoreCase("<server>")) {
            results.addAll(pluginRef.getBungeeListener.getServerList());
            for (String serverName : pluginRef.getBungeeListener.getServerList()) {
                if (currentValue.length() > 0)
                {
                    if (serverName.toLowerCase().startsWith(currentValue.toLowerCase()))
                        results.add(serverName);
                } else {
                    results.add(serverName);
                }
            }
        } else if (item.equalsIgnoreCase("<joinaction>")) {
            for (JOINACTION action : JOINACTION.values()) {
                results.add(String.valueOf(action.name()));
            }
        } else if (item.equalsIgnoreCase("<failaction>")) {
            for (FAILACTION action : FAILACTION.values()) {
                results.add(String.valueOf(action.name()));
            }
        } else if (item.equalsIgnoreCase("<completeaction>")) {
            for (COMPLETEDACTION action : COMPLETEDACTION.values()) {
                results.add(String.valueOf(action.name()));
            }
        } else {
            results.add(item);
        }
        return results;
    }

    private boolean isPlayer(CommandSender sender) {
        if (sender instanceof Player)
            return true;
        return false;
    }
}
