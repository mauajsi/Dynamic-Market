package com.example.economia.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import com.example.economia.MarketManager;
import org.bukkit.Material;

public class SellGUI {
    private final MarketManager marketManager;
    private static final String GUI_TITLE = "Vendi Items";
    private static final int GUI_SIZE = 54; // 6 rows

    public SellGUI(MarketManager marketManager) {
        this.marketManager = marketManager;
    }

    public void openSellGUI(Player player) {
        Inventory sellInventory = Bukkit.createInventory(player, GUI_SIZE, GUI_TITLE);
        player.openInventory(sellInventory);
    }

    public void processSale(Player player, Inventory inventory) {
        double totalValue = 0.0;
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                double price = marketManager.getPrice("sell", item.getType());
                if (price > 0) {
                    totalValue += price * item.getAmount();
                }
            }
        }

        if (totalValue > 0) {
            // Add money to player's balance
            marketManager.getEconomy().depositPlayer(player, totalValue);
            player.sendMessage(ChatColor.GREEN + "Hai venduto gli items per $" + totalValue);
        } else {
            player.sendMessage(ChatColor.RED + "Nessun item valido da vendere!");
        }

        inventory.clear();
    }
}