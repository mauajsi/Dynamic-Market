package com.dynamicmarket.data;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketCategory;
import com.dynamicmarket.data.MarketItem;
import com.dynamicmarket.managers.DatabaseManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class DataManager {
    
    private final DynamicMarket plugin;
    private final DatabaseManager databaseManager;
    private final Map<String, MarketCategory> categories;
    private final Map<String, MarketItem> items;
    
    public DataManager(DynamicMarket plugin) {
        this.plugin = plugin;
        this.databaseManager = new DatabaseManager(plugin);
        this.categories = new HashMap<>();
        this.items = new HashMap<>();
        loadAllData();
    }
    
    public void loadAllData() {
        loadCategories();
        loadItems();
    }
    
    public void saveAllData() {
        saveCategories();
        saveItems();
    }
    
    public void loadCategories() {
        categories.clear();
        List<MarketCategory> loadedCategories = databaseManager.loadCategories();
        for (MarketCategory category : loadedCategories) {
            categories.put(category.getName().toLowerCase(), category);
        }
    }
    
    public void loadItems() {
        items.clear();
        List<MarketItem> loadedItems = databaseManager.loadItems();
        for (MarketItem item : loadedItems) {
            items.put(item.getMaterial().name().toLowerCase(), item);
        }
    }
    
    public void saveCategories() {
        for (MarketCategory category : categories.values()) {
            databaseManager.saveCategory(category);
        }
    }
    
    public void saveItems() {
        for (MarketItem item : items.values()) {
            databaseManager.saveItem(item);
        }
    }
    
    public MarketCategory getCategory(String name) {
        return categories.get(name.toLowerCase());
    }
    
    public Collection<MarketCategory> getCategories() {
        return categories.values();
    }
    
    public MarketItem getItem(String material) {
        return items.get(material.toLowerCase());
    }
    
    public Collection<MarketItem> getItems() {
        return items.values();
    }
    
    public Collection<MarketItem> getItemsInCategory(String categoryName) {
        List<MarketItem> categoryItems = new ArrayList<>();
        for (MarketItem item : items.values()) {
            if (item.getCategory().equalsIgnoreCase(categoryName)) {
                categoryItems.add(item);
            }
        }
        return categoryItems;
    }
    
    public void addCategory(MarketCategory category) {
        categories.put(category.getName().toLowerCase(), category);
        databaseManager.saveCategory(category);
    }
    
    public void removeCategory(String name) {
        categories.remove(name.toLowerCase());
        databaseManager.deleteCategory(name);
    }
    
    public void addItem(MarketItem item) {
        items.put(item.getMaterial().name().toLowerCase(), item);
        databaseManager.saveItem(item);
    }
    
    public void removeItem(String material) {
        items.remove(material.toLowerCase());
        databaseManager.deleteItem(material);
    }
    
    public boolean categoryExists(String name) {
        return categories.containsKey(name.toLowerCase());
    }
    
    public boolean itemExists(String material) {
        return items.containsKey(material.toLowerCase());
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public void resetMarket() {
        categories.clear();
        items.clear();
        databaseManager.resetDatabase();
        loadAllData();
    }
}