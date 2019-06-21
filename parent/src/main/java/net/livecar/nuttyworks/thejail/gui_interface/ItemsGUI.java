package net.livecar.nuttyworks.thejail.gui_interface;

import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.GUIMENUTYPE;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemsGUI implements Listener {
    private Player owningPlayer;
    private TheJail_Plugin pluginRef;
    private Inventory inventory;
    private String exitMessage;
    private GUIMENUTYPE guiType;

    public ItemsGUI(String name, int size, TheJail_Plugin pluginRef, Player player, String exitMessage, GUIMENUTYPE guitype) {
        this.pluginRef = pluginRef;
        this.owningPlayer = player;
        this.exitMessage = exitMessage;
        this.guiType = guitype;

        pluginRef.getServer().getPluginManager().registerEvents(this, pluginRef);
        inventory = Bukkit.createInventory(player, size, name);
    }

    public void setSlotItem(int index, ItemStack item) {
        inventory.setItem(index, item);
    }

    public void open() {
        owningPlayer.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().hashCode() == inventory.hashCode()) {

            switch (this.guiType)
            {

                case ONJOINCOMMANDS:
                    pluginRef.getSettings.onJoinCommands = pluginRef.getMCVersionUtils.itemStackToStringList(inventory.getContents());
                    break;
                case ONFAILCOMMANDS:
                    pluginRef.getSettings.onFailCommands = pluginRef.getMCVersionUtils.itemStackToStringList(inventory.getContents());
                    break;
                case ONCOMPLETECOMMANDS:
                    pluginRef.getSettings.onCompletedCommands = pluginRef.getMCVersionUtils.itemStackToStringList(inventory.getContents());
                    break;
                case MISSIONITEMS:
                    pluginRef.getSettings.missionItems = inventory.getContents();
                    pluginRef.getMessageManager.sendMessage(owningPlayer, exitMessage);
                    break;
            }
            destroy();
        }
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        owningPlayer = null;
        inventory = null;
        pluginRef = null;
    }
}
