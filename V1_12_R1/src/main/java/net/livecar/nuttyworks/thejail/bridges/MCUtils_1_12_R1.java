package net.livecar.nuttyworks.thejail.bridges;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class MCUtils_1_12_R1 extends MCVersionBridge {

    @Override
    public ItemStack getMainHand(Player plr) {
        return plr.getInventory().getItemInMainHand();
    }

    @Override
    public ItemStack getSecondHand(Player plr) {
        return plr.getInventory().getItemInOffHand();
    }

    @Override
    public void setMainHand(Player plr, ItemStack item) { plr.getInventory().setItemInMainHand(item); }

    @Override
    public void setSecondHand(Player plr, ItemStack item) { plr.getInventory().setItemInOffHand(item); }

    @Override
    public boolean isHoldingBook(Player player) {
        switch (player.getInventory().getItemInMainHand().getType()) {
            case WRITTEN_BOOK:
            case BOOK_AND_QUILL:
            case BOOK:
                return true;
            default:
                return false;
        }
    }

    @Override
    public ItemStack[] stringListToItemStack(List<String> stringlist) {
        ItemStack[] newInv = new ItemStack[54];

        int nCnt=0;
        for (String command : stringlist)
        {
            ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) item.getItemMeta();
            meta.setPage(1,command);
            item.setItemMeta(meta);
            newInv[nCnt] = item;
        }
        return newInv;
    }

    @Override
    public List<String> itemStackToStringList(ItemStack[] inventory) {
        List<String> commands = new ArrayList<>();

        for (ItemStack item : inventory)
        {
            if (item == null)
                continue;
            if (item.getType() == Material.WRITTEN_BOOK) {
                BookMeta meta = (BookMeta) item.getItemMeta();
                String commandString = "";
                for (int pageNum = 1; pageNum <= meta.getPageCount(); pageNum++)
                    commandString += meta.getPage(1).trim();
                commands.add(commandString);
            }
        }
        return commands;
    }
}
