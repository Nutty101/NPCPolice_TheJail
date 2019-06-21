package net.livecar.nuttyworks.thejail.listeners;

import net.livecar.nuttyworks.npc_police.api.Enumerations;
import net.livecar.nuttyworks.npc_police.api.events.BountyChangedEvent;
import net.livecar.nuttyworks.npc_police.api.events.PlayerSpottedEvent;
import net.livecar.nuttyworks.npc_police.api.events.StatusChangedEvent;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.FAILTRIGGERS;
import net.livecar.nuttyworks.thejail.enumerations.MISSIONTYPE;
import net.livecar.nuttyworks.thejail.playerdata.PlayerMission;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class NPCPoliceEvents implements org.bukkit.event.Listener {

    private TheJail_Plugin pluginRef 	= null;

    public NPCPoliceEvents(TheJail_Plugin plugin)
    {
        this.pluginRef = plugin;
    }

    @EventHandler
    public void onPlayerSpotted(PlayerSpottedEvent event) {

        Player plr = (Player)event.getPlayer();
        PlayerMission plrData = pluginRef.playerStorage.get(event.getPlayer().getUniqueId());

        if (plrData.lastSpottedBy == null)
        {
            plrData.lastSpottedBy = event.getPlayerManager().getLastSpottedBy().getEntity().getUniqueId();
            pluginRef.getMessageManager.sendMessage(plr, "player_messages.penalty.spotted",pluginRef.getCitizensUtils.getFirstQuester(),event.getPlayerManager().getLastSpottedBy());
        } else if (plrData.lastSpottedBy.equals(event.getPlayerManager().getLastSpottedBy().getEntity().getUniqueId())) {
            //
        } else {
            plrData.lastSpottedBy = event.getPlayerManager().getLastSpottedBy().getEntity().getUniqueId();
            pluginRef.getMessageManager.sendMessage(plr, "player_messages.penalty.spotted", pluginRef.getCitizensUtils.getFirstQuester(), event.getPlayerManager().getLastSpottedBy());
        }
    }

        @EventHandler
    public void onStatusChanged(StatusChangedEvent event)
    {

        if (event.getStatus() == Enumerations.CURRENT_STATUS.FREE)
            return;

        Player plr = (Player)event.getPlayer();
        PlayerMission plrData = pluginRef.playerStorage.get(event.getPlayer().getUniqueId());

        if (event.getPlayerManager().getPriorStatus() != event.getPlayerManager().getCurrentStatus())
        {
            switch (event.getStatus())
            {
                case ESCAPED:
                    if (pluginRef.getSettings.failTriggers.contains(FAILTRIGGERS.ESCAPED))
                    {
                        plrData.triggerFail(FAILTRIGGERS.ESCAPED);
                    } else {
                        pluginRef.getMessageManager.sendMessage(plr, "player_messages.penalty.escaped",pluginRef.getCitizensUtils.getFirstQuester(),event.getPlayerManager().getLastSpottedBy());
                        plrData.penalty += pluginRef.getSettings.penaltyStatusEscaped;
                    }
                    break;
                case JAILED:
                    if (pluginRef.getSettings.failTriggers.contains(FAILTRIGGERS.ARRESTED))
                    {
                        plrData.triggerFail(FAILTRIGGERS.ARRESTED);
                    } else {
                        pluginRef.getMessageManager.sendMessage(plr, "player_messages.penalty.jailed",pluginRef.getCitizensUtils.getFirstQuester(),event.getPlayerManager().getLastSpottedBy());
                        plrData.penalty += pluginRef.getSettings.penaltyStatusJailed;
                    }
                    break;
                case WANTED:
                    if (pluginRef.getSettings.failTriggers.contains(FAILTRIGGERS.WANTED))
                    {
                        plrData.triggerFail(FAILTRIGGERS.WANTED);
                    } else {
                        pluginRef.getMessageManager.sendMessage(plr, "player_messages.penalty.wanted",pluginRef.getCitizensUtils.getFirstQuester(),event.getPlayerManager().getLastSpottedBy());
                        plrData.penalty += pluginRef.getSettings.penaltyStatusWanted;
                    }
                    break;
                default:
                    break;

            }
        }
    }

    @EventHandler
    public void onBountyChanged(BountyChangedEvent event)
    {
        if (pluginRef.getSettings.failTriggers.contains(FAILTRIGGERS.BOUNTY)) {
            Player plr = (Player) event.getPlayer();
            PlayerMission plrData = pluginRef.playerStorage.get(event.getPlayer().getUniqueId());

            if (plrData.requestedAction != MISSIONTYPE.NONE && pluginRef.getSettings.failBounty < event.getBounty()) {
                plrData.triggerFail(FAILTRIGGERS.BOUNTY);
            }
        }
    }


}
