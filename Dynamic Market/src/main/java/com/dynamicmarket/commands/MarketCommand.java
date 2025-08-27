package com.dynamicmarket.commands;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketCategory;
import com.dynamicmarket.gui.MarketGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MarketCommand implements CommandExecutor {
    
    private final DynamicMarket plugin;
    
    public MarketCommand(DynamicMarket plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check permission
        if (!player.hasPermission("market.use")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }
        
        // Check if Vault economy is available
        if (plugin.getVaultEconomy() == null) {
            player.sendMessage("§cEconomy system is not available! Please contact an administrator.");
            return true;
        }
        
        // Handle different arguments
        if (args.length == 0) {
            // Open main market GUI
            openMarketGUI(player);
            return true;
        }
        
        // Handle subcommands
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
            case "?":
                sendHelpMessage(player);
                break;
                
            case "info":
            case "stats":
                sendMarketInfo(player);
                break;
                
            case "balance":
            case "bal":
                sendBalanceInfo(player);
                break;
                
            case "search":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /market search <item_name>");
                    return true;
                }
                searchItems(player, args[1]);
                break;
                
            case "category":
            case "cat":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /market category <category_name>");
                    return true;
                }
                openCategoryDirectly(player, args[1]);
                break;
                
            case "reload":
                if (!player.hasPermission("market.admin.reload")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                reloadMarket(player);
                break;
                
            default:
                // Try to open category with this name
                if (plugin.getMarketManager().categoryExists(subCommand)) {
                    openCategoryDirectly(player, subCommand);
                } else {
                    player.sendMessage("§cUnknown subcommand: " + subCommand);
                    player.sendMessage("§7Use §e/market help §7for available commands.");
                }
                break;
        }
        
        return true;
    }
    
    private void openMarketGUI(Player player) {
        try {
            MarketGUI marketGUI = new MarketGUI(plugin, player);
            marketGUI.open();
            
            // Send opening message
            player.sendMessage(plugin.getConfigManager().getMessage("market.opening"));
            
        } catch (Exception e) {
            player.sendMessage("§cFailed to open market! Please try again or contact an administrator.");
            plugin.getLogger().severe("Failed to open market GUI for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l=== MARKET HELP ===");
        player.sendMessage("§e/market §7- Open the main market GUI");
        player.sendMessage("§e/market help §7- Show this help message");
        player.sendMessage("§e/market info §7- Show market statistics");
        player.sendMessage("§e/market balance §7- Show your current balance");
        player.sendMessage("§e/market search <item> §7- Search for items");
        player.sendMessage("§e/market category <name> §7- Open specific category");
        player.sendMessage("");
        player.sendMessage("§7Available categories:");
        
        for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            int itemCount = category.getItems().size();
            player.sendMessage("§7▪ §e" + category.getName() + " §7(" + itemCount + " items)");
        }
        
        player.sendMessage("");
        player.sendMessage("§7§oTip: Prices change based on supply and demand!");
        player.sendMessage("§6§l===================");
        player.sendMessage("");
    }
    
    private void sendMarketInfo(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l=== MARKET STATISTICS ===");
        player.sendMessage("");
        player.sendMessage("§7Market Statistics:");
        
        int totalCategories = plugin.getMarketManager().getAllCategories().size();
        int totalItems = plugin.getMarketManager().getAllCategories().stream()
            .mapToInt(category -> category.getItems().size())
            .sum();
            
        int totalStock = plugin.getMarketManager().getAllCategories().stream()
            .flatMap(category -> category.getItems().values().stream())
            .mapToInt(item -> item.getStock())
            .sum();
            
        player.sendMessage("§7▪ §eTotal Categories: §f" + totalCategories);
        player.sendMessage("§7▪ §eTotal Items: §f" + totalItems);
        player.sendMessage("§7▪ §eTotal Stock: §f" + totalStock);
        
        // Show price adjustment settings
        double adjustmentRate = plugin.getConfigManager().getPriceAdjustmentRate();
        double minPrice = plugin.getConfigManager().getMinPrice();
        double maxPrice = plugin.getConfigManager().getMaxPrice();
        
        player.sendMessage("");
        player.sendMessage("§7▪ §ePrice Adjustment Rate: §f" + (adjustmentRate * 100) + "%");
        player.sendMessage("§7▪ §ePrice Range: §f$" + String.format("%.2f", minPrice) + " - $" + String.format("%.2f", maxPrice));
        
        // Show most expensive and cheapest items
        showPriceExtremes(player);
        
        player.sendMessage("§6§l=========================");
        player.sendMessage("");
    }
    
    private void showPriceExtremes(Player player) {
        double highestPrice = 0.0;
        double lowestPrice = Double.MAX_VALUE;
        String mostExpensiveItem = "None";
        String cheapestItem = "None";
        
        for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            for (var item : category.getItems().values()) {
                if (item.getBuyPrice() > highestPrice) {
                    highestPrice = item.getBuyPrice();
                    mostExpensiveItem = item.getDisplayName() != null ? 
                        item.getDisplayName() : 
                        item.getMaterial().name().toLowerCase().replace("_", " ");
                }
                
                if (item.getBuyPrice() < lowestPrice) {
                    lowestPrice = item.getBuyPrice();
                    cheapestItem = item.getDisplayName() != null ? 
                        item.getDisplayName() : 
                        item.getMaterial().name().toLowerCase().replace("_", " ");
                }
            }
        }
        
        player.sendMessage("");
        if (highestPrice > 0) {
            player.sendMessage("§7▪ §eMost Expensive: §f" + mostExpensiveItem + " §7($" + String.format("%.2f", highestPrice) + ")");
        }
        if (lowestPrice < Double.MAX_VALUE) {
            player.sendMessage("§7▪ §eCheapest: §f" + cheapestItem + " §7($" + String.format("%.2f", lowestPrice) + ")");
        }
    }
    
    private void sendBalanceInfo(Player player) {
        double balance = plugin.getVaultEconomy().getBalance(player);
        
        player.sendMessage("");
        player.sendMessage("§6§l=== YOUR BALANCE ===");
        player.sendMessage("§7▪ §eCurrent Balance: §a$" + String.format("%.2f", balance));
        
        // Show what they can afford
        showAffordableItems(player, balance);
        
        player.sendMessage("§6§l===================");
        player.sendMessage("");
    }
    
    private void showAffordableItems(Player player, double balance) {
        int affordableCount = 0;
        
        for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            for (var item : category.getItems().values()) {
                if (item.getBuyPrice() <= balance && item.getStock() > 0) {
                    affordableCount++;
                }
            }
        }
        
        player.sendMessage("§7▪ §eAffordable Items: §f" + affordableCount);
        
        if (affordableCount == 0) {
            player.sendMessage("§7▪ §cYou cannot afford any items currently.");
            player.sendMessage("§7▪ §7Try selling items with §e/sell §7to earn money!");
        }
    }
    
    private void searchItems(Player player, String searchTerm) {
        player.sendMessage("");
        player.sendMessage("§6§l=== SEARCH RESULTS FOR: " + searchTerm.toUpperCase() + " ===");
        
        boolean found = false;
        
        for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            for (var item : category.getItems().values()) {
                String itemName = item.getDisplayName() != null ? 
                    item.getDisplayName() : 
                    item.getMaterial().name().toLowerCase().replace("_", " ");
                    
                if (itemName.toLowerCase().contains(searchTerm.toLowerCase()) ||
                    item.getMaterial().name().toLowerCase().contains(searchTerm.toLowerCase())) {
                    
                    found = true;
                    String stockStatus = item.getStock() > 0 ? "§a" + item.getStock() + " in stock" : "§cOut of stock";
                    
                    player.sendMessage("§7▪ §e" + itemName + " §7(" + category.getName() + ")");
                    player.sendMessage("  §7Buy: §a$" + String.format("%.2f", item.getBuyPrice()) + 
                                     " §7| Sell: §a$" + String.format("%.2f", item.getSellPrice()) + 
                                     " §7| " + stockStatus);
                }
            }
        }
        
        if (!found) {
            player.sendMessage("§cNo items found matching '" + searchTerm + "'");
            player.sendMessage("§7Try using different keywords or browse categories with §e/market");
        }
        
        player.sendMessage("§6§l" + "=".repeat(20 + searchTerm.length()));
        player.sendMessage("");
    }
    
    private void openCategoryDirectly(Player player, String categoryName) {
        if (!plugin.getMarketManager().categoryExists(categoryName)) {
            player.sendMessage("§cCategory '" + categoryName + "' does not exist!");
            player.sendMessage("§7Use §e/market §7to see available categories.");
            return;
        }
        
        try {
            MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
            com.dynamicmarket.gui.CategoryGUI categoryGUI = new com.dynamicmarket.gui.CategoryGUI(plugin, player, category);
            categoryGUI.open();
            
            player.sendMessage(plugin.getConfigManager().getMessage("category-opened", "category", categoryName));
            
        } catch (Exception e) {
            player.sendMessage("§cFailed to open category! Please try again or contact an administrator.");
            plugin.getLogger().severe("Failed to open category GUI for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void reloadMarket(Player player) {
        try {
            plugin.getConfigManager().loadConfig();
            plugin.getMarketManager().loadMarketData();
            
            player.sendMessage("§a§lMarket reloaded successfully!");
            plugin.getLogger().info("Market reloaded by " + player.getName());
            
        } catch (Exception e) {
            player.sendMessage("§cFailed to reload market! Check console for errors.");
            plugin.getLogger().severe("Failed to reload market: " + e.getMessage());
            e.printStackTrace();
        }
    }
}