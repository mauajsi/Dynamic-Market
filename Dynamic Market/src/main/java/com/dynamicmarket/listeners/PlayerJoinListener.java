package com.dynamicmarket.listeners;

import com.dynamicmarket.DynamicMarket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final DynamicMarket plugin;
    
    public PlayerJoinListener(DynamicMarket plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has permission to receive market notifications
        if (player.hasPermission("market.notifications")) {
            // Send welcome message with market info (delayed to ensure player is fully loaded)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                sendMarketWelcomeMessage(player);
            }, 40L); // 2 seconds delay
        }
        
        // Check if player is an admin and send admin notifications
        if (player.hasPermission("market.admin")) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                sendAdminNotifications(player);
            }, 60L); // 3 seconds delay
        }
        
        // Log player join for market statistics
        plugin.getLogger().info("Player " + player.getName() + " joined - Market system ready");
    }
    
    private void sendMarketWelcomeMessage(Player player) {
        // Check if welcome messages are enabled
        if (!plugin.getConfigManager().getConfig().getBoolean("welcome-message.enabled", true)) {
            return;
        }
        
        // Send welcome message
        player.sendMessage("");
        player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§e§l                    WELCOME TO DYNAMIC MARKET!");
        player.sendMessage("");
        player.sendMessage("§7▪ §fUse §a/market §fto browse and buy items");
        player.sendMessage("§7▪ §fUse §a/sell §fto sell your items for money");
        player.sendMessage("§7▪ §fPrices change based on supply and demand!");
        player.sendMessage("");
        
        // Show current market statistics
        int totalCategories = plugin.getMarketManager().getAllCategories().size();
        int totalItems = plugin.getMarketManager().getAllCategories().stream()
            .mapToInt(category -> category.getAllItems().size())
            .sum();
            
        player.sendMessage("§7▪ §eMarket Status: §a" + totalCategories + " categories, " + totalItems + " items available");
        
        double balance = plugin.getVaultEconomy().getBalance(player);
        player.sendMessage("§7▪ §eYour Balance: §a$" + String.format("%.2f", balance));
        
        player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("");
    }
    
    private void sendAdminNotifications(Player player) {
        // Check if admin notifications are enabled
        if (!plugin.getConfigManager().getConfig().getBoolean("admin-notifications.enabled", true)) {
            return;
        }
        
        // Send admin notification
        player.sendMessage("");
        player.sendMessage("§c§l[MARKET ADMIN] §7You have administrative access to the market system.");
        
        // Check for any market issues that need attention
        checkAndReportMarketIssues(player);
        
        player.sendMessage("§7Use §c/marketadmin §7for management commands.");
        player.sendMessage("");
    }
    
    private void checkAndReportMarketIssues(Player player) {
        // Check for low stock items
        int lowStockCount = 0;
        int outOfStockCount = 0;
        
        int lowStockThreshold = plugin.getConfigManager().getConfig().getInt("low-stock-threshold", 10);
        
        for (com.dynamicmarket.data.MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            for (com.dynamicmarket.data.MarketItem item : category.getAllItems()) {
                if (item.getStock() <= 0) {
                    outOfStockCount++;
                } else if (item.getStock() <= lowStockThreshold) {
                    lowStockCount++;
                }
            }
        }
        
        if (outOfStockCount > 0) {
            player.sendMessage("§c§l⚠ WARNING: " + outOfStockCount + " items are out of stock!");
        }
        
        if (lowStockCount > 0) {
            player.sendMessage("§e§l⚠ NOTICE: " + lowStockCount + " items have low stock.");
        }
        
        // Check for price anomalies
        checkPriceAnomalies(player);
        
        // Check database connection
        try {
            if (!plugin.getDataManager().getDatabaseManager().isConnected()) {
                player.sendMessage("§c§l⚠ CRITICAL: Database connection is not available!");
            }
        } catch (Exception e) {
            player.sendMessage("§c§l⚠ CRITICAL: Database connection check failed!");
        }
    }
    
    private void checkPriceAnomalies(Player player) {
        int highPriceCount = 0;
        int lowPriceCount = 0;
        
        double maxPrice = plugin.getConfigManager().getMaxPrice();
        double minPrice = plugin.getConfigManager().getMinPrice();
        
        for (com.dynamicmarket.data.MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            for (com.dynamicmarket.data.MarketItem item : category.getAllItems()) {
                if (item.getBuyPrice() >= maxPrice * 0.9) { // 90% of max price
                    highPriceCount++;
                }
                if (item.getBuyPrice() <= minPrice * 1.1) { // 110% of min price
                    lowPriceCount++;
                }
            }
        }
        
        if (highPriceCount > 0) {
            player.sendMessage("§e§l⚠ NOTICE: " + highPriceCount + " items have very high prices.");
        }
        
        if (lowPriceCount > 0) {
            player.sendMessage("§e§l⚠ NOTICE: " + lowPriceCount + " items have very low prices.");
        }
    }
    
    /**
     * Handles first-time player setup
     */
    private void handleFirstTimePlayer(Player player) {
        // Check if this is the player's first time
        if (!player.hasPlayedBefore()) {
            // Give starting money if configured
            double startingMoney = plugin.getConfigManager().getConfig().getDouble("starting-money", 0.0);
            if (startingMoney > 0) {
                plugin.getVaultEconomy().depositPlayer(player, startingMoney);
                player.sendMessage("§a§lWelcome! You've been given $" + String.format("%.2f", startingMoney) + " to get started!");
            }
            
            // Send tutorial message
            sendTutorialMessage(player);
        }
    }
    
    private void sendTutorialMessage(Player player) {
        if (!plugin.getConfigManager().getConfig().getBoolean("tutorial.enabled", true)) {
            return;
        }
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("");
            player.sendMessage("§6§l=== MARKET TUTORIAL ===");
            player.sendMessage("§7This server uses a dynamic market system!");
            player.sendMessage("");
            player.sendMessage("§e1. §fUse §a/market §fto open the market and buy items");
            player.sendMessage("§e2. §fUse §a/sell §fto sell items from your inventory");
            player.sendMessage("§e3. §fPrices change based on how much items are traded");
            player.sendMessage("§e4. §fBuy low, sell high to make profit!");
            player.sendMessage("");
            player.sendMessage("§7§oTip: Popular items become more expensive, rare items become cheaper!");
            player.sendMessage("§6§l========================");
            player.sendMessage("");
        }, 100L); // 5 seconds delay
    }
}