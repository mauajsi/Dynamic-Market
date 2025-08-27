package com.dynamicmarket.managers;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PurchaseHandler {
    
    private final DynamicMarket plugin;
    
    public PurchaseHandler(DynamicMarket plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Processes a purchase transaction
     * @param player The player making the purchase
     * @param marketItem The item being purchased
     * @param amount The amount to purchase
     * @return true if the purchase was successful, false otherwise
     */
    public boolean processPurchase(Player player, MarketItem marketItem, int amount) {
        // Validate purchase
        if (!validatePurchase(player, marketItem, amount)) {
            return false;
        }
        
        double totalCost = marketItem.getBuyPrice() * amount;
        
        // Check if player has enough money
        if (!plugin.getVaultEconomy().hasBalance(player, totalCost)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds",
                "cost", String.format("%.2f", totalCost)));
            return false;
        }

        // Check if player has enough inventory space
        if (!hasInventorySpace(player, marketItem.createItemStack(1), amount)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-space"));
            return false;
        }

        // Process the transaction
        plugin.getVaultEconomy().withdrawPlayer(player, totalCost);
        
        // Give items to player
        giveItemsToPlayer(player, marketItem.createItemStack(1), amount);
        
        // Update market item (decrease stock, update prices, record transaction)
        marketItem.decreaseStock(amount);
marketItem.updatePriceOnBuy(amount);
        
        // Log transaction
        plugin.getDataManager().getDatabaseManager().logTransaction(
            player.getUniqueId().toString(),
            player.getName(),
            marketItem.getId(),
            marketItem.getCategory(),
            "BUY",
            amount,
            marketItem.getBuyPrice()
        );
        
        // Save market data
        plugin.getMarketManager().saveMarketData();
        
        // Send success message
        String itemName = marketItem.getDisplayName() != null ? 
            marketItem.getDisplayName() : 
            marketItem.getMaterial().name().toLowerCase().replace("_", " ");
            
        player.sendMessage(plugin.getConfigManager().getMessage("market.purchase.success",
            "amount", String.valueOf(amount),
            "item", itemName,
            "cost", String.format("%.2f", totalCost)));
        
        return true;
    }
    
    /**
     * Validates if a purchase can be made
     */
    private boolean validatePurchase(Player player, MarketItem marketItem, int amount) {
        if (player == null) {
            return false;
        }
        
        if (marketItem == null) {
            player.sendMessage("§cItem not found in market!");
            return false;
        }
        
        if (amount <= 0) {
            player.sendMessage("§cInvalid amount!");
            return false;
        }
        
        if (!marketItem.canBuy(amount)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-stock",
                "available", String.valueOf(marketItem.getStock())));
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if player has enough inventory space for the items
     */
    private boolean hasInventorySpace(Player player, ItemStack item, int amount) {
        ItemStack[] contents = player.getInventory().getContents();
        int availableSpace = 0;
        
        for (ItemStack slot : contents) {
            if (slot == null) {
                availableSpace += item.getMaxStackSize();
            } else if (slot.isSimilar(item)) {
                availableSpace += item.getMaxStackSize() - slot.getAmount();
            }
            
            if (availableSpace >= amount) {
                return true;
            }
        }
        
        return availableSpace >= amount;
    }
    
    /**
     * Gives items to the player's inventory
     */
    private void giveItemsToPlayer(Player player, ItemStack item, int amount) {
        int remaining = amount;
        
        while (remaining > 0) {
            int stackSize = Math.min(remaining, item.getMaxStackSize());
            ItemStack giveItem = item.clone();
            giveItem.setAmount(stackSize);
            
            player.getInventory().addItem(giveItem);
            remaining -= stackSize;
        }
    }
    
    /**
     * Processes a purchase with confirmation dialog
     */
    public void processPurchaseWithConfirmation(Player player, MarketItem marketItem, int amount) {
        double totalCost = marketItem.getBuyPrice() * amount;
        
        // For small purchases, skip confirmation
        if (totalCost <= plugin.getConfigManager().getConfig().getDouble("confirmation-threshold", 100.0)) {
            processPurchase(player, marketItem, amount);
            return;
        }
        
        // Create confirmation GUI for expensive purchases
        ConfirmationGUI confirmationGUI = new ConfirmationGUI(plugin, player, marketItem, amount, totalCost);
        confirmationGUI.open();
    }

    public static boolean handlePurchase(DynamicMarket plugin, Player player, MarketItem marketItem, int amount) {
        PurchaseHandler handler = new PurchaseHandler(plugin);
        return handler.processPurchase(player, marketItem, amount);
    }
    
    /**
     * Gets the maximum amount a player can afford
     */
    public int getMaxAffordableAmount(Player player, MarketItem marketItem) {
        double balance = plugin.getVaultEconomy().getBalance(player);
        double itemPrice = marketItem.getBuyPrice();
        
        if (itemPrice <= 0) {
            return 0;
        }
        
        int maxAffordable = (int) Math.floor(balance / itemPrice);
        return Math.min(maxAffordable, marketItem.getStock());
    }
    
    /**
     * Calculates the total cost for a purchase
     */
    public double calculateTotalCost(MarketItem marketItem, int amount) {
        return marketItem.getBuyPrice() * amount;
    }
    
    /**
     * Checks if a player can afford a purchase
     */
    public boolean canAfford(Player player, MarketItem marketItem, int amount) {
        double totalCost = calculateTotalCost(marketItem, amount);
        return plugin.getVaultEconomy().hasBalance(player, totalCost);
    }
}