package com.example.economia.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.entity.Player;

public class SellGUIListener implements Listener {
    private final SellGUI sellGUI;

    public SellGUIListener(SellGUI sellGUI) {
        this.sellGUI = sellGUI;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Vendi Items")) {
            sellGUI.processSale((Player) event.getPlayer(), event.getInventory());
        }
    }
}