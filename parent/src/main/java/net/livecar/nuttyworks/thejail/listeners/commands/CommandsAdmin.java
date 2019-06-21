package net.livecar.nuttyworks.thejail.listeners.commands;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.annotations.CommandInfo;
import net.livecar.nuttyworks.thejail.enumerations.MISSIONTYPE;
import net.livecar.nuttyworks.thejail.playerdata.PlayerScore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandsAdmin {
    @CommandInfo(
            name = "reload",
            group = "Admin Commands",
            badArgumentsMessage = "command_reload_args",
            helpMessage = "command_reload_help",
            arguments = {""},
            permission = "thejail.admin.reload",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
    )
    public boolean jailConfig_ReloadConfig(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {
        pluginRef.getSettings.loadSettings();
        pluginRef.getLanguageManager.loadLanguages(true);
        if (sender instanceof Player)
            pluginRef.getMessageManager.sendMessage(sender, "general_messages.configs_reloaded", ((Player) sender).getDisplayName().toString());
        if (!(sender instanceof Player))
            pluginRef.getMessageManager.sendMessage(sender, "console_messages.configs_reloaded");
        return true;
    }

    @CommandInfo(
            name = "version",
            group = "Admin Commands",
            badArgumentsMessage = "command_version_args",
            helpMessage = "command_version_help",
            arguments = {""},
            permission = "thejail.admin.version",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
    )
    public boolean jailConfig_CurrentVersion(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {
        // Configuration Commands
        if (sender instanceof Player) {
            pluginRef.getMessageManager.sendJsonRaw((Player) sender, "[{\"text\":\"--\",\"color\":\"dark_aqua\"},{\"text\":\"[\",\"color\":\"white\"},{\"text\":\"The Jail By Nutty101\",\"color\":\"yellow\"},{\"text\":\"]\",\"color\":\"white\"},{\"text\":\"-----------------------\",\"color\":\"dark_aqua\"}]");
            pluginRef.getMessageManager.sendJsonRaw((Player) sender, "[{\"text\":\"Version\",\"color\":\"green\"},{\"text\":\":\",\"color\":\"yellow\"},{\"text\":\" " + pluginRef.getDescription().getVersion() + " \",\"color\":\"white\"}]");
            pluginRef.getMessageManager.sendJsonRaw((Player) sender, "[{\"text\":\"Plugin Link\",\"color\":\"dark_green\"},{\"text\":\": \",\"color\":\"yellow\"},{\"text\":\"https://www.spigotmc.org/resources/npc-police.9553/\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.spigotmc.org/resources/npcpolice_thejail.9553/\"}}]");
        } else {
            sender.sendMessage("--[The Jail By Nutty101]-----------------------");
            sender.sendMessage("Version: " + pluginRef.getDescription().getVersion());
            sender.sendMessage("Plugin Link: https://www.spigotmc.org/resources/npcpolice_thejail.9553");
        }
        return true;
    }

    @CommandInfo(
            name = "save",
            group = "Admin Commands",
            badArgumentsMessage = "command_save_args",
            helpMessage = "command_save_help",
            arguments = {""},
            permission = "thejail.admin.save",
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
    )
    public boolean theJail_SaveConfig(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {
        pluginRef.getSettings.saveSettings();
        return true;
    }

}
