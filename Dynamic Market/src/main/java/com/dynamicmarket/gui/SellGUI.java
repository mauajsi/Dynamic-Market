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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellGUI implements org.bukkit.inventory.InventoryHolder {
    
    private final DynamicMarket plugin;
    private final Player player;
    private Inventory inventory;
    
    public SellGUI(DynamicMarket plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        createInventory();
    }
    
    private void createInventory() {
        String title = plugin.getConfigManager().getSellTitle();
        inventory = Bukkit.createInventory(this, 54, title);
        
        populateInventory();
    }
    
    private void populateInventory() {
        // Clear inventory and make it completely empty
        inventory.clear();
    }
    
    public void handleClose() {
        Map<MarketItem, Integer> itemsToSell = new HashMap<>();
        Map<Integer, ItemStack> unsoldItems = new HashMap<>();
        
        // First, collect all items and their total amounts
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                MarketItem marketItem = plugin.getMarketManager().findItemByMaterial(item.getType());
                
                if (marketItem == null) {
                    plugin.getLogger().info("MarketItem non trovato per il materiale: " + item.getType());
                    unsoldItems.put(slot, item);
                } else if (itemMatches(item, marketItem)) {
                    // Add to the total amount for this market item
                    itemsToSell.merge(marketItem, item.getAmount(), Integer::sum);
                    plugin.getLogger().info("Item aggiunto per la vendita: " + item.getType() + " x" + item.getAmount());
                } else {
                    plugin.getLogger().info("Item non corrisponde al MarketItem: " + item.getType());
                    plugin.getLogger().info("MarketItem display name: " + marketItem.getDisplayName());
                    plugin.getLogger().info("MarketItem lore: " + marketItem.getLore());
                    if (item.hasItemMeta()) {
                        plugin.getLogger().info("Item display name: " + item.getItemMeta().getDisplayName());
                        plugin.getLogger().info("Item lore: " + item.getItemMeta().getLore());
                    } else {
                        plugin.getLogger().info("Item non ha metadata");
                    }
                    unsoldItems.put(slot, item);
                }
            }
        }
        
        // Return unsold items to player
        for (ItemStack item : unsoldItems.values()) {
            player.getInventory().addItem(item);
            player.sendMessage(plugin.getConfigManager().getMessage("market.sell.not-sellable"));
        }
        
        // Now process all sales
        if (!itemsToSell.isEmpty()) {
            double totalEarnings = 0.0;
            
            for (Map.Entry<MarketItem, Integer> entry : itemsToSell.entrySet()) {
                MarketItem marketItem = entry.getKey();
                int amount = entry.getValue();
                
                SellHandler sellHandler = new SellHandler(plugin);
                double earnings = sellHandler.processSaleQuiet(player, marketItem, amount);
                if (earnings > 0) {
                    totalEarnings += earnings;
                }
            }
            
            if (totalEarnings > 0) {
                player.sendMessage(plugin.getConfigManager().getMessage("market.sell.all-success",
                    "earnings", String.format("%.2f", totalEarnings)));
            }
        }
    }
    
    private boolean itemMatches(ItemStack item, MarketItem marketItem) {
        // Per gli item base come le Oak Planks, confronta solo il materiale
        if (isBasicItem(item.getType())) {
            return item.getType() == marketItem.getMaterial();
        }
        
        // Basic material check
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
            return false;
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
    
    private boolean isBasicItem(Material material) {
        // Lista di materiali base che dovrebbero essere confrontati solo per tipo
        return material == Material.OAK_PLANKS ||
               material == Material.SPRUCE_PLANKS ||
               material == Material.BIRCH_PLANKS ||
               material == Material.JUNGLE_PLANKS ||
               material == Material.ACACIA_PLANKS ||
               material == Material.DARK_OAK_PLANKS ||
               material == Material.CRIMSON_PLANKS ||
               material == Material.WARPED_PLANKS;
    }
    
    public void refresh() {
        populateInventory();
    }
    
    public void open() {
        player.openInventory(inventory);
        player.sendMessage(plugin.getConfigManager().getMessage("sell-gui-opened"));
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Handles click events in the sell GUI
     */
    public void handleClick(int slot, ItemStack clickedItem, boolean isShiftClick, boolean isRightClick) {
        // Allow any click in the inventory
    }
}