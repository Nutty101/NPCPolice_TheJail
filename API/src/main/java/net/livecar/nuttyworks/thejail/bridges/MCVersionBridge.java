package net.livecar.nuttyworks.thejail.bridges;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public abstract class MCVersionBridge {

    abstract public ItemStack getMainHand(Player plr);
    abstract public ItemStack getSecondHand(Player plr);

    abstract public void setMainHand(Player plr, ItemStack item);
    abstract public void setSecondHand(Player plr, ItemStack item);

    abstract public boolean isHoldingBook(Player player);

    abstract public ItemStack[] stringListToItemStack(List<String> stringlist);
    abstract public List<String> itemStackToStringList(ItemStack[] inventory);
}
