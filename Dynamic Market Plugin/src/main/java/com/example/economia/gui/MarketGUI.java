package com.example.economia.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.ConfigurationSection;

import com.example.economia.MarketItem;
import com.example.economia.MarketManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class MarketGUI {
    private final MarketManager marketManager;
    private final Plugin plugin;

    public MarketGUI(MarketManager marketManager, Plugin plugin) {
        this.marketManager = marketManager;
        this.plugin = plugin;
    }

    // Change from private to public
    public List<Material> getCategoryItems(String category) {
        List<Material> items = new ArrayList<>();
        ConfigurationSection categorySection = plugin.getConfig()
            .getConfigurationSection("categories." + category + ".items");
        
        if (categorySection != null) {
            for (String itemName : categorySection.getKeys(false)) {
                Material material = Material.getMaterial(itemName);
                if (material != null) {
                    items.add(material);
                }
            }
        }
        return items;
    }

    public void openMainMenu(Player player) {
        ConfigurationSection categories = plugin.getConfig().getConfigurationSection("categories");
        if (categories == null || categories.getKeys(false).isEmpty()) {
            player.sendMessage(ChatColor.RED + "Market categories are not configured properly.");
            return;
        }

        // Always create a 54-slot inventory (6 rows)
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Market - Categories");

        for (String category : categories.getKeys(false)) {
            String iconPath = category + ".icon";
            String slotPath = category + ".slot";
            Material icon = Material.STONE; // Default icon
            
            // Get custom slot from config, default to next available if not specified
            int slot = categories.getInt(slotPath, -1);
            if (slot < 0 || slot >= 54) {
                continue; // Skip if invalid slot
            }
            
            if (categories.contains(iconPath)) {
                Material configIcon = Material.getMaterial(categories.getString(iconPath));
                if (configIcon != null) {
                    icon = configIcon;
                }
            }
            
            createCategoryButton(gui, slot, icon, category);
        }

        player.openInventory(gui);
    }

    public void openCategoryMenu(Player player, String category) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Market - " + category);

        List<Material> items = getCategoryItems(category);
        int slot = 0;

        for (Material material : items) {
            if (slot >= 45) break;

            double price = marketManager.getPrice("buy", material);
            double price2 = marketManager.getPrice("sell", material);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            meta.setDisplayName(ChatColor.YELLOW + formatMaterialName(material.name()));
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GOLD + "Price (Buy): " + ChatColor.GREEN + "$" + String.format("%.2f", price));
            lore.add(ChatColor.GOLD + "Price (Sell): " + ChatColor.GREEN + "$" + String.format("%.2f", price2));
            lore.add("");
            lore.add(ChatColor.GRAY + "Left click to buy");
            lore.add(ChatColor.GRAY + "Right click to sell");
            meta.setLore(lore);
            
            item.setItemMeta(meta);
            gui.setItem(slot, item);
            slot++;
        }

        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back to Categories");
        backButton.setItemMeta(backMeta);
        gui.setItem(49, backButton);

        player.openInventory(gui);
    }

    private ItemStack createMarketItem(MarketItem marketItem) {
        ItemStack item = marketItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        
        List<String> lore = new ArrayList<>();
        if (meta.hasLore()) {
            lore = meta.getLore();
        }
        
        lore.add(ChatColor.GOLD + "Price (Buy): " + ChatColor.GREEN + "$" + marketItem.getBuyPrice());
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }

    private void createCategoryButton(Inventory gui, int slot, Material icon, String category) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + category);
        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }

    private String formatMaterialName(String name) {
        return Arrays.stream(name.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(name);
    }

    public void updateInventory(Inventory inventory) {
        for (MarketItem marketItem : marketManager.getMarketItems()) {
            ItemStack displayItem = createMarketItem(marketItem);
            inventory.addItem(displayItem);
        }
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
