package com.dynamicmarket.gui;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketCategory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.dynamicmarket.gui.CategoryGUI;
import com.dynamicmarket.gui.SellGUI;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MarketGUI implements org.bukkit.inventory.InventoryHolder {
    
    private final DynamicMarket plugin;
    private final Player player;
    private Inventory inventory;
    
    public MarketGUI(DynamicMarket plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        createInventory();
    }
    
    private void createInventory() {
        String title = plugin.getConfigManager().getMarketTitle();
        inventory = Bukkit.createInventory(this, 54, title);
        
        populateInventory();
    }
    
    private void populateInventory() {
        // Clear inventory
        inventory.clear();
        
        // Add category items
        List<MarketCategory> categories = plugin.getMarketManager().getSortedCategories();
        
        for (MarketCategory category : categories) {
            int slot = category.getGuiSlot();
            if (slot >= 0 && slot < 54) {
                ItemStack categoryItem = createCategoryItem(category);
                inventory.setItem(slot, categoryItem);
            }
        }
        
        // Add decorative items
        addDecorativeItems();
        
        // Add navigation items
        addNavigationItems();
    }
    
    private ItemStack createCategoryItem(MarketCategory category) {
        ItemStack item = new ItemStack(category.getIcon());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(category.getDisplayName());
            
            List<String> lore = new ArrayList<>();
            
            // Add category description
            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                lore.addAll(category.getDescription());
                lore.add("");
            }
            
            // Add category statistics
            lore.add("§7Items available: §e" + category.getItemCount());
            
            if (!category.isEmpty()) {
                // Calculate average prices
                double avgBuyPrice = category.getAllItems().stream()
                    .mapToDouble(marketItem -> marketItem.getBuyPrice())
                    .average().orElse(0.0);
                
                double avgSellPrice = category.getAllItems().stream()
                    .mapToDouble(marketItem -> marketItem.getSellPrice())
                    .average().orElse(0.0);
                
                lore.add("§7Avg. buy price: §a$" + String.format("%.2f", avgBuyPrice));
                lore.add("§7Avg. sell price: §c$" + String.format("%.2f", avgSellPrice));
            }
            
            lore.add("");
            lore.add("§e§lClick to browse!");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private void addDecorativeItems() {
        // Add glass panes for decoration
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        
        // Fill empty slots in the border
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        
        for (int slot : borderSlots) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, glassPane);
            }
        }
    }
    
    private void addNavigationItems() {
        // Add refresh button
        ItemStack refreshItem = new ItemStack(Material.EMERALD);
        ItemMeta refreshMeta = refreshItem.getItemMeta();
        if (refreshMeta != null) {
            refreshMeta.setDisplayName("§a§lRefresh Market");
            List<String> refreshLore = new ArrayList<>();
            refreshLore.add("§7Click to refresh the market");
            refreshLore.add("§7and update all prices!");
            refreshMeta.setLore(refreshLore);
            refreshItem.setItemMeta(refreshMeta);
        }
        inventory.setItem(49, refreshItem);
        
        // Add player info item
        ItemStack playerInfoItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playerInfoMeta = playerInfoItem.getItemMeta();
        if (playerInfoMeta != null) {
            playerInfoMeta.setDisplayName("§6§l" + player.getName());
            List<String> playerInfoLore = new ArrayList<>();
            
            double balance = plugin.getVaultEconomy().getBalance(player);
            playerInfoLore.add("§7Balance: §a$" + String.format("%.2f", balance));
            playerInfoLore.add("");
            playerInfoLore.add("§7Welcome to the Dynamic Market!");
            playerInfoLore.add("§7Prices change based on supply");
            playerInfoLore.add("§7and demand. Trade wisely!");
            
            playerInfoMeta.setLore(playerInfoLore);
            playerInfoItem.setItemMeta(playerInfoMeta);
        }
        inventory.setItem(4, playerInfoItem);
        
        // Add sell items button
        ItemStack sellItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta sellMeta = sellItem.getItemMeta();
        if (sellMeta != null) {
            sellMeta.setDisplayName("§6§lSell Items");
            List<String> sellLore = new ArrayList<>();
            sellLore.add("§7Click to open the sell menu");
            sellLore.add("§7and sell your items to the market!");
            sellMeta.setLore(sellLore);
            sellItem.setItemMeta(sellMeta);
        }
        inventory.setItem(45, sellItem);
        
        // Add market statistics item
        ItemStack statsItem = new ItemStack(Material.BOOK);
        ItemMeta statsMeta = statsItem.getItemMeta();
        if (statsMeta != null) {
            statsMeta.setDisplayName("§b§lMarket Statistics");
            List<String> statsLore = new ArrayList<>();
            
            int totalCategories = plugin.getMarketManager().getAllCategories().size();
            int totalItems = plugin.getMarketManager().getAllCategories().stream()
                .mapToInt(MarketCategory::getItemCount)
                .sum();
            
            statsLore.add("§7Total Categories: §e" + totalCategories);
            statsLore.add("§7Total Items: §e" + totalItems);
            statsLore.add("");
            statsLore.add("§7Market is constantly updating");
            statsLore.add("§7based on player activity!");
            
            statsMeta.setLore(statsLore);
            statsItem.setItemMeta(statsMeta);
        }
        inventory.setItem(53, statsItem);
    }
    
    public void refresh() {
        populateInventory();
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Handles click events in the market GUI
     */
    public void handleClick(int slot) {
        ItemStack clickedItem = inventory.getItem(slot);
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // Handle category clicks
        for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            if (category.getGuiSlot() == slot) {
                // Open category GUI
                CategoryGUI categoryGUI = new CategoryGUI(plugin, player, category);
                categoryGUI.open();
                return;
            }
        }
        
        // Handle special item clicks
        switch (slot) {
            case 49: // Refresh button
                refresh();
                player.sendMessage("§aMarket refreshed!");
                break;
                
            case 45: // Sell items button
                SellGUI sellGUI = new SellGUI(plugin, player);
                sellGUI.open();
                break;
                
            case 4: // Player info (no action)
            case 53: // Statistics (no action)
                break;
                
            default:
                // Decorative items (no action)
                break;
        }
    }
}