package com.dynamicmarket.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MarketItem {
    
    private String id;
    private Material material;
    private String displayName;
    private List<String> lore;
    private double buyPrice;
    private double sellPrice;
    private int stock;
    private int totalSold;
    private int totalBought;
    private long lastUpdated;
    private String category;
    
    public MarketItem(String id, Material material, String displayName, List<String> lore, 
                     double buyPrice, double sellPrice, int stock, String category) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stock = stock;
        this.totalSold = 0;
        this.totalBought = 0;
        this.lastUpdated = System.currentTimeMillis();
        this.category = category;
    }
    
    public ItemStack createItemStack(int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (displayName != null && !displayName.isEmpty()) {
                meta.setDisplayName(displayName);
            }
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void updatePriceOnBuy(int amount) {
        totalBought += amount;
        // Increase buy price by 0.5% per item bought
        buyPrice = Math.min(1000.0, buyPrice * (1 + (amount * 0.005)));
        // Decrease sell price slightly
        sellPrice = Math.max(0.1, sellPrice * (1 - (amount * 0.0025)));
        lastUpdated = System.currentTimeMillis();
    }
    
    public void updatePriceOnSell(int amount) {
        totalSold += amount;
        stock += amount;
        // Decrease buy price slightly
        buyPrice = Math.max(0.1, buyPrice * (1 - (amount * 0.0025)));
        // Decrease sell price by 0.5% per item sold
        sellPrice = Math.max(0.1, sellPrice * (1 - (amount * 0.005)));
        lastUpdated = System.currentTimeMillis();
    }
    
    public void increaseStock(int amount) {
        stock += amount;
    }
    
    public void updatePricesAfterSell(int amount) {
        updatePriceOnSell(amount);
    }
    
    public void recordSell(int amount) {
        totalSold += amount;
        lastUpdated = System.currentTimeMillis();
    }
    
    public boolean canBuy(int amount) {
        return stock >= amount;
    }
    
    public void decreaseStock(int amount) {
        stock = Math.max(0, stock - amount);
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public List<String> getLore() {
        return lore;
    }
    
    public void setLore(List<String> lore) {
        this.lore = lore;
    }
    
    public double getBuyPrice() {
        return buyPrice;
    }
    
    public void setBuyPrice(double buyPrice) {
        this.buyPrice = Math.max(0.1, Math.min(1000.0, buyPrice));
    }
    
    public double getSellPrice() {
        return sellPrice;
    }
    
    public void setSellPrice(double sellPrice) {
        this.sellPrice = Math.max(0.1, Math.min(1000.0, sellPrice));
    }
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = Math.max(0, stock);
    }
    
    public int getTotalSold() {
        return totalSold;
    }
    
    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }
    
    public int getTotalBought() {
        return totalBought;
    }
    
    public void setTotalBought(int totalBought) {
        this.totalBought = totalBought;
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void recordBuy(int amount) {
        totalBought += amount;
        lastUpdated = System.currentTimeMillis();
    }

    public void updatePricesAfterBuy(int amount) {
        updatePriceOnBuy(amount);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}