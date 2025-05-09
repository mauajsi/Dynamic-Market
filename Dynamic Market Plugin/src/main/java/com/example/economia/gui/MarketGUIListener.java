package com.example.economia.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.example.economia.MarketManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import net.milkbowl.vault.economy.Economy;

public class MarketGUIListener implements Listener {
    private final MarketManager marketManager;
    private final MarketGUI marketGUI;
    private final Economy economy;

    public MarketGUIListener(MarketManager marketManager, MarketGUI marketGUI, Economy economy) {
        this.marketManager = marketManager;
        this.marketGUI = marketGUI;
        this.economy = economy;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (!event.getView().getTitle().startsWith(ChatColor.DARK_GREEN + "Market")) return;
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Handle back button click
        if (clickedItem.getType() == Material.ARROW && 
            clickedItem.getItemMeta().getDisplayName().equals(ChatColor.RED + "Back to Categories")) {
            marketGUI.openMainMenu(player);
            return;
        }

        String title = event.getView().getTitle();
        int slot = event.getSlot();

        if (title.equals(ChatColor.DARK_GREEN + "Market - Categories")) {
            handleCategoryClick(player, clickedItem);
        } else {
            handleItemClick(player, clickedItem, event.isRightClick(), slot);
        }
    }

    private void handleCategoryClick(Player player, ItemStack clickedItem) {
        if (clickedItem.getItemMeta() == null) return;
        
        String categoryName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        marketGUI.openCategoryMenu(player, categoryName);
    }

    private void handleItemClick(Player player, ItemStack clickedItem, boolean isRightClick, int slot) {
        if (clickedItem.getItemMeta() == null) return;
    
        if (clickedItem.getType().name().equals("ARROW")) {
            marketGUI.openMainMenu(player);
            return;
        }
    
        // Open quantity selector GUI
        openQuantitySelector(player, clickedItem.getType(), isRightClick);
    }

    private void openQuantitySelector(Player player, Material material, boolean isSelling) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Select Quantity - " + formatMaterialName(material.name()));
        
        // Single item
        ItemStack singleItem = new ItemStack(material, 1);
        ItemMeta singleMeta = singleItem.getItemMeta();
        singleMeta.setDisplayName(ChatColor.YELLOW + "Buy/Sell 1x");
        List<String> singleLore = new ArrayList<>();
        double pricePerUnit = marketManager.getPrice("sell", material);
        double pricePerUnit2 = marketManager.getPrice("buy", material);
        singleLore.add(ChatColor.GREEN + "Price: $" + String.format("%.2f", isSelling ? pricePerUnit : pricePerUnit2));
        singleLore.add(ChatColor.GRAY + (isSelling ? "Click to sell" : "Click to buy"));
        singleMeta.setLore(singleLore);
        singleItem.setItemMeta(singleMeta);
        gui.setItem(4, singleItem);
    
        // 16 items (left)
        if (material.getMaxStackSize() > 1) {
            ItemStack leftStack = new ItemStack(material, 16);
            ItemMeta leftMeta = leftStack.getItemMeta();
            leftMeta.setDisplayName(ChatColor.YELLOW + "Buy/Sell 16x");
            List<String> leftLore = new ArrayList<>();
            leftLore.add(ChatColor.GREEN + "Price: $" + String.format("%.2f", pricePerUnit * 16));
            leftLore.add(ChatColor.GRAY + (isSelling ? "Click to sell" : "Click to buy"));
            leftMeta.setLore(leftLore);
            leftStack.setItemMeta(leftMeta);
            gui.setItem(3, leftStack);
        }
    
        // 32 items (far left)
        if (material.getMaxStackSize() > 1) {
            ItemStack farLeftStack = new ItemStack(material, 32);
            ItemMeta farLeftMeta = farLeftStack.getItemMeta();
            farLeftMeta.setDisplayName(ChatColor.YELLOW + "Buy/Sell 32x");
            List<String> farLeftLore = new ArrayList<>();
            farLeftLore.add(ChatColor.GREEN + "Price: $" + String.format("%.2f", pricePerUnit * 32));
            farLeftLore.add(ChatColor.GRAY + (isSelling ? "Click to sell" : "Click to buy"));
            farLeftMeta.setLore(farLeftLore);
            farLeftStack.setItemMeta(farLeftMeta);
            gui.setItem(2, farLeftStack);
        }
    
