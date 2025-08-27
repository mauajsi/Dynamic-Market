package com.dynamicmarket.commands;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketCategory;
import com.dynamicmarket.data.MarketItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class MarketAdminCommand implements CommandExecutor, TabCompleter {
    private final DynamicMarket plugin;
    
    public MarketAdminCommand(DynamicMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            // Suggest subcommands
            List<String> subcommands = Arrays.asList(
                "addcategory", "removecategory", "add", "remove", "setprice", "move",
                "reset", "info", "list", "reload", "backup", "restore",
                "stock", "addstock", "removestock"
            );
            for (String sub : subcommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length > 1) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "add":
                case "remove":
                case "setprice":
                case "move":
                case "info":
                case "stock":
                case "addstock":
                case "removestock":
                    if (args.length == 2) {
                        // Suggest categories
                        plugin.getMarketManager().getAllCategories().forEach(category -> {
                            if (category.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(category.getName());
                            }
                        });
                    } else if (args.length == 3) {
                        // Suggest items within the selected category
                        String categoryName = args[1];
                        MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
                        if (category != null) {
                             category.getItems().forEach((id, marketItem) -> {
                                     if (marketItem.getMaterial().name().toLowerCase().startsWith(args[2].toLowerCase())) {
                                         completions.add(marketItem.getMaterial().name());
                                     }
                                 }
                             );
                         }
                    }
                    break;
            }
        }
        return completions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check basic admin permission
        if (!sender.hasPermission("market.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
            case "?":

                sendHelpMessage(sender);
                break;
                
            case "addcategory":
                handleAddCategory(sender, args);
                break;
                
            case "removecategory":
            case "delcategory":
                handleRemoveCategory(sender, args);
                break;
                
            case "add":
                handleAddItem(sender, args);
                break;
                
            case "remove":
            case "del":
                handleRemoveItem(sender, args);
                break;
                
            case "setprice":
                handleSetPrice(sender, args);
                break;
                
            case "move":
                handleMoveItem(sender, args);
                break;
                
            case "reset":
                handleReset(sender, args);
                break;
                
            case "info":
            case "stats":
                handleInfo(sender, args);
                break;
                
            case "list":
                handleList(sender, args);
                break;
                
            case "reload":
                handleReload(sender);
                break;
                
            case "backup":
                handleBackup(sender);
                break;
                
            case "restore":
                handleRestore(sender, args);
                break;

            case "stock":
                handleSetStock(sender, args);
                break;

            case "addstock":
                handleAddStock(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
                break;

            case "removestock":
                handleRemoveStock(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
                break;
                
            default:
                sender.sendMessage("§cUnknown subcommand: " + subCommand);
                sender.sendMessage("§7Use §e/marketadmin help §7for available commands.");
                break;
        }
        
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§c§l=== MARKET ADMIN HELP ===");
        sender.sendMessage("§e/marketadmin addcategory <name> <icon> §7- Create new category");
        sender.sendMessage("§e/marketadmin removecategory <name> §7- Remove category");
        sender.sendMessage("§e/marketadmin add <category> <price> §7- Add held item to market");
        sender.sendMessage("§e/marketadmin remove <category> <item> §7- Remove item from market");
        sender.sendMessage("§e/marketadmin setprice <category> <item> <buy> [sell] §7- Set prices");
        sender.sendMessage("§e/marketadmin move <from> <to> <item> §7- Move item between categories");
        sender.sendMessage("§e/marketadmin reset §7- Reset market to defaults");
        sender.sendMessage("§e/marketadmin info [category] §7- Show market/category info");
        sender.sendMessage("§e/marketadmin list [category] §7- List categories/items");
        sender.sendMessage("§e/marketadmin reload §7- Reload market data");
        sender.sendMessage("§e/marketadmin backup §7- Create market backup");
        sender.sendMessage("§e/marketadmin restore <file> §7- Restore from backup");
        sender.sendMessage("§e/marketadmin stock <category> <item> <amount> §7- Set item stock");
        sender.sendMessage("§7Example: /marketadmin stock Tools DIAMOND_PICKAXE 100");
        sender.sendMessage("§e/marketadmin addstock <category> <item> <amount> §7- Increase item stock");
        sender.sendMessage("§7Example: /marketadmin addstock Tools DIAMOND_PICKAXE 50");
        sender.sendMessage("§e/marketadmin removestock <category> <item> <amount> §7- Decrease item stock");
        sender.sendMessage("§7Example: /marketadmin removestock Tools DIAMOND_PICKAXE 20");
        sender.sendMessage("§c§l=========================");
        sender.sendMessage("");
    }
    
    private void handleAddCategory(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.category")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /marketadmin addcategory <name> <icon>");
            sender.sendMessage("§7Example: /marketadmin addcategory Tools DIAMOND_PICKAXE");
            return;
        }
        
        String categoryName = args[1];
        String iconName = args[2].toUpperCase();
        
        // Check if category already exists
        if (plugin.getMarketManager().categoryExists(categoryName)) {
            sender.sendMessage("§cCategory '" + categoryName + "' already exists!");
            return;
        }
        
        // Validate icon material
        Material iconMaterial;
        try {
            iconMaterial = Material.valueOf(iconName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid icon material: " + iconName);
            return;
        }
        
        // Create category
        String displayName = categoryName;
        if (args.length > 3) {
            displayName = String.join(" ", Arrays.copyOfRange(args, 3, args.length)).replace("&", "§");
        }
        
        // Add category to market
        plugin.getMarketManager().addCategory(categoryName, iconMaterial, displayName, Arrays.asList(), 0);
        plugin.getMarketManager().saveMarketData();
        
        sender.sendMessage("§a§lCategory created successfully!");
        sender.sendMessage("§7▪ §eName: §f" + categoryName);
        sender.sendMessage("§7▪ §eIcon: §f" + iconMaterial.name());
        sender.sendMessage("§7Use §e/setslot " + categoryName + " <slot> §7to set GUI position.");
        
        plugin.getLogger().info("Category '" + categoryName + "' created by " + sender.getName());
    }
    
    private void handleRemoveCategory(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.category")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /marketadmin removecategory <name>");
            return;
        }
        
        String categoryName = args[1];
        
        if (!plugin.getMarketManager().categoryExists(categoryName)) {
            sender.sendMessage("§cCategory '" + categoryName + "' does not exist!");
            return;
        }
        
        MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
        int itemCount = category.getItems().size();
        
        // Confirm deletion if category has items
        if (itemCount > 0) {
            sender.sendMessage("§e§lWARNING: This category contains " + itemCount + " items!");
            sender.sendMessage("§7All items will be permanently deleted.");
            sender.sendMessage("§7Use §c/marketadmin removecategory " + categoryName + " confirm §7to proceed.");
            
            if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                return;
            }
        }
        
        // Remove category
        plugin.getMarketManager().removeCategory(categoryName);
        plugin.getMarketManager().saveMarketData();
        
        sender.sendMessage("§a§lCategory removed successfully!");
        sender.sendMessage("§7▪ §eCategory: §f" + categoryName);
        sender.sendMessage("§7▪ §eItems deleted: §f" + itemCount);
        
        plugin.getLogger().info("Category '" + categoryName + "' removed by " + sender.getName());
    }
    
    private void handleAddItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.add")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /marketadmin add <category> <price>");
            sender.sendMessage("§7Hold the item you want to add in your hand.");
            return;
        }
        
        Player player = (Player) sender;
        ItemStack handItem = player.getInventory().getItemInMainHand();
        
        if (handItem == null || handItem.getType() == Material.AIR) {
            sender.sendMessage("§cYou must be holding an item to add it to the market!");
            return;
        }
        
        String categoryName = args[1];
        if (!plugin.getMarketManager().categoryExists(categoryName)) {
            sender.sendMessage("§cCategory '" + categoryName + "' does not exist!");
            return;
        }
        
        double buyPrice;
        try {
            buyPrice = Double.parseDouble(args[2]);
            if (buyPrice <= 0) {
                sender.sendMessage("§cPrice must be greater than 0!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid price: " + args[2]);
            return;
        }
        
        // Check if item already exists in market
        MarketItem existingItem = plugin.getMarketManager().findItemByMaterial(handItem.getType());
        if (existingItem != null) {
            sender.sendMessage("§cThis item already exists in the market!");
            sender.sendMessage("§7Use §e/marketadmin setprice §7to modify prices instead.");
            return;
        }
        
        // Create market item
        String itemId = categoryName.toLowerCase() + "_" + handItem.getType().name().toLowerCase();
        double sellPrice = buyPrice * 0.7; // Default sell price is 70% of buy price
        int defaultStock = plugin.getConfigManager().getConfig().getInt("default-stock", 100);
        
        MarketItem marketItem = new MarketItem(
            itemId,
            handItem.getType(),
            handItem.getItemMeta() != null ? handItem.getItemMeta().getDisplayName() : null,
            handItem.getItemMeta() != null ? handItem.getItemMeta().getLore() : null,
            buyPrice,
            sellPrice,
            defaultStock,
            categoryName
        );
        
        // Add item to category
        plugin.getMarketManager().addItemToCategory(categoryName, marketItem);
        plugin.getMarketManager().saveMarketData();
        
        String itemName = marketItem.getDisplayName() != null ? 
            marketItem.getDisplayName() : 
            handItem.getType().name().toLowerCase().replace("_", " ");
        
        sender.sendMessage("§a§lItem added to market successfully!");
        sender.sendMessage("§7▪ §eItem: §f" + itemName);
        sender.sendMessage("§7▪ §eCategory: §f" + categoryName);
        sender.sendMessage("§7▪ §eBuy Price: §a$" + String.format("%.2f", buyPrice));
        sender.sendMessage("§7▪ §eSell Price: §a$" + String.format("%.2f", sellPrice));
        sender.sendMessage("§7▪ §eStock: §f" + defaultStock);
        
        plugin.getLogger().info("Item '" + itemName + "' added to category '" + categoryName + "' by " + sender.getName());
    }

    private void handleRemoveItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.remove")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /marketadmin remove <category> <item>");
            sender.sendMessage("§7Example: /marketadmin remove Tools DIAMOND_PICKAXE");
            return;
        }
        
        String categoryName = args[1];
        String itemName = args[2].toUpperCase();
        
        if (!plugin.getMarketManager().categoryExists(categoryName)) {
            sender.sendMessage("§cCategory '" + categoryName + "' does not exist!");
            return;
        }
        
        Material material;
        try {
            material = Material.valueOf(itemName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid item: " + itemName);
            return;
        }
        
        MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
        MarketItem item = null;
        
        // Find item in category
        for (MarketItem marketItem : category.getItems().values()) {
            if (marketItem.getMaterial() == material) {
                item = marketItem;
                break;
            }
        }
        
        if (item == null) {
            sender.sendMessage("§cItem '" + itemName + "' not found in category '" + categoryName + "'!");
            return;
        }
        
        // Remove item
        plugin.getMarketManager().removeItemFromCategory(categoryName, item.getId());
        plugin.getMarketManager().saveMarketData();
        
        String displayName = item.getDisplayName() != null ? 
            item.getDisplayName() : 
            material.name().toLowerCase().replace("_", " ");
        
        sender.sendMessage("§a§lItem removed from market successfully!");
        sender.sendMessage("§7▪ §eItem: §f" + displayName);
        sender.sendMessage("§7▪ §eCategory: §f" + categoryName);
        
        plugin.getLogger().info("Item '" + displayName + "' removed from category '" + categoryName + "' by " + sender.getName());
    }
    
    private void handleSetPrice(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.setprice")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /marketadmin setprice <category> <item> <buy_price> [sell_price]");
            return;
        }
        
        String categoryName = args[1];
        String itemName = args[2].toUpperCase();
        
        if (!plugin.getMarketManager().categoryExists(categoryName)) {
            sender.sendMessage("§cCategory '" + categoryName + "' does not exist!");
            return;
        }
        
        Material material;
        try {
            material = Material.valueOf(itemName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid item: " + itemName);
            return;
        }
        
        double buyPrice;
        try {
            buyPrice = Double.parseDouble(args[3]);
            if (buyPrice <= 0) {
                sender.sendMessage("§cBuy price must be greater than 0!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid buy price: " + args[3]);
            return;
        }
        
        double sellPrice = buyPrice * 0.7; // Default to 70% of buy price
        if (args.length > 4) {
            try {
                sellPrice = Double.parseDouble(args[4]);
                if (sellPrice < 0) {
                    sender.sendMessage("§cSell price cannot be negative!");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid sell price: " + args[4]);
                return;
            }
        }
        
        // Find and update item
        MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
        MarketItem item = null;
        
        for (MarketItem marketItem : category.getItems().values()) {
            if (marketItem.getMaterial() == material) {
                item = marketItem;
                break;
            }
        }
        
        if (item == null) {
            sender.sendMessage("§cItem '" + itemName + "' not found in category '" + categoryName + "'!");
            return;
        }
        
        double oldBuyPrice = item.getBuyPrice();
        double oldSellPrice = item.getSellPrice();
        
        item.setBuyPrice(buyPrice);
        item.setSellPrice(sellPrice);
        
        plugin.getMarketManager().saveMarketData();
        
        String displayName = item.getDisplayName() != null ? 
            item.getDisplayName() : 
            material.name().toLowerCase().replace("_", " ");
        
        sender.sendMessage("§a§lItem prices updated successfully!");
        sender.sendMessage("§7▪ §eItem: §f" + displayName);
        sender.sendMessage("§7▪ §eBuy Price: §c$" + String.format("%.2f", oldBuyPrice) + " §7→ §a$" + String.format("%.2f", buyPrice));
        sender.sendMessage("§7▪ §eSell Price: §c$" + String.format("%.2f", oldSellPrice) + " §7→ §a$" + String.format("%.2f", sellPrice));
        
        plugin.getLogger().info("Prices for '" + displayName + "' updated by " + sender.getName());
    }
    
    private void handleMoveItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.move")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /marketadmin move <from_category> <to_category> <item>");
            return;
        }
        
        String fromCategory = args[1];
        String toCategory = args[2];
        String itemName = args[3].toUpperCase();
        
        if (!plugin.getMarketManager().categoryExists(fromCategory)) {
            sender.sendMessage("§cSource category '" + fromCategory + "' does not exist!");
            return;
        }
        
        if (!plugin.getMarketManager().categoryExists(toCategory)) {
            sender.sendMessage("§cDestination category '" + toCategory + "' does not exist!");
            return;
        }
        
        Material material;
        try {
            material = Material.valueOf(itemName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid item: " + itemName);
            return;
        }
        
        // Move item
        MarketCategory sourceCategory = plugin.getMarketManager().getCategory(fromCategory);
        MarketItem itemToMove = null;
        
        // Find item in source category
        for (MarketItem item : sourceCategory.getItems().values()) {
            if (item.getMaterial() == material) {
                itemToMove = item;
                break;
            }
        }
        
        if (itemToMove == null) {
            sender.sendMessage("§cItem not found in source category!");
            return;
        }
        
        plugin.getMarketManager().moveItem(fromCategory, toCategory, itemToMove.getId());
        plugin.getMarketManager().saveMarketData();
        
        sender.sendMessage("§a§lItem moved successfully!");
        sender.sendMessage("§7▪ §eItem: §f" + material.name().toLowerCase().replace("_", " "));
        sender.sendMessage("§7▪ §eFrom: §f" + fromCategory);
        sender.sendMessage("§7▪ §eTo: §f" + toCategory);
        
        plugin.getLogger().info("Item '" + material.name() + "' moved from '" + fromCategory + "' to '" + toCategory + "' by " + sender.getName());
    }
    
    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.reset")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        sender.sendMessage("§c§l⚠ WARNING: This will reset the entire market to default configuration!");
        sender.sendMessage("§7All custom categories, items, and prices will be lost.");
        sender.sendMessage("§7Use §c/marketadmin reset confirm §7to proceed.");
        
        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            return;
        }
        
        // Reset market
        plugin.getMarketManager().resetMarket();
        plugin.getMarketManager().saveMarketData();
        
        sender.sendMessage("§a§lMarket reset to defaults successfully!");
        sender.sendMessage("§7All data has been restored to the original configuration.");
        
        plugin.getLogger().warning("Market reset to defaults by " + sender.getName());
    }
    
    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            // Show general market info
            showGeneralMarketInfo(sender);
        } else {
            // Show specific category info
            showCategoryInfo(sender, args[1]);
        }
    }
    
    private void showGeneralMarketInfo(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§c§l=== MARKET ADMIN INFO ===");
        
        int totalCategories = plugin.getMarketManager().getAllCategories().size();
        int totalItems = 0;
        for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            totalItems += category.getItems().size();
        }
        
        sender.sendMessage("§7▪ §eTotal Categories: §f" + totalCategories);
        sender.sendMessage("§7▪ §eTotal Items: §f" + totalItems);
        
        // Show data manager stats
        if (plugin.getDataManager() != null) {
            sender.sendMessage("§7▪ §eData Manager: §aActive");
        } else {
            sender.sendMessage("§7▪ §eData Manager: §cInactive");
        }
        
        // Show categories
        sender.sendMessage("");
        sender.sendMessage("§7Categories:");
        for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            int itemCount = category.getItems().size();
            sender.sendMessage("§7▪ §e" + category.getName() + " §7(" + itemCount + " items, slot " + category.getGuiSlot() + ")");
        }
        
        sender.sendMessage("§c§l========================");
        sender.sendMessage("");
    }
    
    private void showCategoryInfo(CommandSender sender, String categoryName) {
        if (!plugin.getMarketManager().categoryExists(categoryName)) {
            sender.sendMessage("§cCategory '" + categoryName + "' does not exist!");
            return;
        }
        
        MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
        
        sender.sendMessage("");
        sender.sendMessage("§c§l=== CATEGORY INFO: " + categoryName.toUpperCase() + " ===");
        sender.sendMessage("§7▪ §eDisplay Name: §f" + (category.getDisplayName() != null ? category.getDisplayName() : categoryName));
        sender.sendMessage("§7▪ §eIcon: §f" + category.getIcon().name());
        sender.sendMessage("§7▪ §eGUI Slot: §f" + category.getGuiSlot());
        sender.sendMessage("§7▪ §eItem Count: §f" + category.getItems().size());
        
        if (!category.getItems().isEmpty()) {
            sender.sendMessage("");
            sender.sendMessage("§7Items:");
            for (MarketItem item : category.getItems().values()) {
                String itemName = item.getDisplayName() != null ? 
                    item.getDisplayName() : 
                    item.getMaterial().name().toLowerCase().replace("_", " ");
                
                sender.sendMessage("§7▪ §e" + itemName + " §7(Buy: $" + String.format("%.2f", item.getBuyPrice()) + 
                                 ", Sell: $" + String.format("%.2f", item.getSellPrice()) + 
                                 ", Stock: " + item.getStock() + ")");
            }
        }
        
        sender.sendMessage("§c§l" + "=".repeat(20 + categoryName.length()));
        sender.sendMessage("");
    }
    
    private void handleList(CommandSender sender, String[] args) {
        if (args.length < 2) {
            // List all categories
            sender.sendMessage("");
            sender.sendMessage("§c§l=== ALL CATEGORIES ===");
            
            if (plugin.getMarketManager().getAllCategories().isEmpty()) {
                sender.sendMessage("§7No categories found.");
            } else {
                for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
                    int itemCount = category.getItems().size();
                    sender.sendMessage("§7▪ §e" + category.getName() + " §7(" + itemCount + " items)");
                }
            }
            
            sender.sendMessage("§c§l==================");
            sender.sendMessage("");
        } else {
            // List items in specific category
            String categoryName = args[1];
            
            if (!plugin.getMarketManager().categoryExists(categoryName)) {
                sender.sendMessage("§cCategory '" + categoryName + "' does not exist!");
                return;
            }
            
            MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
            
            sender.sendMessage("");
            sender.sendMessage("§c§l=== ITEMS IN " + categoryName.toUpperCase() + " ===");
            
            if (category.getItems().isEmpty()) {
                sender.sendMessage("§7No items found in this category.");
            } else {
                for (MarketItem item : category.getItems().values()) {
                    String itemName = item.getDisplayName() != null ? 
                        item.getDisplayName() : 
                        item.getMaterial().name().toLowerCase().replace("_", " ");
                    
                    sender.sendMessage("§7▪ §e" + itemName);
                    sender.sendMessage("  §7Buy: §a$" + String.format("%.2f", item.getBuyPrice()) + 
                                     " §7| Sell: §a$" + String.format("%.2f", item.getSellPrice()) + 
                                     " §7| Stock: §f" + item.getStock());
                }
            }
            
            sender.sendMessage("§c§l" + "=".repeat(15 + categoryName.length()));
            sender.sendMessage("");
        }
    }
    
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("market.admin.reload")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        try {
            plugin.getConfigManager().loadConfig();
            plugin.getMarketManager().loadMarketData();
            
            sender.sendMessage("§a§lMarket data reloaded successfully!");
            plugin.getLogger().info("Market data reloaded by " + sender.getName());
            
        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload market data! Check console for errors.");
            plugin.getLogger().severe("Failed to reload market data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleBackup(CommandSender sender) {
        if (!sender.hasPermission("market.admin.backup")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        try {
            plugin.getMarketManager().createBackup();
            sender.sendMessage("§a§lBackup created successfully!");
            sender.sendMessage("§7▪ §eFile: §fmarket-data backup created in backups folder");
            
            plugin.getLogger().info("Market backup created by " + sender.getName());
            
        } catch (Exception e) {
            sender.sendMessage("§cFailed to create backup! Check console for errors.");
            plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleSetStock(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.stock")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /marketadmin stock <category> <item> <amount>");
            return;
        }

        String categoryName = args[1];
        String itemName = args[2];
        int amount;

        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", args[2]));
            return;
        }

        if (amount < 0) {
            sender.sendMessage("§cAmount cannot be negative.");
            return;
        }

        MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
        if (category == null) {
            sender.sendMessage("§cCategory '" + categoryName + "' not found.");
            return;
        }

        MarketItem item = category.getItem(itemName);
        if (item == null) {
            sender.sendMessage("§cItem '" + itemName + "' not found in category '" + categoryName + "'.");
            return;
        }

        item.setStock(amount);
        plugin.getMarketManager().saveMarketData();
        sender.sendMessage("§aStock for §e" + itemName + "§a in category §e" + categoryName + "§a set to §e" + amount + "§a.");
    }

    private void handleAddStock(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.addstock")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /marketadmin addstock <category> <item> <amount>");
            return;
        }

        String categoryName = args[0];
        String itemName = args[1].toLowerCase();
        int amountToAdd;

        try {
            amountToAdd = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", args[2]));
            return;
        }

        if (amountToAdd <= 0) {
            sender.sendMessage("§cAmount to add must be positive.");
            return;
        }

        MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
        if (category == null) {
            sender.sendMessage("§cCategory '" + categoryName + "' not found.");
            return;
        }

        MarketItem item = category.getItem(itemName);
        if (item == null) {
            sender.sendMessage("§cItem '" + itemName + "' not found in category '" + categoryName + "'.");
            return;
        }

        item.setStock(item.getStock() + amountToAdd);
        plugin.getMarketManager().saveMarketData();
        sender.sendMessage("§aAdded §e" + amountToAdd + "§a stock to §e" + itemName + "§a in category §e" + categoryName + "§a. New stock: §e" + item.getStock() + "§a.");
    }

    private void handleRemoveStock(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.removestock")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /marketadmin removestock <category> <item> <amount>");
            return;
        }

        String categoryName = args[0];
        String itemName = args[1].toLowerCase();
        int amountToRemove;

        try {
            amountToRemove = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "number", args[2]));
            return;
        }

        if (amountToRemove <= 0) {
            sender.sendMessage("§cAmount to remove must be positive.");
            return;
        }

        MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
        if (category == null) {
            sender.sendMessage("§cCategory '" + categoryName + "' not found.");
            return;
        }

        MarketItem item = category.getItem(itemName);
        if (item == null) {
            sender.sendMessage("§cItem '" + itemName + "' not found in category '" + categoryName + "'.");
            return;
        }

        int currentStock = item.getStock();
        if (currentStock - amountToRemove < 0) {
            sender.sendMessage("§cCannot remove §e" + amountToRemove + "§c stock. Only §e" + currentStock + "§c available.");
            return;
        }

        item.setStock(currentStock - amountToRemove);
        plugin.getMarketManager().saveMarketData();
        sender.sendMessage("§aRemoved §e" + amountToRemove + "§a stock from §e" + itemName + "§a in category §e" + categoryName + "§a. New stock: §e" + item.getStock() + "§a.");
    }

    private void handleRestore(CommandSender sender, String[] args) {
        if (!sender.hasPermission("market.admin.restore")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /marketadmin restore <backup_file>");
            return;
        }
        
        String backupFile = args[1];
        
        try {
            plugin.getMarketManager().restoreFromBackup(backupFile);
            sender.sendMessage("§a§lMarket restored from backup successfully!");
            sender.sendMessage("§7▪ §eFile: §f" + backupFile);
            
            plugin.getLogger().info("Market restored from backup by " + sender.getName() + ": " + backupFile);
            
        } catch (Exception e) {
            sender.sendMessage("§cFailed to restore from backup! Check console for errors.");
            plugin.getLogger().severe("Failed to restore from backup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}