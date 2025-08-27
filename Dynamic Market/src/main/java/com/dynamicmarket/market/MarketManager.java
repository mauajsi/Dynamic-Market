package com.dynamicmarket.market;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketCategory;
import com.dynamicmarket.data.MarketItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;

public class MarketManager {
    
    private final DynamicMarket plugin;
    private final Map<String, MarketCategory> categories;
    
    public MarketManager(DynamicMarket plugin) {
        this.plugin = plugin;
        this.categories = new HashMap<>();
        loadMarketData();
    }
    
    public void loadMarketData() {
        categories.clear();
        List<MarketCategory> loadedCategories = plugin.getDataManager().getDatabaseManager().loadCategories();
        List<MarketItem> loadedItems = plugin.getDataManager().getDatabaseManager().loadItems();
        
        if (loadedCategories.isEmpty()) {
            createDefaultCategories();
            saveMarketData();
            return;
        }
        
        for (MarketCategory category : loadedCategories) {
            categories.put(category.getName().toLowerCase(), category);
        }
        
        for (MarketItem item : loadedItems) {
            MarketCategory category = categories.get(item.getCategory().toLowerCase());
            if (category != null) {
                category.addItem(item);
            }
        }
        
        plugin.getLogger().info("Loaded " + categories.size() + " market categories.");
    }
    
    public void saveMarketData() {
        for (MarketCategory category : categories.values()) {
            plugin.getDataManager().getDatabaseManager().saveCategory(category);
            
            for (MarketItem item : category.getAllItems()) {
                plugin.getDataManager().getDatabaseManager().saveItem(item);
            }
        }
    }
    
