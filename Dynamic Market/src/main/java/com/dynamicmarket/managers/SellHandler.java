package com.dynamicmarket.managers;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class SellHandler {
    
    private final DynamicMarket plugin;
    
    public SellHandler(DynamicMarket plugin) {
        this.plugin = plugin;
    }
    
    private boolean itemMatches(ItemStack item, MarketItem marketItem) {
        // Confronta il materiale dell'item
        if (item.getType() != marketItem.getMaterial()) {
            return false;
        }

        // Se l'oggetto di mercato non ha un nome specifico o lore, confronta solo il materiale
        if ((marketItem.getDisplayName() == null || marketItem.getDisplayName().isEmpty()) &&
            (marketItem.getLore() == null || marketItem.getLore().isEmpty())) {
            return true;
        }

        // Se l'oggetto del giocatore non ha metadata, non può corrispondere a un oggetto con nome o lore
        if (!item.hasItemMeta()) {
            return marketItem.getDisplayName() == null && marketItem.getLore() == null;
        }

        // Controlla il nome dell'oggetto se specificato
        if (marketItem.getDisplayName() != null && !marketItem.getDisplayName().isEmpty()) {
            if (!item.getItemMeta().hasDisplayName() || 
                !item.getItemMeta().getDisplayName().equals(marketItem.getDisplayName())) {
                return false;
            }
        }

        // Controlla il lore dell'oggetto se specificato
        if (marketItem.getLore() != null && !marketItem.getLore().isEmpty()) {
            if (!item.getItemMeta().hasLore() || 
                !item.getItemMeta().getLore().equals(marketItem.getLore())) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * Processes a sale transaction
     * @param player The player selling the item
     * @param marketItem The market item being sold
     * @param amount The amount to sell
     * @return true if the sale was successful, false otherwise
     */
    public boolean processSale(Player player, MarketItem marketItem, int amount) {
        double earned = processSaleQuiet(player, marketItem, amount);
        
        if (earned > 0) {
            String itemName = marketItem.getDisplayName() != null ? 
                marketItem.getDisplayName() : 
                marketItem.getMaterial().name().toLowerCase().replace("_", " ");
                
            player.sendMessage(plugin.getConfigManager().getMessage("sell-success",
                "amount", String.valueOf(amount),
                "item", itemName,
                "price", String.format("%.2f", earned)));
            return true;
        }
        
        return false;
    }
    
    /**
     * Processes a sale transaction without sending success message
     * @param player The player selling the item
     * @param marketItem The market item being sold
     * @param amount The amount to sell
     * @return The amount earned from the sale, 0 if failed
     */
    public double processSaleQuiet(Player player, MarketItem marketItem, int amount) {
        // Validate sale
        if (!validateSale(player, marketItem, amount)) {
            return 0.0;
        }
        
        // Check if player has enough items
        int playerAmount = getPlayerItemAmount(player, marketItem);
        if (playerAmount < amount) {
            player.sendMessage(plugin.getConfigManager().getMessage("market.insufficient-items",
                "required", String.valueOf(amount),
                "available", String.valueOf(playerAmount)));
            return 0.0;
        }
        
        double totalEarned = marketItem.getSellPrice() * amount;
        
        // Remove items from player inventory
        if (!removeItemsFromPlayer(player, marketItem, amount)) {
            player.sendMessage("§cFailed to remove items from inventory!");
            return 0.0;
        }
        
        // Give money to player
        plugin.getVaultEconomy().depositPlayer(player, totalEarned);
        
        // Update market item (increase stock, update prices, record transaction)
        marketItem.increaseStock(amount);
        marketItem.updatePriceOnSell(amount);
        
        // Log transaction
        plugin.getDataManager().getDatabaseManager().logTransaction(
            player.getUniqueId().toString(),
            player.getName(),
            marketItem.getId(),
            marketItem.getCategory(),
            "SELL",
            amount,
            marketItem.getSellPrice()
        );
        
        // Save market data
        plugin.getMarketManager().saveMarketData();
        
        return totalEarned;
    }
    
    /**
     * Validates if a sale can be made
     */
    private boolean validateSale(Player player, MarketItem marketItem, int amount) {
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
        
        return true;
    }
    
    /**
     * Gets the amount of a specific item the player has
     */
    private int getPlayerItemAmount(Player player, MarketItem marketItem) {
        // Cerca prima nella GUI di vendita se è aperta
        if (player.getOpenInventory() != null && 
            player.getOpenInventory().getTopInventory().getHolder() instanceof com.dynamicmarket.gui.SellGUI) {
            
            ItemStack[] contents = player.getOpenInventory().getTopInventory().getContents();
            int totalAmount = 0;
            
            plugin.getLogger().info("Cercando " + marketItem.getMaterial() + " nella GUI di vendita");
            
            for (ItemStack item : contents) {
                if (item != null && item.getType() == marketItem.getMaterial()) {
                    plugin.getLogger().info("Trovato item del tipo corretto: " + item.getType());
                    // Per oggetti base come le assi di quercia, confronta solo il materiale
                    if (isBasicItem(marketItem.getMaterial())) {
                        totalAmount += item.getAmount();
                        plugin.getLogger().info("Item base trovato, aggiunto " + item.getAmount() + " al totale");
                    }
                    // Altrimenti controlla anche nome e lore
                    else if (itemMatches(item, marketItem)) {
                        totalAmount += item.getAmount();
                        plugin.getLogger().info("Item complesso trovato, aggiunto " + item.getAmount() + " al totale");
                    } else {
                        plugin.getLogger().info("Item non corrisponde: nome o lore non corrispondono");
                    }
                }
            }
            
            return totalAmount;
        }
        
        // Se non è nella GUI di vendita, cerca nell'inventario del giocatore
        ItemStack[] contents = player.getInventory().getContents();
        int totalAmount = 0;
        
        plugin.getLogger().info("Cercando " + marketItem.getMaterial() + " nell'inventario del giocatore");
        
        for (ItemStack item : contents) {
            if (item != null && item.getType() == marketItem.getMaterial()) {
                plugin.getLogger().info("Trovato item del tipo corretto: " + item.getType());
                // Per oggetti base come le assi di quercia, confronta solo il materiale
                if (isBasicItem(marketItem.getMaterial())) {
                    totalAmount += item.getAmount();
                    plugin.getLogger().info("Item base trovato, aggiunto " + item.getAmount() + " al totale");
                }
                // Altrimenti controlla anche nome e lore
                else if (itemMatches(item, marketItem)) {
                    totalAmount += item.getAmount();
                    plugin.getLogger().info("Item complesso trovato, aggiunto " + item.getAmount() + " al totale");
                } else {
                    plugin.getLogger().info("Item non corrisponde: nome o lore non corrispondono");
                }
            }
        }
        
        return totalAmount;
    }

    private boolean isBasicItem(Material material) {
        return material == Material.OAK_PLANKS || 
               material == Material.SPRUCE_PLANKS ||
               material == Material.BIRCH_PLANKS ||
               material == Material.JUNGLE_PLANKS ||
               material == Material.ACACIA_PLANKS ||
               material == Material.DARK_OAK_PLANKS ||
               material == Material.CRIMSON_PLANKS ||
               material == Material.WARPED_PLANKS;
    }
    
    /**
     * Removes items from player inventory
     */
    private boolean removeItemsFromPlayer(Player player, MarketItem marketItem, int amount) {
        int remaining = amount;
        
        // Cerca prima nella GUI di vendita se è aperta
        if (player.getOpenInventory() != null && 
            player.getOpenInventory().getTopInventory().getHolder() instanceof com.dynamicmarket.gui.SellGUI) {
            
            ItemStack[] contents = player.getOpenInventory().getTopInventory().getContents();
            
            for (int i = 0; i < contents.length && remaining > 0; i++) {
                ItemStack item = contents[i];
                if (item != null && item.getType() == marketItem.getMaterial()) {
                    // Per oggetti base come le assi di quercia, confronta solo il materiale
                    if (isBasicItem(marketItem.getMaterial())) {
                        int toRemove = Math.min(remaining, item.getAmount());
                        remaining -= toRemove;
                        
                        if (toRemove >= item.getAmount()) {
                            contents[i] = null;
                        } else {
                            item.setAmount(item.getAmount() - toRemove);
                        }
                    }
                    // Altrimenti controlla anche nome e lore
                    else if (itemMatches(item, marketItem)) {
                        int toRemove = Math.min(remaining, item.getAmount());
                        remaining -= toRemove;
                        
                        if (toRemove >= item.getAmount()) {
                            contents[i] = null;
                        } else {
                            item.setAmount(item.getAmount() - toRemove);
                        }
                    }
                }
            }
            
            player.getOpenInventory().getTopInventory().setContents(contents);
            if (remaining == 0) return true;
        }
        
        // Se non è nella GUI di vendita o se rimangono ancora item da rimuovere, cerca nell'inventario del giocatore
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == marketItem.getMaterial()) {
                // Per oggetti base come le assi di quercia, confronta solo il materiale
                if (isBasicItem(marketItem.getMaterial())) {
                    int toRemove = Math.min(remaining, item.getAmount());
                    remaining -= toRemove;
                    
                    if (toRemove >= item.getAmount()) {
                        contents[i] = null;
                    } else {
                        item.setAmount(item.getAmount() - toRemove);
                    }
                }
                // Altrimenti controlla anche nome e lore
                else if (itemMatches(item, marketItem)) {
                    int toRemove = Math.min(remaining, item.getAmount());
                    remaining -= toRemove;
                    
                    if (toRemove >= item.getAmount()) {
                        contents[i] = null;
                    } else {
                        item.setAmount(item.getAmount() - toRemove);
                    }
                }
            }
        }

        player.getInventory().setContents(contents);
        player.updateInventory();
        
        return remaining == 0;
    }
    
    /**
     * Calculates the total value of selling items
     */
    public double calculateSellValue(MarketItem marketItem, int amount) {
        return marketItem.getSellPrice() * amount;
    }
    
    /**
     * Gets the maximum amount a player can sell
     */
    public int getMaxSellableAmount(Player player, MarketItem marketItem) {
        return getPlayerItemAmount(player, marketItem);
    }
    
    /**
     * Processes selling all items of a specific type
     */
    public boolean sellAllItems(Player player, MarketItem marketItem) {
        int maxAmount = getMaxSellableAmount(player, marketItem);
        
        if (maxAmount <= 0) {
            player.sendMessage("§cYou don't have any of this item to sell!");
            return false;
        }
        
        return processSale(player, marketItem, maxAmount);
    }
    
    /**
     * Checks if a player has enough items to sell
     */
    public boolean hasEnoughItems(Player player, MarketItem marketItem, int amount) {
        return getPlayerItemAmount(player, marketItem) >= amount;
    }
    
    /**
     * Gets a preview of what the player would earn from selling
     */
    public SellPreview getSellPreview(Player player, MarketItem marketItem) {
        int availableAmount = getPlayerItemAmount(player, marketItem);
        double pricePerItem = marketItem.getSellPrice();
        
        return new SellPreview(availableAmount, pricePerItem, availableAmount * pricePerItem);
    }
    
    /**
     * Inner class for sell preview information
     */
    public static class SellPreview {
        private final int availableAmount;
        private final double pricePerItem;
        private final double totalValue;
        
        public SellPreview(int availableAmount, double pricePerItem, double totalValue) {
            this.availableAmount = availableAmount;
            this.pricePerItem = pricePerItem;
            this.totalValue = totalValue;
        }
        
        public int getAvailableAmount() {
            return availableAmount;
        }
        
        public double getPricePerItem() {
            return pricePerItem;
        }
        
        public double getTotalValue() {
            return totalValue;
        }
    }
}