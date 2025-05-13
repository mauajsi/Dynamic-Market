package com.example.economia;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.ItemStack;
import net.milkbowl.vault.economy.Economy;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class MarketManager {
    private Economy economy;
    private List<MarketItem> marketItems;
    private FileConfiguration config;
    private Plugin plugin;

    public MarketManager(Economy economy, Plugin plugin) {
        this.economy = economy;
        this.plugin = plugin;
        this.marketItems = new ArrayList<>();
        this.config = plugin.getConfig();
        loadMarketFromConfig();
    }

    public Economy getEconomy() {
        return economy;
    }

    public List<MarketItem> getMarketItems() {
        return new ArrayList<>(marketItems);
    }

    public double getPrice(String type, Material material) {
        for (MarketItem item : marketItems) {
            if (item.getMaterial() == material) {
                return type.equalsIgnoreCase("sell") ? item.getSellPrice() : item.getBuyPrice();
            }
        }
        return 1.0;
    }

    public double getItemPrice(String type, ItemStack item) {
        return getPrice(type, item.getType());
    }

    public void adjustPriceForBuy(Material material, int amount) {
        double currentPrice = getPrice("buy", material);
        // Reduced from 0.02 (2%) to 0.005 (0.5%) per item
        double newPrice = currentPrice * (1 + (0.005 * amount));
        updatePrice(material, newPrice, "buy");
    }

    public void adjustPriceForSell(Material material, int amount) {
        double currentPrice = getPrice("sell", material);
        // Reduced from 0.02 (2%) to 0.005 (0.5%) per item
        double newPrice = currentPrice * (1 - (0.005 * amount));
        updatePrice(material, newPrice, "sell");
    }

    private void loadMarketFromConfig() {
        ConfigurationSection categories = config.getConfigurationSection("categories");
        if (categories == null) return;

        for (String categoryName : categories.getKeys(false)) {
            ConfigurationSection category = categories.getConfigurationSection(categoryName);
            ConfigurationSection items = category.getConfigurationSection("items");
            
            if (items != null) {
                for (String itemName : items.getKeys(false)) {
                    Material material = Material.getMaterial(itemName);
                    if (material != null) {
                        double buyPrice = items.getDouble(itemName + ".buy_price", 1.0);
                        double sellPrice = items.getDouble(itemName + ".sell_price", 0.5);
                        marketItems.add(new MarketItem(material, buyPrice, sellPrice));
                    }
                }
            }
        }
    }

    public void saveMarketData() {
        for (MarketItem item : marketItems) {
            String path = "categories.";
            for (String category : config.getConfigurationSection("categories").getKeys(false)) {
                if (config.contains("categories." + category + ".items." + item.getMaterial().name())) {
                    path += category + ".items." + item.getMaterial().name();
                    config.set(path + ".buy_price", item.getBuyPrice());
                    config.set(path + ".sell_price", item.getSellPrice());
                    break;
                }
            }
        }
        plugin.saveConfig();
    }

    public void updatePrice(Material material, double price, String type) {
        double finalPrice = Math.max(0.1, Math.min(1000.0, price)); // Limit price between 0.1 and 1000.0
        
        for (MarketItem item : marketItems) {
            if (item.getMaterial() == material) {
                if (type.equalsIgnoreCase("sell")) {
                    item.setSellPrice(finalPrice);
                } else {
                    item.setBuyPrice(finalPrice);
                }
                break;
            }
        }
        saveMarketData();
    }

    public boolean addItem(String category, Material material, double buyPrice, double sellPrice) {
        ConfigurationSection categories = plugin.getConfig().getConfigurationSection("categories");
        if (categories == null || !categories.contains(category)) {
            return false;
        }

        String itemPath = "categories." + category + ".items." + material.name();
        plugin.getConfig().set(itemPath + ".buy_price", buyPrice);
        plugin.getConfig().set(itemPath + ".sell_price", sellPrice);
        plugin.saveConfig();
        return true;
    }

    public boolean removeItem(String category, Material material) {
        ConfigurationSection categories = plugin.getConfig().getConfigurationSection("categories");
        if (categories == null || !categories.contains(category)) {
            return false;
        }

        String itemPath = "categories." + category + ".items." + material.name();
        if (!plugin.getConfig().contains(itemPath)) {
            return false;
        }

        plugin.getConfig().set(itemPath, null);
        plugin.saveConfig();
        return true;
    }

    public boolean updateItemPrices(String category, Material material, double buyPrice, double sellPrice) {
        ConfigurationSection categories = plugin.getConfig().getConfigurationSection("categories");
        if (categories == null || !categories.contains(category)) {
            return false;
        }

        String itemPath = "categories." + category + ".items." + material.name();
        if (!plugin.getConfig().contains(itemPath)) {
            return false;
        }

        plugin.getConfig().set(itemPath + ".buy_price", buyPrice);
        plugin.getConfig().set(itemPath + ".sell_price", sellPrice);
        plugin.saveConfig();
        return true;
    }

    public boolean moveItem(String fromCategory, String toCategory, Material material) {
        ConfigurationSection categories = plugin.getConfig().getConfigurationSection("categories");
        if (categories == null || !categories.contains(fromCategory) || !categories.contains(toCategory)) {
            return false;
        }

        String fromPath = "categories." + fromCategory + ".items." + material.name();
        if (!plugin.getConfig().contains(fromPath)) {
            return false;
        }

        double buyPrice = plugin.getConfig().getDouble(fromPath + ".buy_price");
        double sellPrice = plugin.getConfig().getDouble(fromPath + ".sell_price");

        // Remove from old category
        plugin.getConfig().set(fromPath, null);

        // Add to new category
        String toPath = "categories." + toCategory + ".items." + material.name();
        plugin.getConfig().set(toPath + ".buy_price", buyPrice);
        plugin.getConfig().set(toPath + ".sell_price", sellPrice);

        plugin.saveConfig();
        return true;
    }

    public boolean addCategory(String categoryName, Material icon) {
        if (plugin.getConfig().contains("categories." + categoryName)) {
            return false;
        }
        
        plugin.getConfig().set("categories." + categoryName + ".icon", icon.name());
        plugin.getConfig().set("categories." + categoryName + ".items", new HashMap<>());
        plugin.saveConfig();
        return true;
    }

    public List<String> getCategories() {
        ConfigurationSection categories = plugin.getConfig().getConfigurationSection("categories");
        return categories != null ? 
            new ArrayList<>(categories.getKeys(false)) : 
            new ArrayList<>();
    }

    public boolean resetMarket() {
        try {
            // Delete current config
            plugin.getConfig().set("categories", null);
            
            // Save empty config
            plugin.saveConfig();
            
            // Save default config
            plugin.saveResource("config.yml", true);
            
            // Reload config
            plugin.reloadConfig();
            this.config = plugin.getConfig();
            
            // Reload market items
            marketItems.clear();
            loadMarketFromConfig();
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reset market: " + e.getMessage());
            return false;
        }
    }
}