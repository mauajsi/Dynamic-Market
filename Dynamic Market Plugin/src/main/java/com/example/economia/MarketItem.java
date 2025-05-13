package com.example.economia;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MarketItem {
    private ItemStack item;
    private double buyPrice, sellPrice;

    public MarketItem(Material material, double buyPrice, double sellPrice) {
        this.item = new ItemStack(material);
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public Material getMaterial() {
        return item.getType();
    }

    public ItemStack getItemStack() {  // Changed from getItem() to getItemStack()
        return item.clone();
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }
    
    public void setBuyPrice(double price) {
        this.buyPrice = price;
    }

    public void setSellPrice(double price) {
        this.sellPrice = price;
    }
}