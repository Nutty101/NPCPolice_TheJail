package net.livecar.nuttyworks.thejail.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.STATESETTING;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BungeeCordListener implements PluginMessageListener {
    // BungeeCord Checks
    public STATESETTING bungeeCordEnabled = STATESETTING.NOTSET;

    private TheJail_Plugin pluginRef = null;
    private String[] serverList = new String[]{"none"};

    public BungeeCordListener(TheJail_Plugin pluginRef) {
        this.pluginRef = pluginRef;

        pluginRef.getServer().getMessenger().registerOutgoingPluginChannel(pluginRef, "BungeeCord");
        pluginRef.getServer().getMessenger().registerIncomingPluginChannel(pluginRef, "BungeeCord", this);
    }

    public void startBungeeChecks(Player player) {
        if (bungeeCordEnabled != STATESETTING.NOTSET)
            return;

        this.bungeeCordEnabled = STATESETTING.FALSE;

        final Player fnlPlayer = player;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("GetServers");

                fnlPlayer.sendPluginMessage(pluginRef, "BungeeCord", out.toByteArray());
            }
        }, 500);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player plr, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (!subchannel.equalsIgnoreCase("GetServers"))
            return;

        String serverList = in.readUTF();
        if (!serverList.isEmpty()) {
            this.serverList = serverList.trim().split("\\s*,\\s*");
        }
    }

    public List<String> getServerList() {
        return Arrays.asList(serverList);
    }

    public void switchServer(Player plr, String serverName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeUTF("Connect");
            dos.writeUTF(serverName);
            plr.sendPluginMessage(pluginRef, "BungeeCord", baos.toByteArray());
            baos.close();
            dos.close();
        } catch (IOException e) {
        }
    }

}