    private void createDefaultCategories() {
        // Create default categories
        MarketCategory blocks = new MarketCategory("blocks", Material.STONE, "§6Blocks", 
                Arrays.asList("§7Building materials", "§7and decorative blocks"), 10);
        
        MarketCategory tools = new MarketCategory("tools", Material.DIAMOND_PICKAXE, "§6Tools & Weapons", 
                Arrays.asList("§7Tools, weapons and", "§7useful equipment"), 12);
        
        MarketCategory food = new MarketCategory("food", Material.BREAD, "§6Food & Farming", 
                Arrays.asList("§7Food items and", "§7farming supplies"), 14);
        
        MarketCategory redstone = new MarketCategory("redstone", Material.REDSTONE, "§6Redstone", 
                Arrays.asList("§7Redstone components", "§7and mechanisms"), 16);
        
        // Add some default items
        blocks.addItem(new MarketItem("stone", Material.STONE, "§fStone", null, 1.0, 0.5, 1000, "blocks"));
        blocks.addItem(new MarketItem("cobblestone", Material.COBBLESTONE, "§fCobblestone", null, 0.8, 0.4, 1000, "blocks"));
        blocks.addItem(new MarketItem("oak_planks", Material.OAK_PLANKS, "§fOak Planks", null, 2.0, 1.0, 500, "blocks"));
        blocks.addItem(new MarketItem("dirt", Material.DIRT, "§fDirt", null, 0.5, 0.2, 2000, "blocks"));
        blocks.addItem(new MarketItem("glass", Material.GLASS, "§fGlass", null, 3.0, 1.5, 300, "blocks"));
        blocks.addItem(new MarketItem("cobblestone_wall", Material.COBBLESTONE_WALL, "§fCobblestone Wall", null, 1.5, 0.7, 400, "blocks"));
        blocks.addItem(new MarketItem("brick", Material.BRICK, "§fBrick", null, 2.5, 1.2, 350, "blocks"));
        blocks.addItem(new MarketItem("sand", Material.SAND, "§fSand", null, 0.7, 0.3, 1000, "blocks"));
        blocks.addItem(new MarketItem("gravel", Material.GRAVEL, "§fGravel", null, 0.6, 0.25, 1000, "blocks"));
        
        tools.addItem(new MarketItem("iron_pickaxe", Material.IRON_PICKAXE, "§fIron Pickaxe", null, 50.0, 25.0, 10, "tools"));
        tools.addItem(new MarketItem("wooden_pickaxe", Material.WOODEN_PICKAXE, "§fWooden Pickaxe", null, 10.0, 5.0, 50, "tools"));
        tools.addItem(new MarketItem("stone_sword", Material.STONE_SWORD, "§fStone Sword", null, 15.0, 7.5, 30, "tools"));
        tools.addItem(new MarketItem("diamond_pickaxe", Material.DIAMOND_PICKAXE, "§fDiamond Pickaxe", null, 200.0, 100.0, 5, "tools"));
        tools.addItem(new MarketItem("golden_shovel", Material.GOLDEN_SHOVEL, "§fGolden Shovel", null, 12.0, 6.0, 20, "tools"));
        tools.addItem(new MarketItem("fishing_rod", Material.FISHING_ROD, "§fFishing Rod", null, 20.0, 10.0, 15, "tools"));
        tools.addItem(new MarketItem("bow", Material.BOW, "§fBow", null, 30.0, 15.0, 10, "tools"));
        tools.addItem(new MarketItem("iron_sword", Material.IRON_SWORD, "§fIron Sword", null, 45.0, 22.5, 10, "tools"));
        
        food.addItem(new MarketItem("bread", Material.BREAD, "§fBread", null, 5.0, 2.5, 100, "food"));
        food.addItem(new MarketItem("wheat", Material.WHEAT, "§fWheat", null, 2.0, 1.0, 200, "food"));
        food.addItem(new MarketItem("cooked_beef", Material.COOKED_BEEF, "§fCooked Beef", null, 8.0, 4.0, 50, "food"));
        food.addItem(new MarketItem("apple", Material.APPLE, "§fApple", null, 4.0, 2.0, 150, "food"));
        food.addItem(new MarketItem("golden_apple", Material.GOLDEN_APPLE, "§fGolden Apple", null, 100.0, 50.0, 5, "food"));
        food.addItem(new MarketItem("cooked_chicken", Material.COOKED_CHICKEN, "§fCooked Chicken", null, 6.0, 3.0, 80, "food"));
        food.addItem(new MarketItem("cooked_porkchop", Material.COOKED_PORKCHOP, "§fCooked Porkchop", null, 7.0, 3.5, 70, "food"));
        food.addItem(new MarketItem("mushroom_stew", Material.MUSHROOM_STEW, "§fMushroom Stew", null, 10.0, 5.0, 40, "food"));
        
        redstone.addItem(new MarketItem("redstone", Material.REDSTONE, "§fRedstone Dust", null, 3.0, 1.5, 100, "redstone"));
        redstone.addItem(new MarketItem("piston", Material.PISTON, "§fPiston", null, 20.0, 10.0, 25, "redstone"));
        redstone.addItem(new MarketItem("lever", Material.LEVER, "§fLever", null, 5.0, 2.5, 75, "redstone"));
        redstone.addItem(new MarketItem("redstone_torch", Material.REDSTONE_TORCH, "§fRedstone Torch", null, 2.0, 1.0, 200, "redstone"));
        redstone.addItem(new MarketItem("button", Material.STONE_BUTTON, "§fStone Button", null, 3.0, 1.5, 150, "redstone"));
        redstone.addItem(new MarketItem("redstone_lamp", Material.REDSTONE_LAMP, "§fRedstone Lamp", null, 18.0, 9.0, 20, "redstone"));
        redstone.addItem(new MarketItem("daylight_detector", Material.DAYLIGHT_DETECTOR, "§fDaylight Detector", null, 25.0, 12.5, 10, "redstone"));
        redstone.addItem(new MarketItem("repeater", Material.REPEATER, "§fRepeater", null, 15.0, 7.5, 20, "redstone"));

        // New category: Farming
        MarketCategory farming = new MarketCategory("farming", Material.WHEAT_SEEDS, "§6Farming",
                Arrays.asList("§7Seeds, crops and", "§7farming tools"), 20);

        farming.addItem(new MarketItem("wheat_seeds", Material.WHEAT_SEEDS, "§fWheat Seeds", null, 1.0, 0.5, 500, "farming"));
        farming.addItem(new MarketItem("carrot", Material.CARROT, "§fCarrot", null, 3.0, 1.5, 200, "farming"));
        farming.addItem(new MarketItem("potato", Material.POTATO, "§fPotato", null, 3.0, 1.5, 200, "farming"));
        farming.addItem(new MarketItem("sugar_cane", Material.SUGAR_CANE, "§fSugar Cane", null, 2.0, 1.0, 300, "farming"));
        farming.addItem(new MarketItem("bone_meal", Material.BONE_MEAL, "§fBone Meal", null, 4.0, 2.0, 100, "farming"));
        farming.addItem(new MarketItem("pumpkin", Material.PUMPKIN, "§fPumpkin", null, 5.0, 2.5, 80, "farming"));
        farming.addItem(new MarketItem("melon_slice", Material.MELON_SLICE, "§fMelon Slice", null, 2.0, 1.0, 250, "farming"));

        // New category: Decorations
        MarketCategory decorations = new MarketCategory("decorations", Material.FLOWER_POT, "§6Decorations",
                Arrays.asList("§7Decorative blocks and", "§7items for building"), 22);



        decorations.addItem(new MarketItem("flower_pot", Material.FLOWER_POT, "§fFlower Pot", null, 5.0, 2.5, 50, "decorations"));
        decorations.addItem(new MarketItem("painting", Material.PAINTING, "§fPainting", null, 10.0, 5.0, 30, "decorations"));
        decorations.addItem(new MarketItem("item_frame", Material.ITEM_FRAME, "§fItem Frame", null, 7.0, 3.5, 40, "decorations"));
        decorations.addItem(new MarketItem("torch", Material.TORCH, "§fTorch", null, 1.0, 0.5, 500, "decorations"));
        decorations.addItem(new MarketItem("crafting_table", Material.CRAFTING_TABLE, "§fCrafting Table", null, 8.0, 4.0, 60, "decorations"));
        decorations.addItem(new MarketItem("furnace", Material.FURNACE, "§fFurnace", null, 12.0, 6.0, 50, "decorations"));


        
        categories.put("blocks", blocks);
        categories.put("tools", tools);
        categories.put("food", food);
        categories.put("redstone", redstone);
        categories.put("farming", farming);
        categories.put("decorations", decorations);
    }
    
