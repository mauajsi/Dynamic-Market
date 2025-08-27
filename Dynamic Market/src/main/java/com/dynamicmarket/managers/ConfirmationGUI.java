package com.dynamicmarket.managers;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationGUI {
    
    private final DynamicMarket plugin;
    private final Player player;
    private final MarketItem marketItem;
    private final int amount;
    private final double totalCost;
    private Inventory inventory;
    
    public ConfirmationGUI(DynamicMarket plugin, Player player, MarketItem marketItem, int amount, double totalCost) {
        this.plugin = plugin;
        this.player = player;
        this.marketItem = marketItem;
        this.amount = amount;
        this.totalCost = totalCost;
        createInventory();
    }
    
    private void createInventory() {
        String title = "§c§lConfirm Purchase";
        inventory = Bukkit.createInventory(null, 27, title);
        
        populateInventory();
    }
    
    private void populateInventory() {
        // Fill with glass panes
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, glassPane);
        }
        
        // Item being purchased
        ItemStack purchaseItem = marketItem.createItemStack(amount);
        ItemMeta purchaseMeta = purchaseItem.getItemMeta();
        if (purchaseMeta != null) {
            List<String> lore = new ArrayList<>();
            lore.add("§7You are about to purchase:");
            lore.add("");
            lore.add("§7▪ §eAmount: §f" + amount);
            lore.add("§7▪ §ePrice per item: §a$" + String.format("%.2f", marketItem.getBuyPrice()));
            lore.add("§7▪ §eTotal cost: §c$" + String.format("%.2f", totalCost));
            lore.add("");
            double balance = plugin.getVaultEconomy().getBalance(player);
            lore.add("§7▪ §eYour balance: §a$" + String.format("%.2f", balance));
            lore.add("§7▪ §eAfter purchase: §a$" + String.format("%.2f", plugin.getVaultEconomy().getBalance(player) - totalCost));
            lore.add("");
            lore.add("§c§lThis is an expensive purchase!");
            lore.add("§7Please confirm your decision below.");
            
            purchaseMeta.setLore(lore);
            purchaseItem.setItemMeta(purchaseMeta);
        }
        inventory.setItem(13, purchaseItem);
        
        // Confirm button (Green)
        ItemStack confirmItem = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§a§l✓ CONFIRM PURCHASE");
            List<String> confirmLore = new ArrayList<>();
            confirmLore.add("§7Click to confirm and complete");
            confirmLore.add("§7the purchase transaction.");
            confirmLore.add("");
            confirmLore.add("§a§lYes, I want to buy this!");
            confirmMeta.setLore(confirmLore);
            confirmItem.setItemMeta(confirmMeta);
        }
        inventory.setItem(10, confirmItem);
        
        // Cancel button (Red)
        ItemStack cancelItem = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName("§c§l✗ CANCEL PURCHASE");
            List<String> cancelLore = new ArrayList<>();
            cancelLore.add("§7Click to cancel and return");
            cancelLore.add("§7to the market without buying.");
            cancelLore.add("");
            cancelLore.add("§c§lNo, take me back!");
            cancelMeta.setLore(cancelLore);
            cancelItem.setItemMeta(cancelMeta);
        }
        inventory.setItem(16, cancelItem);
        
        // Warning item
        ItemStack warningItem = new ItemStack(Material.YELLOW_CONCRETE);
        ItemMeta warningMeta = warningItem.getItemMeta();
        if (warningMeta != null) {
            warningMeta.setDisplayName("§e§l⚠ EXPENSIVE PURCHASE WARNING");
            List<String> warningLore = new ArrayList<>();
            warningLore.add("§7This purchase costs more than");
            warningLore.add("§7$" + String.format("%.2f", plugin.getConfigManager().getConfig().getDouble("confirmation-threshold", 100.0)) + " and requires confirmation.");
            warningLore.add("");
            warningLore.add("§7Make sure you really want to");
            warningLore.add("§7spend this much money!");
            warningLore.add("");
            warningLore.add("§e§lDouble-check before confirming!");
            warningMeta.setLore(warningLore);
            warningItem.setItemMeta(warningMeta);
        }
        inventory.setItem(4, warningItem);
        
        // Balance info
        ItemStack balanceItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta balanceMeta = balanceItem.getItemMeta();
        if (balanceMeta != null) {
            balanceMeta.setDisplayName("§6§lYour Balance");
            List<String> balanceLore = new ArrayList<>();
            
            double currentBalance = plugin.getVaultEconomy().getBalance(player);
            double afterBalance = currentBalance - totalCost;
            
            balanceLore.add("§7Current balance: §a$" + String.format("%.2f", currentBalance));
            balanceLore.add("§7Purchase cost: §c-$" + String.format("%.2f", totalCost));
            balanceLore.add("§7After purchase: §" + (afterBalance >= 0 ? "a" : "c") + "$" + String.format("%.2f", afterBalance));
            
            if (afterBalance < 0) {
                balanceLore.add("");
                balanceLore.add("§c§lINSUFFICIENT FUNDS!");
                balanceLore.add("§7You cannot afford this purchase.");
            } else if (afterBalance < currentBalance * 0.1) {
                balanceLore.add("");
                balanceLore.add("§e§lWARNING: Low balance after purchase!");
                balanceLore.add("§7This will use most of your money.");
            }
            
            balanceMeta.setLore(balanceLore);
            balanceItem.setItemMeta(balanceMeta);
        }
        inventory.setItem(22, balanceItem);
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
     * Handles click events in the confirmation GUI
     */
    public void handleClick(int slot, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        switch (slot) {
            case 10: // Confirm button
                confirmPurchase();
                break;
                
            case 16: // Cancel button
                cancelPurchase();
                break;
                
            default:
                // Ignore other clicks
                break;
        }
    }
    
    private void confirmPurchase() {
        player.closeInventory();
        
        // Double-check that player still has enough money
        if (!plugin.getVaultEconomy().hasBalance(player, totalCost)) {
            player.sendMessage("§cYou no longer have enough money for this purchase!");
            return;
        }
        
        // Process the purchase
        PurchaseHandler purchaseHandler = new PurchaseHandler(plugin);
        boolean success = purchaseHandler.processPurchase(player, marketItem, amount);
        
        if (success) {
            player.sendMessage("§a§lPurchase confirmed and completed!");
        } else {
            player.sendMessage("§c§lPurchase failed! Please try again.");
        }
    }
    
    private void cancelPurchase() {
        player.closeInventory();
        player.sendMessage("§e§lPurchase cancelled.");
        
        // Return to market (optional - could return to category instead)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.performCommand("market");
        }, 1L);
    }
}