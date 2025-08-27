package com.dynamicmarket.listeners;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketCategory;
import com.dynamicmarket.gui.BuyGUI;
import com.dynamicmarket.gui.CategoryGUI;
import com.dynamicmarket.gui.MarketGUI;
import com.dynamicmarket.gui.SellGUI;
import com.dynamicmarket.gui.SellItemGUI;
import com.dynamicmarket.managers.ConfirmationGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.HumanEntity;

public class InventoryClickListener implements Listener {
    
    private final DynamicMarket plugin;
    
    public InventoryClickListener(DynamicMarket plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        InventoryHolder holder = event.getInventory().getHolder();
        String title = event.getView().getTitle();
        
        // Allow placing items in sell GUI
        if (holder instanceof SellGUI) {
            event.setCancelled(false);
            return;
        }
        
        // Cancel clicks in other GUIs
        if (holder instanceof CategoryGUI || 
            holder instanceof MarketGUI || 
            holder instanceof BuyGUI || 
            holder instanceof SellItemGUI) {
            event.setCancelled(true);
            
            // Handle specific GUI clicks
            if (clickedInventory != null) {
                if (holder instanceof CategoryGUI) {
                    CategoryGUI gui = (CategoryGUI) holder;
                    gui.handleClick(event.getSlot(), event.isRightClick(), event.isShiftClick());
                } 
                else if (holder instanceof MarketGUI) {
                    MarketGUI gui = (MarketGUI) holder;
                    gui.handleClick(event.getSlot());
                }
                else if (holder instanceof BuyGUI) {
                    BuyGUI gui = (BuyGUI) holder;
                    gui.handleClick(event.getSlot(), event.isRightClick(), event.isShiftClick());
                }
                else if (holder instanceof SellItemGUI) {
                    SellItemGUI gui = (SellItemGUI) holder;
                    gui.handleClick(event.getSlot(), event.isRightClick(), event.isShiftClick());
                }
            }
            return;
        }
        
        // Always cancel clicks in the player inventory when GUI is open
        if (isMarketGUI(title) || isCategoryGUI(title) || 
            isConfirmationGUI(title) || isBuyGUI(title) || isSellItemGUI(title)) {
            event.setCancelled(true);
            return;
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        InventoryHolder holder = event.getInventory().getHolder();
        
        // Allow dragging in sell GUI
        if (holder instanceof SellGUI) {
            event.setCancelled(false);
            return;
        }
        
        // Cancel drag events for all other GUIs
        if (isMarketGUI(title) || isCategoryGUI(title) || 
            isConfirmationGUI(title) || isBuyGUI(title) || isSellItemGUI(title)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (event.getSource().getViewers().isEmpty()) {
            return;
        }
        
        HumanEntity human = (HumanEntity) event.getSource().getViewers().get(0);
        String title = human.getOpenInventory().getTitle();
        InventoryHolder holder = event.getSource().getHolder();
        
        // Allow move events in sell GUI
        if (holder instanceof SellGUI) {
            event.setCancelled(false);
            return;
        }
        
        // Cancel move events for all other GUIs
        if (isMarketGUI(title) || isCategoryGUI(title) || 
            isConfirmationGUI(title) || isBuyGUI(title) || isSellItemGUI(title)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        InventoryHolder holder = event.getInventory().getHolder();
        
        // Handle SellGUI close to process items
        if (holder instanceof SellGUI) {
            SellGUI sellGUI = (SellGUI) holder;
            sellGUI.handleClose();
        }
    }
    
    private boolean isMarketGUI(String title) {
        String marketTitle = plugin.getConfigManager().getMarketTitle();
        return title.equals(marketTitle);
    }
    
    private boolean isCategoryGUI(String title) {
        String categoryPrefix = plugin.getConfigManager().getCategoryTitle().replace("{category}", "");
        return title.contains("Category:") || title.startsWith(categoryPrefix.trim());
    }
    
    private boolean isBuyGUI(String title) {
        return title.startsWith("Buy ");
    }
    
    private boolean isSellItemGUI(String title) {
        return title.startsWith("Sell ");
    }
    
    private boolean isSellGUI(String title) {
        String sellTitle = plugin.getConfigManager().getSellTitle();
        return title.equals(sellTitle);
    }
    
    private boolean isConfirmationGUI(String title) {
        return title.contains("Confirm Purchase");
    }
    
    private String extractCategoryNameFromTitle(String title) {
        String cleanTitle = title.replaceAll("§[0-9a-fk-or]", "");
        String categoryTitleFormat = plugin.getConfigManager().getCategoryTitle();
        String categoryPrefix = categoryTitleFormat.replace("{category}", "").trim();
        
        if (cleanTitle.startsWith(categoryPrefix)) {
            String categoryName = cleanTitle.substring(categoryPrefix.length()).trim();
            if (plugin.getMarketManager().getCategory(categoryName) != null) {
                return categoryName;
            }
        }
        
        for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            String categoryName = category.getName();
            String displayName = category.getDisplayName().replaceAll("§[0-9a-fk-or]", "");
            
            if (cleanTitle.contains(categoryName) || cleanTitle.contains(displayName)) {
                return categoryName;
            }
        }
        
        return null;
    }
}