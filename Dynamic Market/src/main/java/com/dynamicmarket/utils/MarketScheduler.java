package com.dynamicmarket.utils;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketCategory;
import com.dynamicmarket.data.MarketItem;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public class MarketScheduler {
    
    private final DynamicMarket plugin;
    private final PriceCalculator priceCalculator;
    private BukkitTask autoSaveTask;
    private BukkitTask priceUpdateTask;
    private BukkitTask marketAnalysisTask;
    
    public MarketScheduler(DynamicMarket plugin) {
        this.plugin = plugin;
        this.priceCalculator = new PriceCalculator(plugin);
    }
    
    public void startTasks() {
        startScheduledTasks();
    }
    
    public void stopTasks() {
        stopScheduledTasks();
    }
    
    public void startScheduledTasks() {
        startAutoSaveTask();
        startPriceUpdateTask();
        startMarketAnalysisTask();
        
        plugin.getLogger().info("Market scheduler tasks started successfully!");
    }
    
    public void stopScheduledTasks() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        if (priceUpdateTask != null) {
            priceUpdateTask.cancel();
        }
        if (marketAnalysisTask != null) {
            marketAnalysisTask.cancel();
        }
        
        plugin.getLogger().info("Market scheduler tasks stopped.");
    }
    
    /**
     * Auto-save market data periodically
     */
    private void startAutoSaveTask() {
        int saveInterval = plugin.getConfigManager().getAutoSaveInterval();
        
        autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getMarketManager().saveMarketData();
                plugin.getLogger().info("Market data auto-saved.");
            }
        }.runTaskTimerAsynchronously(plugin, saveInterval * 20L, saveInterval * 20L);
    }
    
    /**
     * Update prices based on time decay and market conditions
     */
    private void startPriceUpdateTask() {
        priceUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateMarketPrices();
            }
        }.runTaskTimerAsynchronously(plugin, 6000L, 6000L); // Every 5 minutes
    }
    
    /**
     * Perform market analysis and adjustments
     */
    private void startMarketAnalysisTask() {
        marketAnalysisTask = new BukkitRunnable() {
            @Override
            public void run() {
                performMarketAnalysis();
            }
        }.runTaskTimerAsynchronously(plugin, 72000L, 72000L); // Every hour
    }
    
    /**
     * Updates all market prices based on time decay and market conditions
     */
    private void updateMarketPrices() {
        try {
            for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
                for (MarketItem item : category.getAllItems()) {
                    // Apply time-based decay
                    priceCalculator.applyTimeBasedDecay(item);
                    
                    // Apply market condition adjustments
                    applyMarketConditionAdjustments(item);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error during price update: " + e.getMessage());
        }
    }
    
    /**
     * Applies market condition adjustments to item prices
     */
    private void applyMarketConditionAdjustments(MarketItem item) {
        // Check if item needs price adjustment based on stock levels
        int stock = item.getStock();
        double currentBuyPrice = item.getBuyPrice();
        double currentSellPrice = item.getSellPrice();
        
        // Adjust prices based on stock scarcity
        if (stock <= 5) {
            // Very low stock - increase buy price, decrease sell price
            double newBuyPrice = currentBuyPrice * 1.02; // 2% increase
            double newSellPrice = currentSellPrice * 0.98; // 2% decrease
            
            item.setBuyPrice(newBuyPrice);
            item.setSellPrice(newSellPrice);
        } else if (stock >= 500) {
            // High stock - decrease buy price, increase sell price
            double newBuyPrice = currentBuyPrice * 0.99; // 1% decrease
            double newSellPrice = currentSellPrice * 1.01; // 1% increase
            
            item.setBuyPrice(newBuyPrice);
            item.setSellPrice(newSellPrice);
        }
        
        // Ensure sell price is always lower than buy price
        if (item.getSellPrice() >= item.getBuyPrice()) {
            item.setSellPrice(item.getBuyPrice() * 0.7);
        }
    }
    
    /**
     * Performs comprehensive market analysis
     */
    private void performMarketAnalysis() {
        try {
            int totalCategories = plugin.getMarketManager().getAllCategories().size();
            int totalItems = 0;
            int lowStockItems = 0;
            int highStockItems = 0;
            double totalMarketValue = 0.0;
            
            for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
                for (MarketItem item : category.getAllItems()) {
                    totalItems++;
                    totalMarketValue += item.getBuyPrice() * item.getStock();
                    
                    if (item.getStock() <= 10) {
                        lowStockItems++;
                    } else if (item.getStock() >= 200) {
                        highStockItems++;
                    }
                    
                    // Perform item-specific analysis
                    analyzeItemPerformance(item);
                }
            }
            
            // Log market statistics
            plugin.getLogger().info(String.format(
                "Market Analysis - Categories: %d, Items: %d, Low Stock: %d, High Stock: %d, Total Value: $%.2f",
                totalCategories, totalItems, lowStockItems, highStockItems, totalMarketValue
            ));
            
            // Perform market rebalancing if needed
            if (lowStockItems > totalItems * 0.3) { // More than 30% items are low stock
                performEmergencyRestock();
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error during market analysis: " + e.getMessage());
        }
    }
    
    /**
     * Analyzes individual item performance and suggests adjustments
     */
    private void analyzeItemPerformance(MarketItem item) {
        // Calculate trading activity
        int totalTransactions = item.getTotalBought() + item.getTotalSold();
        long daysSinceLastUpdate = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - item.getLastUpdated()
        );
        
        // Flag items with unusual activity
        if (totalTransactions > 1000 && daysSinceLastUpdate <= 7) {
            // High activity item - might need price adjustment
            double[] suggestedPrices = priceCalculator.suggestPriceAdjustment(item);
            
            // Apply gradual adjustment (10% of suggested change)
            double buyAdjustment = (suggestedPrices[0] - item.getBuyPrice()) * 0.1;
            double sellAdjustment = (suggestedPrices[1] - item.getSellPrice()) * 0.1;
            
            item.setBuyPrice(item.getBuyPrice() + buyAdjustment);
            item.setSellPrice(item.getSellPrice() + sellAdjustment);
        }
        
        // Flag stagnant items
        if (totalTransactions == 0 && daysSinceLastUpdate > 30) {
            // No activity for 30+ days - reduce prices to stimulate trading
            item.setBuyPrice(item.getBuyPrice() * 0.95);
            item.setSellPrice(item.getSellPrice() * 1.05);
        }
    }
    
    /**
     * Performs emergency restocking for low-stock items
     */
    private void performEmergencyRestock() {
        plugin.getLogger().info("Performing emergency market restock...");
        
        for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            for (MarketItem item : category.getAllItems()) {
                if (item.getStock() <= 5) {
                    // Add emergency stock
                    int emergencyStock = Math.min(50, item.getStock() * 10);
                    item.setStock(item.getStock() + emergencyStock);
                    
                    // Slightly increase buy price due to restocking cost
                    item.setBuyPrice(item.getBuyPrice() * 1.01);
                }
            }
        }
    }
    
    /**
     * Manually triggers a price update for all items
     */
    public void triggerPriceUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::updateMarketPrices);
    }
    
    /**
     * Manually triggers market analysis
     */
    public void triggerMarketAnalysis() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::performMarketAnalysis);
    }
    
    /**
     * Gets the price calculator instance
     */
    public PriceCalculator getPriceCalculator() {
        return priceCalculator;
    }
}