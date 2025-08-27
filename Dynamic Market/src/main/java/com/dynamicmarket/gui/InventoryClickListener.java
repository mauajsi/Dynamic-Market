package com.dynamicmarket.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        // Cancel all clicks in our custom GUIs
        if (holder instanceof CategoryGUI || 
            holder instanceof MarketGUI || 
            holder instanceof BuyGUI || 
            holder instanceof SellItemGUI ||
            holder instanceof SellGUI) {
            
            event.setCancelled(true); // Prevent item movement
            
            // Handle clicks based on GUI type
            if (holder instanceof CategoryGUI) {
                CategoryGUI gui = (CategoryGUI) holder;
                gui.handleClick(event.getRawSlot(), event.isRightClick(), event.isShiftClick());
            } 
            else if (holder instanceof MarketGUI) {
                MarketGUI gui = (MarketGUI) holder;
                gui.handleClick(event.getRawSlot());
            }
            else if (holder instanceof BuyGUI) {
                BuyGUI gui = (BuyGUI) holder;
                gui.handleClick(event.getRawSlot(), event.isRightClick(), event.isShiftClick());
            }
            else if (holder instanceof SellItemGUI) {
                SellItemGUI gui = (SellItemGUI) holder;
                gui.handleClick(event.getRawSlot(), event.isRightClick(), event.isShiftClick());
            }
            else if (holder instanceof SellGUI) {
                SellGUI gui = (SellGUI) holder;
                gui.handleClick(event.getRawSlot(), event.getClickedInventory().getItem(event.getRawSlot()), event.isShiftClick(), event.isRightClick());
            }
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        
        // Cancel dragging in our custom GUIs
        if (holder instanceof CategoryGUI || 
            holder instanceof MarketGUI || 
            holder instanceof BuyGUI || 
            holder instanceof SellItemGUI ||
            holder instanceof SellGUI) {
            event.setCancelled(true);
        }
    }
}