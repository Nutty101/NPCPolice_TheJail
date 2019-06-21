package net.livecar.nuttyworks.thejail.listeners;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_police.API;
import net.livecar.nuttyworks.npc_police.api.Enumerations;
import net.livecar.nuttyworks.npc_police.api.managers.PlayerManager;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.citizens.TheJailQuesterTrait;
import net.livecar.nuttyworks.thejail.enumerations.FAILTRIGGERS;
import net.livecar.nuttyworks.thejail.enumerations.JOINACTION;
import net.livecar.nuttyworks.thejail.enumerations.MISSIONTYPE;
import net.livecar.nuttyworks.thejail.playerdata.PlayerMission;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.MessageFormat;
import java.time.LocalDateTime;

public class BukkitEvents implements org.bukkit.event.Listener {

    private TheJail_Plugin pluginRef = null;

    public BukkitEvents(TheJail_Plugin plugin) {
        this.pluginRef = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        //remove their info
        if (pluginRef.playerStorage.containsKey(event.getPlayer().getUniqueId())) {
            pluginRef.playerStorage.remove(event.getPlayer().getUniqueId());
        }

        PlayerMission plrData = new PlayerMission(pluginRef,player);
        plrData.joinedTime = LocalDateTime.now();
        plrData.targetNPC = 0;
        plrData.requestedAction = MISSIONTYPE.NONE;
        pluginRef.playerStorage.put(player.getUniqueId(), plrData);

        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerManager plrMgr = API.getPlayerManager(player);
                plrMgr.clearBounty();
                plrMgr.setCurrentStatus(Enumerations.CURRENT_STATUS.FREE);
            }
        }.runTaskLater(pluginRef, 20);

        if (pluginRef.getSettings.onJoinActions.contains(JOINACTION.TELEPORT)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(pluginRef.getSettings.onJoinLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }.runTaskLater(pluginRef, 20);
        }

        if (pluginRef.getSettings.onJoinActions.contains(JOINACTION.CLEARINVENTORY)) {
            player.getInventory().clear();
            player.updateInventory();
        }

        if (pluginRef.getSettings.onJoinCommands.size()>0) {
            for (String cmd : pluginRef.getSettings.onJoinCommands) {
                pluginRef.getServer().dispatchCommand(pluginRef.getServer().getConsoleSender(), MessageFormat.format(cmd, player.getName()));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        //remove their info
        if (pluginRef.playerStorage.containsKey(event.getPlayer().getUniqueId())) {
            pluginRef.playerStorage.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (!event.getHand().equals(EquipmentSlot.HAND))
            return;

        if (event.getRightClicked().hasMetadata("NPC")) {
            NPC npc = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked());
            PlayerMission plrData = pluginRef.playerStorage.get(event.getPlayer().getUniqueId());
            if (plrData.requestedAction == MISSIONTYPE.ESCAPE)
                return;

            if (npc.hasTrait(TheJailQuesterTrait.class)) {
                if (plrData.requestedAction == MISSIONTYPE.NONE) {
                    plrData.startMission(npc);
                    event.setCancelled(true);
                    return;
                } else if (plrData.requestedAction == MISSIONTYPE.RETURN) {
                    if (plrData.originalAction == MISSIONTYPE.EXPORT)
                    {
                        ItemStack inHandItem = pluginRef.getMCVersionUtils.getMainHand(event.getPlayer());
                        if (!plrData.isItemSimular(inHandItem, plrData.actionItem))
                        {
                            pluginRef.getMessageManager.sendMessage(event.getPlayer(), "player_messages.return.wrongitem", npc, null);
                            event.setCancelled(true);
                            return;
                        } else {

                        }
                    }
                    plrData.endMission(npc);
                    event.setCancelled(true);
                    return;
                }
            }

            if (plrData.targetNPC == npc.getId()) {
                PlayerManager plrMgr = API.getPlayerManager(event.getPlayer());
                if (plrMgr.getCurrentStatus() != Enumerations.CURRENT_STATUS.FREE) {
                    NPC target = CitizensAPI.getNPCRegistry().getById(plrData.targetNPC);
                    pluginRef.getMessageManager.sendMessage(event.getPlayer(), "player_messages.return.wanted", npc, target);
                } else {
                    NPC target = CitizensAPI.getNPCRegistry().getById(plrData.targetNPC);
                    switch (plrData.requestedAction) {
                        case BEATUP:
                            pluginRef.getMessageManager.sendMessage(event.getPlayer(), "player_messages.target.beatup", npc, target);
                            break;
                        case EXPORT:
                            pluginRef.getMessageManager.sendMessage(event.getPlayer(), "player_messages.target.export", npc, target);
                            event.getPlayer().getInventory().addItem(plrData.actionItem);
                            plrData.requestedAction = MISSIONTYPE.RETURN;
                            break;
                        case IMPORT:
                            ItemStack inHandItem = pluginRef.getMCVersionUtils.getMainHand(event.getPlayer());
                            if (!plrData.isItemSimular(inHandItem, plrData.actionItem))
                            {
                                pluginRef.getMessageManager.sendMessage(event.getPlayer(), "player_messages.return.wrongitem", npc, null);
                                event.setCancelled(true);
                                return;
                            }


                            if (pluginRef.getMCVersionUtils.getMainHand(event.getPlayer()).equals(plrData.actionItem)) {
                                pluginRef.getMessageManager.sendMessage(event.getPlayer(), "player_messages.target.import", npc, target);
                                plrData.requestedAction = MISSIONTYPE.RETURN;
                                event.getPlayer().getInventory().clear();
                            }
                            break;
                        case KILL:
                            pluginRef.getMessageManager.sendMessage(event.getPlayer(), "player_messages.target.murder", npc, target);
                            break;
                        default:
                            break;
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() == null) {
            return;
        }

        if ((event.getDamager() instanceof Player) && event.getEntity().hasMetadata("NPC")) {
            NPC npc = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
            Player plr = (Player) event.getDamager();
            PlayerMission plrData = pluginRef.playerStorage.get(event.getDamager().getUniqueId());
            PlayerManager plrMgr = API.getPlayerManager(plr);

            if (plrData.requestedAction == MISSIONTYPE.ESCAPE)
                return;

            if (((LivingEntity) event.getEntity()).getHealth() < 11) {
                if (plrMgr.getCurrentStatus() != Enumerations.CURRENT_STATUS.FREE) {
                    pluginRef.getMessageManager.sendMessage(event.getDamager(), "player_messages.damage.hasstatus", npc, plrData.getTargetNPC());
                    event.setCancelled(true);
                    return;
                } else {
                    if (plrData.requestedAction == MISSIONTYPE.BEATUP && plrData.targetNPC == npc.getId()) {
                        pluginRef.getMessageManager.sendMessage(event.getDamager(), "player_messages.damage.beatup", npc, plrData.getTargetNPC());
                        plrData.requestedAction = MISSIONTYPE.RETURN;
                        plrData.targetNPC = 0;
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if (((LivingEntity) event.getEntity()).getHealth() - event.getDamage() < 1) {
                if (plrData.targetNPC == 0) {
                    if (pluginRef.getCitizensUtils.getTargetNPCList().contains(npc.getId())) {
                        if (plrData.originalTarget == npc.getId() && plrData.originalAction != MISSIONTYPE.KILL) {
                            pluginRef.getMessageManager.sendMessage(event.getDamager(), "player_messages.damage.wrongmurder", npc, npc);

                            if (pluginRef.getSettings.failTriggers.contains(FAILTRIGGERS.MURDER))
                            {
                                plrData.triggerFail(FAILTRIGGERS.MURDER);
                            } else {
                                plrData.penalty += 120;
                                if (plrMgr.getCurrentStatus() == Enumerations.CURRENT_STATUS.FREE)
                                    plrMgr.setCurrentStatus(Enumerations.CURRENT_STATUS.WANTED);
                            }
                        }
                    }
                }

                if (plrMgr.getCurrentStatus() != Enumerations.CURRENT_STATUS.FREE) {
                    pluginRef.getMessageManager.sendMessage(event.getDamager(), "player_messages.damage.wantedmurder", npc, npc);
                    return;
                } else {
                    if (plrData.requestedAction == MISSIONTYPE.KILL && plrData.targetNPC == npc.getId()) {
                        pluginRef.getMessageManager.sendMessage(event.getDamager(), "player_messages.damage.murder", npc, npc);
                        plrData.requestedAction = MISSIONTYPE.RETURN;
                        plrData.targetNPC = 0;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void PlayerDamageReceive(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();

            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();

                if ((damaged.getHealth() - event.getDamage()) <= 0) {
                    //Death is coming.
                    if (pluginRef.playerStorage.containsKey(damaged.getUniqueId())) {
                        PlayerMission plrData = pluginRef.playerStorage.get(damaged.getUniqueId());
                        if (pluginRef.getSettings.failTriggers.contains(FAILTRIGGERS.DEATH))
                        {
                            if (!plrData.triggerFail(FAILTRIGGERS.DEATH))
                            {
                                event.setCancelled(true);
                                damaged.setHealth(0.5);
                            }
                        }
                    }
                }
            }
        }
    }
}

