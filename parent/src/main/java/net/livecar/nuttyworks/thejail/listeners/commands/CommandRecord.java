package net.livecar.nuttyworks.thejail.listeners.commands;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandRecord {
    public String       commandName         = "";
    public String       groupName           = "";
    public String       commandPermission   = "";
    public String       badArgumentsMessage = "";
    public String       helpMessage         = "";
    public String[]     arguments           = null;
    public Boolean      allowConsole        = false;
    public int          minArguments        = 0;
    public int          maxArguments        = 50;

    private Class<?>    commandClass        = null;
    private Method      commandMethod       = null;

    public CommandRecord(String commandName, String groupName, String commandPermission, String badArgumentsMessage, String helpMessage, Boolean allowConsole, int minArguments, int maxArguments, String[] arguments, Class<?> commandClass, String commandMethod) {
        this.commandName = commandName;
        this.groupName = groupName;
        this.commandPermission = commandPermission;
        this.badArgumentsMessage = badArgumentsMessage;
        this.helpMessage = helpMessage;
        this.allowConsole = allowConsole;
        this.minArguments = minArguments;
        this.maxArguments = maxArguments;
        this.arguments = arguments;
        this.commandClass = commandClass;
        this.commandMethod = getMethod(commandClass, commandMethod);
    }

    public boolean invokeCommand(TheJail_Plugin pluginRef, CommandSender sender, NPC npc, String[] inargs) {
        try {
            Constructor<?> ctr = commandClass.getConstructor();
            ctr.setAccessible(true);

            return (boolean) commandMethod.invoke(ctr.newInstance(), pluginRef, sender, npc, inargs);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | InstantiationException | NoSuchMethodException e) {
            // Oops!
            e.printStackTrace();
        }
        return false;
    }

    private Method getMethod(Class<?> commandClass, String methodName) {
        try {
            return commandClass.getMethod(methodName, TheJail_Plugin.class, CommandSender.class, NPC.class, String[].class);
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }
}
