package com.dynamicmarket.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketCategory {
    
    private String name;
    private Material icon;
    private String displayName;
    private List<String> description;
    private int guiSlot;
    private Map<String, MarketItem> items;
    
    public MarketCategory(String name, Material icon, String displayName, List<String> description, int guiSlot) {
        this.name = name;
        this.icon = icon;
        this.displayName = displayName;
        this.description = description != null ? description : new ArrayList<>();
        this.guiSlot = guiSlot;
        this.items = new HashMap<>();
    }
    
    public ItemStack createCategoryIcon() {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(displayName != null ? displayName : name);
            
            List<String> lore = new ArrayList<>(description);
            lore.add("");
            lore.add("§7Items: §e" + items.size());
            lore.add("§7Click to browse!");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void addItem(MarketItem item) {
        items.put(item.getId(), item);
    }
    
    public void removeItem(String itemId) {
        items.remove(itemId);
    }
    
    public MarketItem getItem(String itemId) {
        return items.get(itemId);
    }
    
    public MarketItem getItemByMaterial(Material material) {
        return items.values().stream()
                .filter(item -> item.getMaterial() == material)
                .findFirst()
                .orElse(null);
    }
    
    public boolean hasItem(String itemId) {
        return items.containsKey(itemId);
    }
    
    public boolean hasItemByMaterial(Material material) {
        return items.values().stream()
                .anyMatch(item -> item.getMaterial() == material);
    }
    
    public List<MarketItem> getAllItems() {
        return new ArrayList<>(items.values());
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public void clearItems() {
        items.clear();
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public void setIcon(Material icon) {
        this.icon = icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public List<String> getDescription() {
        return description;
    }
    
    public void setDescription(List<String> description) {
        this.description = description != null ? description : new ArrayList<>();
    }
    
    public int getGuiSlot() {
        return guiSlot;
    }
    
    public void setGuiSlot(int guiSlot) {
        this.guiSlot = Math.max(0, Math.min(53, guiSlot));
    }
    
    public Map<String, MarketItem> getItems() {
        return new HashMap<>(items);
    }
    
    @Override
    public String toString() {
        return "MarketCategory{" +
                "name='" + name + '\'' +
                ", icon=" + icon +
                ", displayName='" + displayName + '\'' +
                ", guiSlot=" + guiSlot +
                ", itemCount=" + items.size() +
                '}';
    }
}