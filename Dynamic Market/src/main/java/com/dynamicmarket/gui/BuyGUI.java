package com.dynamicmarket.gui;

import com.dynamicmarket.DynamicMarket;
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

public class BuyGUI implements org.bukkit.inventory.InventoryHolder {
    
    private final DynamicMarket plugin;
    private final Player player;
    private final MarketItem marketItem;
    private Inventory inventory;
    private int selectedQuantity = 1;
    
    public BuyGUI(DynamicMarket plugin, Player player, MarketItem marketItem) {
        this.plugin = plugin;
        this.player = player;
        this.marketItem = marketItem;
        createInventory();
    }
    
    private void createInventory() {
        inventory = Bukkit.createInventory(this, 54, "Buy " + marketItem.getDisplayName());
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
        ItemStack item = marketItem.createItemStack(1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            
            // Add original lore if exists
            if (marketItem.getLore() != null && !marketItem.getLore().isEmpty()) {
                lore.addAll(marketItem.getLore());
                lore.add("");
            }
            
            // Add market information
            lore.add("§7▪ §eBuy Price: §a$" + String.format("%.2f", marketItem.getBuyPrice()));
            lore.add("§7▪ §eTotal Cost: §a$" + String.format("%.2f", marketItem.getBuyPrice() * selectedQuantity));
            lore.add("§7▪ §eStock: §f" + marketItem.getStock());
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
        
        // Confirm purchase button
        ItemStack confirmItem = new ItemStack(Material.PAPER);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§f§lConfirm Purchase: " + selectedQuantity + "x");
            List<String> confirmLore = new ArrayList<>();
            double totalCost = marketItem.getBuyPrice() * selectedQuantity;
            confirmLore.add("§7Total cost: §a$" + String.format("%.2f", totalCost));
            confirmLore.add("§7Item: §f" + marketItem.getMaterial().name());
            confirmLore.add("§7Price each: §a$" + String.format("%.2f", marketItem.getBuyPrice()));
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
            stackLore.add("§7Right click: Max amount");
            stackLore.add("§7Available: " + marketItem.getStock() + " items");
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
            selectedQuantity = Math.min(marketItem.getStock(), selectedQuantity + 1);
            updateQuantityControls();
        }
        else if (slot == 40) { // Confirm purchase
            if (selectedQuantity > 0 && selectedQuantity <= marketItem.getStock()) {
                PurchaseHandler purchaseHandler = new PurchaseHandler(plugin);
                purchaseHandler.processPurchase(player, marketItem, selectedQuantity);
                player.closeInventory();
            }
        }
        else if (slot == 41) { // Stack selector
            if (isRightClick) {
                selectedQuantity = marketItem.getStock(); // Max amount
            } else {
                int numStacks = isShiftClick ? 5 : 1;
                selectedQuantity = Math.min(marketItem.getStock(), 64 * numStacks);
            }
            updateQuantityControls();
        }
        else if (slot == 45) { // Back button
            // Return to category GUI
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