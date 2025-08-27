package com.dynamicmarket.gui;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketItem;
import com.dynamicmarket.managers.SellHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SellItemGUI implements org.bukkit.inventory.InventoryHolder {
    
    private final DynamicMarket plugin;
    private final Player player;
    private final MarketItem marketItem;
    private final ItemStack playerItem;
    private Inventory inventory;
    private int selectedQuantity = 1;
    private final int availableQuantity;
    
    public SellItemGUI(DynamicMarket plugin, Player player, MarketItem marketItem, ItemStack playerItem) {
        this.plugin = plugin;
        this.player = player;
        this.marketItem = marketItem;
        this.playerItem = playerItem.clone();
        this.availableQuantity = calculateAvailableQuantity();
        createInventory();
    }
    
    private int calculateAvailableQuantity() {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(playerItem)) {
                total += item.getAmount();
            }
        }
        return total;
    }
    
    private void createInventory() {
        inventory = Bukkit.createInventory(this, 54, "Sell " + marketItem.getDisplayName());
        populateInventory();
    }
    
    private void populateInventory() {
        // Clear inventory
        inventory.clear();
        
        // Add item display in center
        ItemStack displayItem = createItemDisplay();
        inventory.setItem(22, displayItem);
        
        // Add quantity controls
        updateQuantityControls();
        
        // Add decorative border
        addDecorativeItems();
    }
    
    private ItemStack createItemDisplay() {
        ItemStack item = playerItem.clone();
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            
            // Add original lore if exists
            if (meta.hasLore()) {
                lore.addAll(meta.getLore());
                lore.add("");
            }
            
            // Add market information
            lore.add("§7▪ §eSell Price: §a$" + String.format("%.2f", marketItem.getSellPrice()));
            lore.add("§7▪ §eTotal Value: §a$" + String.format("%.2f", marketItem.getSellPrice() * selectedQuantity));
            lore.add("§7▪ §eAvailable: §f" + availableQuantity);
            lore.add("");
            lore.add("§7Selected Quantity: §e" + selectedQuantity);
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private void updateQuantityControls() {
        // Decrease quantity button
        ItemStack decreaseItem = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta decreaseMeta = decreaseItem.getItemMeta();
        if (decreaseMeta != null) {
            decreaseMeta.setDisplayName("§d§l← Decrease Quantity (" + selectedQuantity + ")");
            List<String> decreaseLore = new ArrayList<>();
            decreaseLore.add("§7Click to decrease quantity by 1");
            decreaseMeta.setLore(decreaseLore);
            decreaseItem.setItemMeta(decreaseMeta);
        }
        inventory.setItem(30, decreaseItem);
        
        // Increase quantity button
        ItemStack increaseItem = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta increaseMeta = increaseItem.getItemMeta();
        if (increaseMeta != null) {
            increaseMeta.setDisplayName("§b§lIncrease Quantity (" + selectedQuantity + ") →");
            List<String> increaseLore = new ArrayList<>();
            increaseLore.add("§7Click to increase quantity by 1");
            increaseMeta.setLore(increaseLore);
            increaseItem.setItemMeta(increaseMeta);
        }
        inventory.setItem(32, increaseItem);
        
        // Confirm sell button
        ItemStack confirmItem = new ItemStack(Material.PAPER);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§f§lConfirm Sale: " + selectedQuantity + "x");
            List<String> confirmLore = new ArrayList<>();
            double totalValue = marketItem.getSellPrice() * selectedQuantity;
            confirmLore.add("§7Total value: §a$" + String.format("%.2f", totalValue));
            confirmLore.add("§7Item: §f" + marketItem.getMaterial().name());
            confirmLore.add("§7Price each: §a$" + String.format("%.2f", marketItem.getSellPrice()));
            confirmMeta.setLore(confirmLore);
            confirmItem.setItemMeta(confirmMeta);
        }
        inventory.setItem(40, confirmItem);
        
        // Stack selector
        ItemStack stackItem = new ItemStack(Material.CHEST);
        ItemMeta stackMeta = stackItem.getItemMeta();
        if (stackMeta != null) {
            stackMeta.setDisplayName("§6§lSelect Stacks (1-9)");
            List<String> stackLore = new ArrayList<>();
            stackLore.add("§7Left click: 1 stack");
            stackLore.add("§7Shift + Left click: 5 stacks");
            stackLore.add("§7Right click: All items");
            stackLore.add("§7Available: " + availableQuantity + " items");
            stackMeta.setLore(stackLore);
            stackItem.setItemMeta(stackMeta);
        }
        inventory.setItem(41, stackItem);
        
        // Update item display
        inventory.setItem(22, createItemDisplay());
    }
    
    private void addDecorativeItems() {
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        
        // Fill border slots
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null && 
                (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)) {
                inventory.setItem(i, glassPane);
            }
        }
        
        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§c§l← Back");
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(45, backItem);
    }
    
    public void handleClick(int slot, boolean isRightClick, boolean isShiftClick) {
        if (slot == 30) { // Decrease quantity
            selectedQuantity = Math.max(1, selectedQuantity - 1);
            updateQuantityControls();
        } 
        else if (slot == 32) { // Increase quantity
            selectedQuantity = Math.min(availableQuantity, selectedQuantity + 1);
            updateQuantityControls();
        }
        else if (slot == 40) { // Confirm sale
            if (selectedQuantity > 0 && selectedQuantity <= availableQuantity) {
                SellHandler sellHandler = new SellHandler(plugin);
                sellHandler.processSale(player, marketItem, selectedQuantity);
                player.closeInventory();
            }
        }
        else if (slot == 41) { // Stack selector
            if (isRightClick) {
                selectedQuantity = availableQuantity; // All items
            } else {
                int numStacks = isShiftClick ? 5 : 1;
                selectedQuantity = Math.min(availableQuantity, 64 * numStacks);
            }
            updateQuantityControls();
        }
        else if (slot == 45) { // Back button
            player.closeInventory();
        }
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}