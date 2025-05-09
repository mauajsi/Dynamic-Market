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

        // Calculate size based on number of categories, minimum 9 slots
        int numCategories = categories.getKeys(false).size();
        int size = Math.max(9, ((numCategories - 1) / 9 + 1) * 9);

        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_GREEN + "Market - Categories");

        int slot = 0;
        for (String category : categories.getKeys(false)) {
            String iconPath = category + ".icon";
            Material icon = Material.STONE; // Default icon
            
            if (categories.contains(iconPath)) {
                Material configIcon = Material.getMaterial(categories.getString(iconPath));
                if (configIcon != null) {
                    icon = configIcon;
                }
            }
            
            createCategoryButton(gui, slot, icon, category);
            slot++;
        }

        player.openInventory(gui);
    }

    public void openCategoryMenu(Player player, String category) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Market - " + category);

        List<Material> items = getCategoryItems(category);
        int slot = 0;

        for (Material material : items) {
            if (slot >= 45) break; // Leave last row for navigation

            double price = marketManager.getPrice("buy", material);
            double price2 = marketManager.getPrice("sell", material);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            // Add the material name
            meta.setDisplayName(ChatColor.YELLOW + formatMaterialName(material.name()));
            
            // Add the price and instructions in the lore
            List<String> lore = new ArrayList<>();
            lore.add("");  // Empty line for spacing
            lore.add(ChatColor.GOLD + "Valore (Buy): " + ChatColor.GREEN + "$" + String.format("%.2f", price));
            lore.add(ChatColor.GOLD + "Valore (Sell): " + ChatColor.GREEN + "$" + String.format("%.2f", price2));
            lore.add("");  // Empty line for spacing
            lore.add(ChatColor.GRAY + "Click sinistro per comprare");
            lore.add(ChatColor.GRAY + "Click destro per vendere");
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

    private ItemStack createMarketItem(MarketItem marketItem) {
        ItemStack item = marketItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        
        List<String> lore = new ArrayList<>();
        if (meta.hasLore()) {
            lore = meta.getLore();
        }
        
        // Add price to lore
        lore.add(ChatColor.GOLD + "Prezzo (Buy): " + ChatColor.GREEN + "$" + marketItem.getBuyPrice());
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
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
