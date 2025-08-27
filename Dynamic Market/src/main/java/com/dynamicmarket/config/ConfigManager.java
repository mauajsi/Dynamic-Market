package com.dynamicmarket.config;

import com.dynamicmarket.DynamicMarket;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.ChatColor;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {
    
    private final DynamicMarket plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    
    public ConfigManager(DynamicMarket plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.messagesFile = new File(plugin.getDataFolder(), "messages_en.yml");
        loadConfig();
    }
    
    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        
        // Load main config
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Load messages
        if (!messagesFile.exists()) {
            plugin.saveResource("messages_en.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        
        // Set default values if they don't exist
        setDefaults();
        saveConfig();
    }
    
    private void setDefaults() {
        if (!config.contains("market.price-adjustment-rate")) {
            config.set("market.price-adjustment-rate", 0.005);
        }
        
        if (!config.contains("market.min-price")) {
            config.set("market.min-price", 0.1);
        }
        
        if (!config.contains("market.max-price")) {
            config.set("market.max-price", 1000.0);
        }
        
        if (!config.contains("market.auto-save-interval")) {
            config.set("market.auto-save-interval", 300); // 5 minutes
        }
        
        if (!config.contains("gui.market-title")) {
            config.set("gui.market-title", "§6§lDynamic Market");
        }
        
        if (!config.contains("gui.category-title")) {
            config.set("gui.category-title", "§6§l{category} - Market");
        }
        
        if (!config.contains("gui.sell-title")) {
            config.set("gui.sell-title", "§6§lSell Items");
        }
        
        if (!config.contains("messages.no-permission")) {
            config.set("messages.no-permission", "§cYou don't have permission to do that!");
        }
        
        if (!config.contains("messages.insufficient-funds")) {
            config.set("messages.insufficient-funds", "§cYou don't have enough money! Required: §e${amount}");
        }
        
        if (!config.contains("messages.insufficient-stock")) {
            config.set("messages.insufficient-stock", "§cNot enough items in stock! Available: §e{stock}");
        }
        
        if (!config.contains("messages.purchase-success")) {
            config.set("messages.purchase-success", "§aSuccessfully purchased §e{amount}x {item} §afor §e${price}!");
        }
        
        if (!config.contains("messages.sell-success")) {
            config.set("messages.sell-success", "§aSuccessfully sold §e{amount}x {item} §afor §e${price}!");
        }
        
        if (!config.contains("messages.inventory-full")) {
            config.set("messages.inventory-full", "§cYour inventory is full!");
        }
        
        if (!config.contains("messages.item-not-sellable")) {
            config.set("messages.item-not-sellable", "§cThis item cannot be sold to the market!");
        }
        
        if (!config.contains("messages.category-created")) {
            config.set("messages.category-created", "§aCategory §e{category} §acreated successfully!");
        }
        
        if (!config.contains("messages.item-added")) {
            config.set("messages.item-added", "§aItem §e{item} §aadded to category §e{category}§a!");
        }
        
        if (!config.contains("messages.market-reset")) {
            config.set("messages.market-reset", "§aMarket has been reset to default configuration!");
        }
    }
    
    public void saveConfig() {
        try {
            config.save(configFile);
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml or messages_en.yml", e);
        }
    }
    
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    // Getter methods for configuration values
    public double getPriceAdjustmentRate() {
        return config.getDouble("market.price-adjustment-rate", 0.005);
    }
    
    public double getMinPrice() {
        return config.getDouble("market.min-price", 0.1);
    }
    
    public double getMaxPrice() {
        return config.getDouble("market.max-price", 1000.0);
    }

    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }
    
    public String getString(String path) {
        return config.getString(path);
    }
    
    public int getAutoSaveInterval() {
        return config.getInt("market.auto-save-interval", 300);
    }
    
    public String getMarketTitle() {
        return config.getString("gui.market-title", "§6§lDynamic Market");
    }
    
    public String getCategoryTitle() {
        return config.getString("gui.category-title", "§6§l{category} - Market");
    }
    
    public String getSellTitle() {
        return config.getString("gui.sell-title", "§6§lSell Items");
    }
    
    public String getMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            message = messagesConfig.getString("messages." + key);
        }
        if (message == null) {
            message = messagesConfig.getString("market.messages." + key);
        }
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : "§cMessage not found: " + key;
    }
    
    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        
        return message;
    }
    
    // Setter methods for configuration values
    public void setPriceAdjustmentRate(double rate) {
        config.set("market.price-adjustment-rate", rate);
        saveConfig();
    }
    
    public void setMinPrice(double price) {
        config.set("market.min-price", price);
        saveConfig();
    }
    
    public void setMaxPrice(double price) {
        config.set("market.max-price", price);
        saveConfig();
    }
    
    public void setAutoSaveInterval(int interval) {
        config.set("market.auto-save-interval", interval);
        saveConfig();
    }
    
    public void setSetting(String key, Object value) {
        config.set(key, value);
        saveConfig();
    }
    
    public FileConfiguration getConfig() {
        return config;
    }

}