package com.dynamicmarket.gui;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketCategory;
import com.dynamicmarket.data.MarketItem;
import com.dynamicmarket.managers.PurchaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CategoryGUI implements org.bukkit.inventory.InventoryHolder {
    
    private final DynamicMarket plugin;
    private final Player player;
    private final MarketCategory category;
    private Inventory inventory;
    private int currentPage;
    private final int itemsPerPage = 28; // 4 rows of 7 items

    
    public CategoryGUI(DynamicMarket plugin, Player player, MarketCategory category) {
        this.plugin = plugin;
        this.player = player;
        this.category = category;
        this.currentPage = 0;
        createInventory();
    }
    
    private void createInventory() {
        String title = plugin.getConfigManager().getCategoryTitle()
            .replace("{category}", category.getDisplayName());
        inventory = Bukkit.createInventory(this, 54, title);
        
        populateInventory();
    }
    
    private void populateInventory() {
        // Clear inventory
        inventory.clear();
        
        // Add items from current page
        List<MarketItem> items = category.getAllItems();
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());
        
        int slot = 10; // Start from slot 10 (second row, second column)
        for (int i = startIndex; i < endIndex; i++) {
            MarketItem item = items.get(i);
            ItemStack displayItem = createItemDisplay(item);
            inventory.setItem(slot, displayItem);
            
            slot++;
            // Skip border slots
            if (slot % 9 == 8) {
                slot += 2;
            }
            if (slot >= 44) {
                break;
            }
        }
        
        // Add decorative items
        addDecorativeItems();
        
        // Add navigation items
        addNavigationItems();
    }
    
    private ItemStack createItemDisplay(MarketItem marketItem) {
        ItemStack item = marketItem.createItemStack(1);
        ItemMeta meta = item.getItemMeta();
        
        // Imposta l'item come non spostabile
        org.bukkit.inventory.ItemFlag[] flags = {org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES};
        meta.addItemFlags(flags);
        
        if (meta != null) {
            // Set display name if not already set
            if (meta.getDisplayName() == null || meta.getDisplayName().isEmpty()) {
                String itemName = marketItem.getMaterial().name().toLowerCase().replace("_", " ");
                itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
                meta.setDisplayName("§f" + itemName);
            }
            
            List<String> lore = new ArrayList<>();
            
            // Add original lore if exists
            if (marketItem.getLore() != null && !marketItem.getLore().isEmpty()) {
                lore.addAll(marketItem.getLore());
                lore.add("");
            }
            
            // Add market information
            lore.add("§7▪ §eBuy Price: §a$" + String.format("%.2f", marketItem.getBuyPrice()));
            lore.add("§7▪ §eSell Price: §c$" + String.format("%.2f", marketItem.getSellPrice()));
            lore.add("§7▪ §eStock: §f" + marketItem.getStock());
            
            // Add trading statistics
            if (marketItem.getTotalBought() > 0 || marketItem.getTotalSold() > 0) {
                lore.add("");
                lore.add("§7§l▪ Trading Statistics:");
                lore.add("§7  Bought: §e" + marketItem.getTotalBought());
                lore.add("§7  Sold: §e" + marketItem.getTotalSold());
            }
            
            // Add stock status
            lore.add("");
            if (marketItem.getStock() <= 0) {
                lore.add("§c§l⚠ OUT OF STOCK");
            } else if (marketItem.getStock() <= 10) {
                lore.add("§6§l⚠ LOW STOCK");
            } else {
                lore.add("§a§l✓ IN STOCK");
            }
            
            // Add purchase/sell instructions
            lore.add("");
            if (marketItem.getStock() > 0) {
                lore.add("§a§l▶ Left click to buy");
                lore.add("§c§l▶ Right click to sell");
            } else {
                lore.add("§c§l✗ Cannot purchase - No stock");
            }
            
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
        
        // Fill border slots
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52};
        
        for (int slot : borderSlots) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, glassPane);
            }
        }


    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private void addNavigationItems() {
        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§c§l← Back to Market");
            List<String> backLore = new ArrayList<>();
            backLore.add("§7Return to the main market");
            backMeta.setLore(backLore);
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(45, backItem);
        
        // Category info
        ItemStack categoryInfo = category.createCategoryIcon();
        inventory.setItem(4, categoryInfo);
        
        // Page navigation
        List<MarketItem> items = category.getAllItems();
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        
        if (totalPages > 1) {
            // Previous page
            if (currentPage > 0) {
                ItemStack prevItem = new ItemStack(Material.SPECTRAL_ARROW);
                ItemMeta prevMeta = prevItem.getItemMeta();
                if (prevMeta != null) {
                    prevMeta.setDisplayName("§e§l← Previous Page");
                    List<String> prevLore = new ArrayList<>();
                    prevLore.add("§7Page " + currentPage + " of " + totalPages);
                    prevMeta.setLore(prevLore);
                    prevItem.setItemMeta(prevMeta);
                }
                inventory.setItem(48, prevItem);
            }
            
            // Next page
            if (currentPage < totalPages - 1) {
                ItemStack nextItem = new ItemStack(Material.SPECTRAL_ARROW);
                ItemMeta nextMeta = nextItem.getItemMeta();
                if (nextMeta != null) {
                    nextMeta.setDisplayName("§e§lNext Page →");
                    List<String> nextLore = new ArrayList<>();
                    nextLore.add("§7Page " + (currentPage + 2) + " of " + totalPages);
                    nextMeta.setLore(nextLore);
                    nextItem.setItemMeta(nextMeta);
                }
                inventory.setItem(50, nextItem);
            }
            
            // Page indicator
            ItemStack pageIndicator = new ItemStack(Material.PAPER);
            ItemMeta pageMeta = pageIndicator.getItemMeta();
            if (pageMeta != null) {
                pageMeta.setDisplayName("§6§lPage " + (currentPage + 1) + " of " + totalPages);
                List<String> pageLore = new ArrayList<>();
                pageLore.add("§7Showing items " + (currentPage * itemsPerPage + 1) + 
                           " to " + Math.min((currentPage + 1) * itemsPerPage, items.size()));
                pageLore.add("§7Total items: " + items.size());
                pageMeta.setLore(pageLore);
                pageIndicator.setItemMeta(pageMeta);
            }
            inventory.setItem(49, pageIndicator);
        }
        
        // Refresh button
        ItemStack refreshItem = new ItemStack(Material.EMERALD);
        ItemMeta refreshMeta = refreshItem.getItemMeta();
        if (refreshMeta != null) {
            refreshMeta.setDisplayName("§a§lRefresh Prices");
            List<String> refreshLore = new ArrayList<>();
            refreshLore.add("§7Click to refresh item prices");
            refreshMeta.setLore(refreshLore);
            refreshItem.setItemMeta(refreshMeta);
        }
        inventory.setItem(53, refreshItem);
    }
    
    public void nextPage() {
        List<MarketItem> items = category.getAllItems();
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        
        if (currentPage < totalPages - 1) {
            currentPage++;
            populateInventory();
        }
    }
    
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            populateInventory();
        }
    }
    
    public void refresh() {
        populateInventory();
    }
    
    public void handleClick(int slot, boolean isRightClick, boolean isShiftClick) {
        // Previeni il movimento degli item
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        
        // Handle navigation items
        if (slot == 45) { // Back button
            new MarketGUI(plugin, player).open();
            return;
        }
        
        if (slot == 48 && currentPage > 0) { // Previous page
            previousPage();
            return;
        }
        
        // Check if clicked slot contains a market item
        ItemStack clickedItem = inventory.getItem(slot);
        if (clickedItem == null || clickedItem.getType() == Material.AIR || 
            clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }
        
        // Check if the slot is in the item display area (slots 10-16, 19-25, 28-34, 37-43)
        if (!isItemSlot(slot)) {
            return;
        }
        
        // Calculate the index in the items list based on the slot
        int index = slotToIndex(slot) + (currentPage * itemsPerPage);
        
        List<MarketItem> items = category.getAllItems();
        if (index >= items.size()) {
            return;
        }
        
        MarketItem marketItem = items.get(index);
        
        // Open buy or sell GUI based on click type
        if (isRightClick) {
            // Open sell GUI if player has the item
            ItemStack playerItem = null;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == marketItem.getMaterial()) {
                    playerItem = item.clone();
                    break;
                }
            }
            
            if (playerItem != null) {
                new SellItemGUI(plugin, player, marketItem, playerItem).open();
            } else {
                player.sendMessage("§cYou don't have any " + marketItem.getDisplayName() + " to sell!");
            }
        } else {
            // Open buy GUI
            new BuyGUI(plugin, player, marketItem).open();
        }
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public MarketCategory getCategory() {
        return category;
    }
    
    private boolean isItemSlot(int slot) {
        // Check if the slot is in one of the valid rows (slots 10-16, 19-25, 28-34, 37-43)
        return (slot >= 10 && slot <= 16) ||
               (slot >= 19 && slot <= 25) ||
               (slot >= 28 && slot <= 34) ||
               (slot >= 37 && slot <= 43);
    }
    
    private int slotToIndex(int slot) {
        // Convert GUI slot to index in items list
        int row = slot / 9 - 1; // -1 because first row is at index 0
        int col = slot % 9 - 1; // -1 because first column is at index 0
        return row * 7 + col;
    }


}