    // Category management methods
    public void addCategory(String name, Material icon, String displayName, List<String> description, int guiSlot) {
        MarketCategory category = new MarketCategory(name, icon, displayName, description, guiSlot);
        categories.put(name, category);
    }
    
    public void removeCategory(String name) {
        categories.remove(name);
    }
    
    public MarketCategory getCategory(String name) {
        return categories.get(name);
    }
    
    public boolean hasCategory(String name) {
        return categories.containsKey(name);
    }
    
    public List<MarketCategory> getAllCategories() {
        return new ArrayList<>(categories.values());
    }
    
    public List<MarketCategory> getSortedCategories() {
        List<MarketCategory> sorted = new ArrayList<>(categories.values());
        sorted.sort(Comparator.comparingInt(MarketCategory::getGuiSlot));
        return sorted;
    }
    
    // Item management methods
    public void addItemToCategory(String categoryName, MarketItem item) {
        MarketCategory category = categories.get(categoryName);
        if (category != null) {
            category.addItem(item);
        }
    }
    
    public void removeItemFromCategory(String categoryName, String itemId) {
        MarketCategory category = categories.get(categoryName);
        if (category != null) {
            category.removeItem(itemId);
        }
    }
    
    public MarketItem findItem(String categoryName, String itemId) {
        MarketCategory category = categories.get(categoryName);
        return category != null ? category.getItem(itemId) : null;
    }
    
    public MarketItem findItemByMaterial(Material material) {
        for (MarketCategory category : categories.values()) {
            MarketItem item = category.getItemByMaterial(material);
            if (item != null) {
                return item;
            }
        }
        return null;
    }
    
    public void moveItem(String fromCategory, String toCategory, String itemId) {
        MarketCategory from = categories.get(fromCategory);
        MarketCategory to = categories.get(toCategory);
        
        if (from != null && to != null && from.hasItem(itemId)) {
            MarketItem item = from.getItem(itemId);
            from.removeItem(itemId);
            to.addItem(item);
        }
    }
    
    public void resetMarket() {
        categories.clear();
        createDefaultCategories();
        saveMarketData();
    }
    
    public Map<String, MarketCategory> getCategories() {
        return new HashMap<>(categories);
    }

    public boolean categoryExists(String categoryName) {
        return categories.containsKey(categoryName);
    }

    public void createBackup() {
        try {
            // Save current state to database
            saveMarketData();
            plugin.getLogger().info("Market data backup created successfully!");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create market data backup", e);
        }
    }

    public void restoreFromBackup(String backupId) {
        try {
            // Restore from database and reload
            categories.clear();
            loadMarketData();
            plugin.getLogger().info("Market data restored successfully!");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not restore from backup: " + backupId, e);
            throw new RuntimeException("Failed to restore from backup", e);
        }
    }
}