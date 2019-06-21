package net.livecar.nuttyworks.thejail.playerdata;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_police.API;
import net.livecar.nuttyworks.npc_police.api.Enumerations;
import net.livecar.nuttyworks.npc_police.api.managers.PlayerManager;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.COMPLETEDACTION;
import net.livecar.nuttyworks.thejail.enumerations.FAILACTION;
import net.livecar.nuttyworks.thejail.enumerations.FAILTRIGGERS;
import net.livecar.nuttyworks.thejail.enumerations.MISSIONTYPE;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerMission
{

	private TheJail_Plugin pluginRef 	= null;

	public PlayerMission(TheJail_Plugin plugin, Player plr)
	{
		pluginRef = plugin;
		player = plr;
	}

	private Player          player			= null;
	private Long			bestScore 		= 0L;

	public UUID				lastSpottedBy;
	public int 				penalty			= 0;
	public LocalDateTime 	joinedTime		= null;
	public LocalDateTime 	completedTime	= null;
	
	public int 				targetNPC		= 0;
	public int 				originalTarget 	= 0;
	
	public MISSIONTYPE 		originalAction 	= MISSIONTYPE.NONE;
	public MISSIONTYPE 		requestedAction	= MISSIONTYPE.NONE;
	public ItemStack 		actionItem		= null;

	public String getNPCName()
	{
		if (targetNPC == 0)
			return "";

		if (pluginRef.getCitizensPlugin.getNPCRegistry().getById(targetNPC) == null)
			return "";

		return pluginRef.getCitizensPlugin.getNPCRegistry().getById(targetNPC).getName();
	}

	public NPC getTargetNPC()
	{
		if (targetNPC == 0)
			return null;

		if (pluginRef.getCitizensPlugin.getNPCRegistry().getById(targetNPC) == null)
			return null;

		return pluginRef.getCitizensPlugin.getNPCRegistry().getById(targetNPC);

	}

	public Long getScore()
	{
		if (this.joinedTime == null)
			return 0L;

		LocalDateTime tempTime = this.joinedTime;
		LocalDateTime scoreTime = completedTime!=null?completedTime:LocalDateTime.now();
		return tempTime.until(scoreTime, ChronoUnit.SECONDS) + this.penalty;
	}

    public String getScoreFormatted()
    {
        if (requestedAction == MISSIONTYPE.NONE)
            return "00:00:00";

        if (joinedTime == null)
            return "00:00:00";

        LocalDateTime curTime = completedTime!=null?completedTime:LocalDateTime.now();
        LocalDateTime tmpTime = joinedTime;

        long timeHours = tmpTime.until(curTime, ChronoUnit.HOURS);
        tmpTime = tmpTime.plusHours(timeHours);

        long timeMinutes = tmpTime.until(curTime, ChronoUnit.MINUTES);
        tmpTime = tmpTime.plusMinutes(timeMinutes);

        long timeSeconds = tmpTime.until(curTime, ChronoUnit.SECONDS);

        String response = "";

        if (timeHours > 0)
            response = String.valueOf(timeHours) + " " + pluginRef.getMessageManager.getResultMessage("result_messages.hours") + " ";
        if (timeMinutes > 0)
            response = response + String.valueOf(timeMinutes) + " " + pluginRef.getMessageManager.getResultMessage("result_messages.minutes") + " ";
        if (timeSeconds > 0)
            response = response + String.valueOf(timeSeconds) + " " + pluginRef.getMessageManager.getResultMessage("result_messages.seconds") + " ";

        return response.trim();
    }


    public PlayerScore getScoreRecord()
	{
		return new PlayerScore(this.player.getUniqueId(),getScore(),this.originalAction.toString());
	}

	public boolean triggerFail(FAILTRIGGERS failreason)
	{
		if (requestedAction != MISSIONTYPE.NONE)
		{
			switch (failreason)
			{
				case DEATH:
					pluginRef.getMessageManager.sendMessage(this.player, "player_messages.failed.death", pluginRef.getCitizensUtils.getFirstQuester(), null);
					break;
				case ARRESTED:
					pluginRef.getMessageManager.sendMessage(this.player, "player_messages.failed.arrested", pluginRef.getCitizensUtils.getFirstQuester(), null);
					break;
				case WANTED:
					pluginRef.getMessageManager.sendMessage(this.player, "player_messages.failed.wanted", pluginRef.getCitizensUtils.getFirstQuester(), null);
					break;
				case ESCAPED:
					pluginRef.getMessageManager.sendMessage(this.player, "player_messages.failed.escaped", pluginRef.getCitizensUtils.getFirstQuester(), null);
					break;
				case MURDER:
					pluginRef.getMessageManager.sendMessage(this.player, "player_messages.failed.murder", pluginRef.getCitizensUtils.getFirstQuester(), null);
					break;
				case BOUNTY:
					pluginRef.getMessageManager.sendMessage(this.player, "player_messages.failed.bounty", pluginRef.getCitizensUtils.getFirstQuester(), null);
					break;
			}

			if (pluginRef.getSettings.onFailActions.contains(FAILACTION.CLEARINVENTORY))
			{
				player.getInventory().clear();
			}
			if (pluginRef.getSettings.onFailActions.contains(FAILACTION.TELEPORT))
			{
				if (pluginRef.getSettings.onFailLocation != null)
					player.teleport(pluginRef.getSettings.onFailLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
			}
			if (pluginRef.getSettings.onFailCommands.size() > 0) {
				for (String cmd : pluginRef.getSettings.onFailCommands) {
					pluginRef.getServer().dispatchCommand(pluginRef.getServer().getConsoleSender(), MessageFormat.format(cmd, player.getName()));
				}
			}
			if (pluginRef.getSettings.onFailActions.contains(FAILACTION.SEND))
			{
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("Connect");
						out.writeUTF(pluginRef.getSettings.onFailSend);
						player.sendPluginMessage(pluginRef, "BungeeCord", out.toByteArray());
					}
				}.runTaskLater(pluginRef, 40);
			}
		}

		this.joinedTime = null;
		this.completedTime = null;
		this.requestedAction = MISSIONTYPE.NONE;
		this.originalAction = MISSIONTYPE.NONE;
		this.actionItem = null;
		this.targetNPC = 0;
		this.penalty = 0;
		this.originalTarget = 0;

		// Don't allow the player to be killed!
		if (pluginRef.getSettings.onFailLocation != null || !pluginRef.getSettings.onFailSend.equals(""))
			return true;

		return false;
	}

	public void startMission(NPC npc)
	{
		//Give a random quest.
		List<Integer> targetNPCS = pluginRef.getCitizensUtils.getTargetNPCList();

		Material[] items = new Material[]{Material.STICK, Material.PAPER, Material.APPLE};
		Random rnd = new Random(new Date().getTime());

		//select a random NPC
		if (targetNPCS.size() == 1)
			this.targetNPC = targetNPCS.get(0);
		else
			this.targetNPC = targetNPCS.get(rnd.nextInt(targetNPCS.size() - 1));

		this.originalTarget = this.targetNPC;
		ItemStack stack = new ItemStack(items[rnd.nextInt(items.length - 1)], 1);
		ItemMeta im = stack.getItemMeta();
		stack.setItemMeta(im);
		this.actionItem = stack;

		this.requestedAction = MISSIONTYPE.values()[rnd.nextInt(MISSIONTYPE.values().length - 2)];
		if (this.requestedAction == MISSIONTYPE.RETURN)
			this.requestedAction = MISSIONTYPE.IMPORT;

		this.originalAction = this.requestedAction;

		NPC target = CitizensAPI.getNPCRegistry().getById(this.targetNPC);

		switch (this.requestedAction) {
			case BEATUP:
				pluginRef.getMessageManager.sendMessage(player, "player_messages.mission.beatup", npc, target);
				break;
			case EXPORT:
				pluginRef.getMessageManager.sendMessage(player, "player_messages.mission.export", npc, target);
				break;
			case IMPORT:
				pluginRef.getMessageManager.sendMessage(player, "player_messages.mission.import", npc, target);
				player.getInventory().addItem(new ItemStack(stack));
				break;
			case KILL:
				pluginRef.getMessageManager.sendMessage(player, "player_messages.mission.import", npc, target);
				break;
			case RETURN:
				break;
			case ESCAPE:
				break;
			case NONE:
				break;
			default:
				break;
		}
		this.joinedTime = LocalDateTime.now();
	}

	public void endMission(NPC npc)
	{
		PlayerManager plrMgr = API.getPlayerManager(player);
		if (plrMgr.getCurrentStatus() != Enumerations.CURRENT_STATUS.FREE) {
			pluginRef.getMessageManager.sendMessage(player, "player_messages.return.wanted", npc, null);
		} else {
			this.requestedAction = MISSIONTYPE.ESCAPE;
			pluginRef.getMessageManager.sendMessage(player, "player_messages.return.finished", npc, null);

			completedTime = LocalDateTime.now();
			pluginRef.getDatabaseManager.queueSaveScoreRequest(getScoreRecord());

			if (pluginRef.getSettings.onCompletedLocation != null)
            {
                player.teleport(pluginRef.getSettings.onCompletedLocation);
            }
            if (!pluginRef.getSettings.onCompletedSend.equals(""))
            {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(pluginRef.getSettings.onCompletedSend);
                        player.sendPluginMessage(pluginRef, "BungeeCord", out.toByteArray());
                    }
                }.runTaskLater(pluginRef, 40);
            }
            if (pluginRef.getSettings.onCompletedCommands.size()>0)
            {
                if (pluginRef.getSettings.onCompletedCommands.size()>0) {
                    for (String cmd : pluginRef.getSettings.onCompletedCommands) {
                        pluginRef.getServer().dispatchCommand(pluginRef.getServer().getConsoleSender(), MessageFormat.format(cmd, player.getName()));
                    }
                }
            }
            if (pluginRef.getSettings.onCompletedActions.contains(COMPLETEDACTION.RESET))
            {
                this.joinedTime = null;
                this.completedTime = null;
                this.requestedAction = MISSIONTYPE.NONE;
                this.originalAction = MISSIONTYPE.NONE;
                this.actionItem = null;
                this.targetNPC = 0;
                this.penalty = 0;
                this.originalTarget = 0;
            }
        }
	}

    public boolean isItemSimular(ItemStack sourceItem, ItemStack compareItem) {

        if (sourceItem.getType() != compareItem.getType())
            return false;

        if (sourceItem.getItemMeta().hasLore() && compareItem.getItemMeta().hasLore()) {
            if (sourceItem.getItemMeta().getLore().size() != compareItem.getItemMeta().getLore().size())
                return false;

            for (int n = 0; n < sourceItem.getItemMeta().getLore().size(); n++) {
                if (!sourceItem.getItemMeta().getLore().get(n).equalsIgnoreCase(compareItem.getItemMeta().getLore().get(n)))
                    return false;
            }
        }

        if (sourceItem.getItemMeta().hasEnchants() != compareItem.getItemMeta().hasEnchants())
            return false;

        if (sourceItem.getItemMeta().hasEnchants() && compareItem.getItemMeta().hasEnchants()) {
            if (sourceItem.getItemMeta().getEnchants().size() != compareItem.getItemMeta().getEnchants().size())
                return false;

            for (Map.Entry<Enchantment, Integer> chant : sourceItem.getItemMeta().getEnchants().entrySet()) {
                if (!compareItem.getItemMeta().getEnchants().containsKey(chant.getKey())) {
                    return false;
                }
            }
        }

        if (compareItem.getItemMeta().hasDisplayName() != sourceItem.getItemMeta().hasDisplayName())
            return false;

        if (compareItem.getItemMeta().hasDisplayName() && sourceItem.getItemMeta().hasDisplayName()) {
            if (!compareItem.getItemMeta().getDisplayName().equalsIgnoreCase(sourceItem.getItemMeta().getDisplayName()))
                return false;
        }

        return true;
    }
}
