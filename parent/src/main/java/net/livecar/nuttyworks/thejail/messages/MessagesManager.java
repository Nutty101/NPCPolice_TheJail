package net.livecar.nuttyworks.thejail.messages;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.COMPLETEDACTION;
import net.livecar.nuttyworks.thejail.enumerations.FAILACTION;
import net.livecar.nuttyworks.thejail.enumerations.FAILTRIGGERS;
import net.livecar.nuttyworks.thejail.enumerations.JOINACTION;
import net.livecar.nuttyworks.thejail.playerdata.PlayerMission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class MessagesManager {
    private List<LogDetail> logHistory;
    private jsonChat        jsonManager;
    private TheJail_Plugin  pluginRef;
    private BukkitTask      debugLogSave;

    public MessagesManager(TheJail_Plugin pluginRef) {
        this.pluginRef = pluginRef;
        jsonManager = new jsonChat(pluginRef);
    }

    public void sendJsonRaw(Player player, String message) {
        this.jsonManager.sendJsonMessage(player, message);
    }

    public void consoleMessage(String msgKey) {
        consoleMessage(msgKey, "", Level.INFO);
    }

    public void consoleMessage(String msgKey, Level logLevel) {
        consoleMessage(msgKey, "", logLevel);
    }

    public void consoleMessage(String msgKey, String extendedMessage) {
        consoleMessage(msgKey, extendedMessage, Level.INFO);
    }

    public void consoleMessage(String msgKey, String extendedMessage, Level logLevel) {
        for (String message : buildMessage(msgKey.toLowerCase(), extendedMessage))
            logToConsole(message, logLevel);
    }

    public void logToConsole(String logLine) {
        pluginRef.getLogger().log(Level.INFO, logLine);
    }

    public void logToConsole(String logLine, Level logLevel) {
        pluginRef.getLogger().log(logLevel, logLine);
    }

    public void debugMessage(Level debugLevel, String extendedMessage) {
        if (logHistory == null)
            logHistory = new ArrayList<LogDetail>();

        if (pluginRef.debugLogLevel.intValue() <= debugLevel.intValue()) {
            String className = new Exception().getStackTrace()[1].getClassName();
            logHistory.add(new LogDetail(debugLevel.toString() + "|" + className.substring(className.lastIndexOf(".")) + "|" + new Exception().getStackTrace()[1].getMethodName() + "|" + new Exception().getStackTrace()[1].getLineNumber() + "|" + extendedMessage));

            if (debugLogSave != null) {
                if (pluginRef.isEnabled()) {
                    debugLogSave = new BukkitRunnable() {
                        @Override
                        public void run() {
                            saveDebugMessages();
                        }
                    }.runTaskLater(pluginRef, 500);
                }
            }
        } else {
            saveDebugMessages();
        }

    }

    private void saveDebugMessages() {
        if (logHistory != null && logHistory.size() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'.log'");
            try (FileWriter fileOut = new FileWriter(new File(pluginRef.loggingPath, dateFormat.format(logHistory.get(0).logDateTime)), true)) {
                for (LogDetail logLine : logHistory) {
                    SimpleDateFormat lnDateFormat = new SimpleDateFormat("hh:mm:ss");

                    fileOut.write(lnDateFormat.format(logLine.logDateTime) + "|" + logLine.logContent + "\r\n");
                }
                logHistory.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] buildMessage(String msgKey, String extendedMessage) {
        return buildMessage(null,msgKey,null,null,0,extendedMessage);
    }

    public String[] buildMessage(CommandSender sender, String msgKey, NPC npc, NPC targetNPC, int ident) {
        return buildMessage(sender,msgKey,npc,targetNPC,ident,"");
    }

    public String[] buildMessage(CommandSender sender, String msgKey, NPC npc, NPC targetNPC, int ident, String extendedMessage)
    {
        try {
            List<String> processedMessages = new ArrayList<String>();

            String[] messages = this.getResultMessage(msgKey.toLowerCase());
            if (messages == null || messages.length == 0) {
                this.logToConsole("Unable to locate [" + msgKey + "]");
                return new String[0];
            }

            for (int nCnt = 0; nCnt < messages.length; nCnt++) {
                messages[nCnt] = messages[nCnt].replaceAll("<message>", extendedMessage);
            }


            for (int nCnt = 0; nCnt < messages.length; nCnt++) {
                processedMessages.add(parseMessage(sender, messages[nCnt], npc, targetNPC, ident));
            }
            return processedMessages.toArray(new String[processedMessages.size()]);
        } catch (Exception err)
        {
            return new String[]{""};
        }
    }

    public void sendMessage(CommandSender sender, String msgKey) {
        sendMessage(sender, buildMessage(sender, msgKey, null, null, 0));
    }

    public void sendMessage(CommandSender sender, String msgKey, String message) {
        sendMessage(sender, buildMessage(msgKey, message));
    }

    public void sendMessage(CommandSender sender, String msgKey, NPC npc,NPC targetNPC) {
        sendMessage(sender, buildMessage(sender, msgKey, npc, targetNPC,0, ""));
    }

    public void sendMessage(CommandSender sender, String msgKey, NPC npc,NPC targetNPC, String message) {
        sendMessage(sender, buildMessage(sender, msgKey, npc, targetNPC,0, message));
    }

    private void sendMessage(CommandSender sender, String[] messages) {
        if (sender instanceof Player) {
            String sjsonMessage = "";
            for (String sMsg : messages) {
                if (sMsg.startsWith("[") && sjsonMessage.length() > 0) {
                    jsonManager.sendJsonMessage((Player) sender, sjsonMessage);
                    sjsonMessage = "";
                }
                sjsonMessage += sMsg;

                if (sjsonMessage.endsWith("]")) {
                    if (sjsonMessage.endsWith(",]"))
                        sjsonMessage = sjsonMessage.substring(0, sjsonMessage.length() - 2) + "]";
                    if (!sjsonMessage.equalsIgnoreCase("[]")) {
                        jsonManager.sendJsonMessage((Player) sender, sjsonMessage);
                        sjsonMessage = "";
                    } else {
                        sjsonMessage = "";
                    }
                }
            }
        } else {
            // sender.sendMessage(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
            // message)));
        }
    }

    public String parseMessage(CommandSender sender, String message, NPC npc, NPC targetNPC, int ident) {
        if (message == null)
            return "";

        if (message.toLowerCase().contains("<plugin.language>"))
            message = message.replaceAll("<plugin\\.language>", pluginRef.getDefaultConfig.getString("language", "en-def"));

        // OnJoin Messages
        for (JOINACTION action : JOINACTION.values())
        {
            if (message.toLowerCase().contains("<setting.onjoin." + action.toString().toLowerCase() + ">")) {

                if (action == JOINACTION.TELEPORT) {
                    if (pluginRef.getSettings.onJoinLocation != null)
                        message = message.replaceAll("<setting\\.onjoin\\." + action.toString().toLowerCase() + ">", "✔\",\"color\":\"yellow");
                    else
                        message = message.replaceAll("<setting\\.onjoin\\." + action.toString().toLowerCase() + ">", "X\",\"color\":\"red");
                } else {
                    if (pluginRef.getSettings.onJoinActions.contains(action))
                        message = message.replaceAll("<setting\\.onjoin\\." + action.toString().toLowerCase() + ">", "✔\",\"color\":\"yellow");
                    else
                        message = message.replaceAll("<setting\\.onjoin\\." + action.toString().toLowerCase() + ">", "X\",\"color\":\"red");
                }
            }
            if (message.toLowerCase().contains("<setting.onjoin." + action.toString().toLowerCase() + ".value>")) {
                if (action == JOINACTION.TELEPORT) {
                    if (pluginRef.getSettings.onJoinLocation == null)
                        message = message.replaceAll("<setting\\.onjoin\\." + action.toString().toLowerCase() + ".value>", this.getResultMessage("result_Messages.notset")[0]);
                    else
                        message = message.replaceAll("<setting\\.onjoin\\." + action.toString().toLowerCase() + ".value>", String.valueOf(pluginRef.getSettings.onJoinLocation.getBlockX()) + "," + String.valueOf(pluginRef.getSettings.onJoinLocation.getBlockY()) + "," + String.valueOf(pluginRef.getSettings.onJoinLocation.getBlockZ()));
                }
            }
        }

        if (message.toLowerCase().contains("<setting.onjoin.actions>")) {
            if (pluginRef.getSettings.onJoinActions.size() == 0)
                message = message.replaceAll("<setting\\.onjoin\\.actions>", this.getResultMessage("result_Messages.notset")[0]);
            else
            {
                String newMessage = "";
                for(JOINACTION action : pluginRef.getSettings.onJoinActions)
                {
                    newMessage += action.toString().toLowerCase() + ", ";
                }
                newMessage = newMessage.substring(0,newMessage.length()-2);
                message = message.replaceAll("<setting\\.onjoin\\.actions>", newMessage);
            }
        }
        if (message.toLowerCase().contains("<setting.onjoin.location>")) {
            if (pluginRef.getSettings.onJoinLocation != null) {
                message = message.replaceAll("<setting\\.onjoin\\.location>", "(" + pluginRef.getSettings.onJoinLocation.getWorld().getName() + "," + pluginRef.getSettings.onJoinLocation.getBlockX() + "," + pluginRef.getSettings.onJoinLocation.getBlockY() + "," + pluginRef.getSettings.onJoinLocation.getBlockZ() + ")");
            } else {
                message = message.replaceAll("<setting\\.onjoin\\.location>", this.getResultMessage("result_Messages.notset")[0]);
            }
        }
        if (message.toLowerCase().contains("<setting.onjoin.messages>")) {
            if (pluginRef.getSettings.onJoinLocation != null) {
                message = message.replaceAll("<setting\\.onjoin\\.messages>", "(" + pluginRef.getSettings.onJoinLocation.getWorld().getName() + "," + pluginRef.getSettings.onJoinLocation.getBlockX() + "," + pluginRef.getSettings.onJoinLocation.getBlockY() + "," + pluginRef.getSettings.onJoinLocation.getBlockZ() + ")");
            } else {
                message = message.replaceAll("<setting\\.onjoin\\.messages>", this.getResultMessage("result_Messages.notset")[0]);
            }
        }

        // OnCompleted Messages
        for (COMPLETEDACTION action : COMPLETEDACTION.values())
        {
            if (message.toLowerCase().contains("<setting.oncomplete." + action.toString().toLowerCase() + ">")) {
                if (action == COMPLETEDACTION.TELEPORT) {
                    if (pluginRef.getSettings.onCompletedLocation != null)
                        message = message.replaceAll("<setting\\.oncomplete\\." + action.toString().toLowerCase() + ">", "✔\",\"color\":\"yellow");
                    else
                        message = message.replaceAll("<setting\\.oncomplete\\." + action.toString().toLowerCase() + ">", "X\",\"color\":\"red");
                } else if (action == COMPLETEDACTION.SEND) {
                    if (!pluginRef.getSettings.onCompletedSend.equals(""))
                        message = message.replaceAll("<setting\\.oncomplete\\." + action.toString().toLowerCase() + ">", "✔\",\"color\":\"yellow");
                    else
                        message = message.replaceAll("<setting\\.oncomplete\\." + action.toString().toLowerCase() + ">", "X\",\"color\":\"red");
                } else {
                if (pluginRef.getSettings.onCompletedActions.contains(action))
                    message = message.replaceAll("<setting\\.oncomplete\\." + action.toString().toLowerCase() + ">", "✔\",\"color\":\"yellow");
                else
                    message = message.replaceAll("<setting\\.oncomplete\\." + action.toString().toLowerCase() + ">", "X\",\"color\":\"red");
                }
            }
            if (message.toLowerCase().contains("<setting.oncomplete." + action.toString().toLowerCase() + ".value>")) {
                if (action == COMPLETEDACTION.TELEPORT)
                    if (pluginRef.getSettings.onCompletedLocation != null)
                        message = message.replaceAll("<setting\\.oncomplete\\." + action.toString().toLowerCase() + ".value>", String.valueOf(pluginRef.getSettings.onCompletedLocation.getBlockX()) + "," + String.valueOf(pluginRef.getSettings.onCompletedLocation.getBlockY()) + "," + String.valueOf(pluginRef.getSettings.onCompletedLocation.getBlockZ()));
                    else
                        message = message.replaceAll("<setting\\.oncomplete\\." + action.toString().toLowerCase() + ".value>", this.getResultMessage("result_Messages.notset")[0]);
                if (action == COMPLETEDACTION.SEND)
                    message = message.replaceAll("<setting\\.oncomplete\\." + action.toString().toLowerCase() + ".value>", pluginRef.getSettings.onCompletedSend);
            }
        }
        if (message.toLowerCase().contains("<setting.oncomplete.actions>")) {
            if (pluginRef.getSettings.onCompletedActions.size() == 0)
                message = message.replaceAll("<setting\\.oncomplete\\.actions>", this.getResultMessage("result_Messages.notset")[0]);
            else
            {
                String newMessage = "";
                for(COMPLETEDACTION action : pluginRef.getSettings.onCompletedActions)
                {
                    newMessage += action.toString().toLowerCase() + ", ";
                }
                newMessage = newMessage.substring(0,newMessage.length()-2);
                message = message.replaceAll("<setting\\.oncomplete\\.actions>", newMessage);
            }
        }
        if (message.toLowerCase().contains("<setting.oncomplete.send>"))
            message = message.replaceAll("<setting\\.oncomplete\\.send>", pluginRef.getSettings.onCompletedSend);


        // OnFail Messages
        for (FAILACTION action : FAILACTION.values())
        {
            if (message.toLowerCase().contains("<setting.onfail." + action.toString().toLowerCase() + ">")) {

                if (action == FAILACTION.TELEPORT) {
                    if (pluginRef.getSettings.onFailLocation != null)
                        message = message.replaceAll("<setting\\.onfail\\." + action.toString().toLowerCase() + ">", "✔\",\"color\":\"yellow");
                    else
                        message = message.replaceAll("<setting\\.onfail\\." + action.toString().toLowerCase() + ">", "X\",\"color\":\"red");
                } else if (action == FAILACTION.SEND)
                {
                    if (!pluginRef.getSettings.onFailSend.equals(""))
                        message = message.replaceAll("<setting\\.onfail\\." + action.toString().toLowerCase() + ">", "✔\",\"color\":\"yellow");
                    else
                        message = message.replaceAll("<setting\\.onfail\\." + action.toString().toLowerCase() + ">", "X\",\"color\":\"red");
                } else {
                    if (pluginRef.getSettings.onFailActions.contains(action))
                        message = message.replaceAll("<setting\\.onfail\\." + action.toString().toLowerCase() + ">", "✔\",\"color\":\"yellow");
                    else
                        message = message.replaceAll("<setting\\.onfail\\." + action.toString().toLowerCase() + ">", "X\",\"color\":\"red");
                }
            }
            if (message.toLowerCase().contains("<setting.onfail." + action.toString().toLowerCase() + ".value>")) {
                if (action == FAILACTION.TELEPORT)
                    if (pluginRef.getSettings.onFailLocation == null)
                        message = message.replaceAll("<setting\\.onfail\\." + action.toString().toLowerCase() + ".value>",this.getResultMessage("result_Messages.notset")[0]);
                        else
                        message = message.replaceAll("<setting\\.onfail\\." + action.toString().toLowerCase() + ".value>", String.valueOf(pluginRef.getSettings.onFailLocation.getBlockX()) + "," + String.valueOf(pluginRef.getSettings.onFailLocation.getBlockY()) + "," + String.valueOf(pluginRef.getSettings.onFailLocation.getBlockZ()));
                if (action == FAILACTION.SEND)
                    message = message.replaceAll("<setting\\.onfail\\." + action.toString().toLowerCase() + ".value>", pluginRef.getSettings.onFailSend);
            }

        }

        if (message.toLowerCase().contains("<setting.onfail.actions>")) {
            if (pluginRef.getSettings.onFailActions.size() == 0)
                message = message.replaceAll("<setting\\.onfail\\.actions>", this.getResultMessage("result_Messages.notset")[0]);
            else
            {
                String newMessage = "";
                for(FAILACTION action : pluginRef.getSettings.onFailActions)
                {
                    newMessage += action.toString().toLowerCase() + ", ";
                }
                newMessage = newMessage.substring(0,newMessage.length()-2);
                message = message.replaceAll("<setting\\.onfail\\.actions>", newMessage);
            }
        }
        if (message.toLowerCase().contains("<setting.onfail.location>")) {
            if (pluginRef.getSettings.onFailLocation != null) {
                message = message.replaceAll("<setting\\.onfail\\.location>", "(" + pluginRef.getSettings.onFailLocation.getWorld().getName() + "," + pluginRef.getSettings.onFailLocation.getBlockX() + "," + pluginRef.getSettings.onFailLocation.getBlockY() + "," + pluginRef.getSettings.onFailLocation.getBlockZ() + ")");
            } else {
                message = message.replaceAll("<setting\\.onfail\\.location>", this.getResultMessage("result_Messages.notset")[0]);
            }
        }
        if (message.toLowerCase().contains("<setting.onfail.send>"))
            message = message.replaceAll("<setting\\.onfail\\.send>", pluginRef.getSettings.onFailSend);


        // OnFail Triggers
        for (FAILTRIGGERS action : FAILTRIGGERS.values())
        {
            if (message.toLowerCase().contains("<setting.triggers." + action.toString().toLowerCase() + ">")) {
                if (pluginRef.getSettings.failTriggers.contains(action))
                    message = message.replaceAll("<setting\\.triggers\\." + action.toString().toLowerCase() + ">","✔\",\"color\":\"yellow" );
                else
                    message = message.replaceAll("<setting\\.triggers\\." + action.toString().toLowerCase() + ">","X\",\"color\":\"red" );
            }
            if (message.toLowerCase().contains("<setting.triggers." + action.toString().toLowerCase() + ".value>")) {
                if (action == FAILTRIGGERS.BOUNTY)
                message = message.replaceAll("<setting\\.triggers\\." + action.toString().toLowerCase() + ".value>", String.valueOf(pluginRef.getSettings.failBounty));
            }
        }


        //Penalties
        if (message.toLowerCase().contains("<setting.penalty.escaped>"))
            message = message.replaceAll("<setting\\.penalty\\.escaped>",String.valueOf(pluginRef.getSettings.penaltyStatusEscaped));
        if (message.toLowerCase().contains("<setting.penalty.wanted>"))
            message = message.replaceAll("<setting\\.penalty\\.wanted>",String.valueOf(pluginRef.getSettings.penaltyStatusWanted));
        if (message.toLowerCase().contains("<setting.penalty.jailed>"))
            message = message.replaceAll("<setting\\.penalty\\.jailed>",String.valueOf(pluginRef.getSettings.penaltyStatusJailed));


        //Player Messages
        if (sender instanceof Player) {
            PlayerMission plrMission = pluginRef.playerStorage.get(((Player) sender).getUniqueId());
            if (message.toLowerCase().contains("<player.score>"))
                message = message.replaceAll("<player\\.score>", plrMission.getScore().toString());
            if (message.toLowerCase().contains("<player.scorefmt>"))
                message = message.replaceAll("<player\\.scorefmt>", plrMission.getScoreFormatted());
            if (message.toLowerCase().contains("<player.targetnpc>"))
                message = message.replaceAll("<player\\.targetnpc>", plrMission.getNPCName());
            if (message.toLowerCase().contains("<player.item>")) {
                if (plrMission.actionItem.getItemMeta().getDisplayName().equals(""))
                    message = message.replaceAll("<player\\.item>", plrMission.actionItem.getType().name());
                else
                    message = message.replaceAll("<player\\.item>", plrMission.actionItem.getItemMeta().getDisplayName());
            }
        }

        //NPC Messages
        if (npc != null) {
            if (message.toLowerCase().contains("<npc.id>"))
                message = message.replaceAll("<npc\\.id>", Integer.toString(npc.getId()));
            if (message.toLowerCase().contains("<npc.name>"))
                message = message.replaceAll("<npc\\.name>", npc.getName().replace("[", "").replace("]", "]"));
            if (message.toLowerCase().contains("<npc.spawned>"))
                message = message.replaceAll("<npc\\.spawned>", npc.isSpawned() ? this.getResultMessage("result_Messages.true_text")[0] : this.getResultMessage("result_Messages.false_text")[0]);
            if (message.toLowerCase().contains("<npc.location>"))
                message = message.replaceAll("<npc\\.location>", "(" + npc.getEntity().getLocation().getBlockX() + "," + npc.getEntity().getLocation().getBlockY() + "," + npc.getEntity().getLocation().getBlockZ() + ")");
            if (message.toLowerCase().contains("<npc.type>"))
                message = message.replaceAll("<npc\\.type>", npc.getEntity().getType().name());
        }

        //Target NPC Messages
        if (targetNPC != null) {
            if (message.toLowerCase().contains("<targetnpc.id>"))
                message = message.replaceAll("<targetnpc\\.id>", Integer.toString(targetNPC.getId()));
            if (message.toLowerCase().contains("<targetnpc.name>"))
                message = message.replaceAll("<targetnpc\\.name>", targetNPC.getName().replace("[", "").replace("]", "]"));
            if (message.toLowerCase().contains("<targetnpc.spawned>"))
                message = message.replaceAll("<targetnpc\\.spawned>", targetNPC.isSpawned() ? this.getResultMessage("result_Messages.true_text")[0] : this.getResultMessage("result_Messages.false_text")[0]);
            if (message.toLowerCase().contains("<targetnpc.location>"))
                message = message.replaceAll("<targetnpc\\.location>", "(" + targetNPC.getEntity().getLocation().getBlockX() + "," + targetNPC.getEntity().getLocation().getBlockY() + "," + targetNPC.getEntity().getLocation().getBlockZ() + ")");
            if (message.toLowerCase().contains("<targetnpc.type>"))
                message = message.replaceAll("<targetnpc\\.type>", targetNPC.getEntity().getType().name());
        }

        return message;
    }

    public String[] getResultMessage(String msgKey) {
        String language = pluginRef.currentLanguage;
        msgKey = msgKey.toLowerCase();
        List<String> response = new ArrayList<String>();

        if (!pluginRef.getLanguageManager.languageStorage.containsKey(language + "-thejail")) {
            logToConsole("Missing language file [" + language + "-thejail" + "] Check your config files.");
            language = "en_def";
        }

        if (!pluginRef.getLanguageManager.languageStorage.containsKey(language + "-thejail")) {
            logToConsole("Missing language file [" + language + "-thejail" + "] Check your config files.");
            response.add("Language file failure. Contact the servers admin");
            return response.toArray(new String[response.size()]);
        }

        if (!pluginRef.getLanguageManager.languageStorage.get(language + "-thejail").contains(msgKey)) {
            language = "en_def";
        }

        if (pluginRef.getLanguageManager.languageStorage.get(language + "-thejail").isList(msgKey)) {
            response.addAll(pluginRef.getLanguageManager.languageStorage.get(language + "-thejail").getStringList(msgKey));
        } else {
            response.add(pluginRef.getLanguageManager.languageStorage.get(language + "-thejail").getString(msgKey));
        }
        return response.toArray(new String[response.size()]);
    }

}