        // Mirror the same options on the right side
        if (material.getMaxStackSize() > 1) {
            gui.setItem(5, gui.getItem(3)); // 16 items
            gui.setItem(6, gui.getItem(2)); // 32 items
        }
    
        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backButton.setItemMeta(backMeta);
        gui.setItem(8, backButton);
    
        player.openInventory(gui);
    }

    @EventHandler
    public void onQuantitySelect(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(ChatColor.DARK_GREEN + "Select Quantity")) {
            return;
        }
        
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked.getType() == Material.ARROW) {
            // Get the original category from the title (e.g., "Select Quantity - Coal" -> get the category it belongs to)
            for (String category : marketGUI.getPlugin().getConfig().getConfigurationSection("categories").getKeys(false)) {
                if (marketGUI.getCategoryItems(category).contains(clicked.getType())) {
                    marketGUI.openCategoryMenu(player, category);
                    return;
                }
            }
            // If category not found, return to main menu
            marketGUI.openMainMenu(player);
            return;
        }
    
        String amountStr = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).split(" ")[1];
        int amount = Integer.parseInt(amountStr.substring(0, amountStr.length() - 1));
        Material material = clicked.getType();
        
        if (event.isRightClick()) {
            sellItems(player, material, amount);
        } else {
            buyItems(player, material, amount);
        }
    }

    private String formatMaterialName(String name) {
        return Arrays.stream(name.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private void buyItems(Player player, Material material, int amount) {
        double pricePerUnit = marketManager.getPrice("buy", material);
        double totalPrice = Math.floor((pricePerUnit * amount) * 10) / 10;
        
        if (economy.getBalance(player) >= totalPrice) {
            economy.withdrawPlayer(player, totalPrice);
            ItemStack items = new ItemStack(material, amount);
            
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(items);
            
            if (!leftover.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Your inventory is full! Some items were not given.");
                economy.depositPlayer(player, totalPrice * (leftover.get(0).getAmount() / (double) amount));
            } else {
                marketManager.adjustPriceForBuy(material, amount);
                player.sendMessage(ChatColor.GREEN + "Bought " + amount + "x " + material.name() + " for $" + totalPrice);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough money!");
        }
    }

    private void sellItems(Player player, Material material, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        
        if (count >= amount) {
            double pricePerUnit = marketManager.getPrice("sell", material);
            double totalPrice = Math.floor((pricePerUnit * amount) * 10) / 10;
            
            int remaining = amount;
            for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() == material) {
                    if (item.getAmount() <= remaining) {
                        remaining -= item.getAmount();
                        player.getInventory().setItem(i, null);
                    } else {
                        item.setAmount(item.getAmount() - remaining);
                        remaining = 0;
                    }
                }
            }
            
            player.updateInventory();
            economy.depositPlayer(player, totalPrice);
            
            marketManager.adjustPriceForSell(material, amount);
            player.sendMessage(ChatColor.GREEN + "Sold " + amount + "x " + material.name() + " for $" + totalPrice);
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough items to sell! You have " + count + " but need " + amount);
        }
    }

    private void updateItemPriceInGUI(Player player, ItemStack clickedItem, Material material) {
        double buyPrice = marketManager.getPrice("buy", material);
        double sellPrice = marketManager.getPrice("sell", material);
        
        ItemMeta meta = clickedItem.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Buy Price: $" + String.format("%.2f", buyPrice));
        lore.add(ChatColor.GREEN + "Sell Price: $" + String.format("%.2f", sellPrice));
        lore.add(ChatColor.GRAY + "Left-click to buy");
        lore.add(ChatColor.GRAY + "Right-click to sell");
        meta.setLore(lore);
        clickedItem.setItemMeta(meta);
    }
}