package net.livecar.nuttyworks.thejail.bridges;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MCUtils_1_13_R1 extends MCVersionBridge {

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
            case WRITABLE_BOOK:
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
            ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
            BookMeta meta = (BookMeta) item.getItemMeta();
            List<String> content = new ArrayList<>();
            content.add(command);
            meta.setPages(content);
            item.setItemMeta(meta);
            ItemMeta itmMeta = item.getItemMeta();
            if (command.length() > 25)
                itmMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', command.substring(1,25)));
            else
                itmMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', command));
            item.setItemMeta(itmMeta);
            newInv[nCnt] = item;
            nCnt++;
            if (nCnt > 54)
                break;
        }
        return newInv;
    }

    @Override
    public List<String> itemStackToStringList(ItemStack[] inventory) {
        HashMap<String,String> commands = new HashMap<>();

        for (ItemStack item : inventory)
        {
            if (item == null)
                continue;
            if (item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK) {
                BookMeta meta = (BookMeta) item.getItemMeta();
                String commandString = "";
                for (int pageNum = 1; pageNum <= meta.getPageCount(); pageNum++)
                    commandString += meta.getPage(1).trim();
                if (!commands.containsKey(commandString))
                    commands.put(commandString,commandString);
            }
        }
        return new ArrayList<>(commands.values());
    }

}
