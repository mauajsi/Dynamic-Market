package com.dynamicmarket.utils;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketItem;

public class PriceCalculator {
    
    private final DynamicMarket plugin;
    private final double priceAdjustmentRate;
    private final double minPrice;
    private final double maxPrice;
    
    public PriceCalculator(DynamicMarket plugin) {
        this.plugin = plugin;
        this.priceAdjustmentRate = plugin.getConfigManager().getPriceAdjustmentRate();
        this.minPrice = plugin.getConfigManager().getMinPrice();
        this.maxPrice = plugin.getConfigManager().getMaxPrice();
    }
    
    /**
     * Calculates the new buy price after a purchase
     * Buy prices increase when items are frequently purchased
     */
    public double calculateNewBuyPrice(MarketItem item, int amountBought) {
        double currentPrice = item.getBuyPrice();
        double adjustment = currentPrice * (amountBought * priceAdjustmentRate);
        double newPrice = currentPrice + adjustment;
        
        // Apply demand multiplier based on stock levels
        double demandMultiplier = calculateDemandMultiplier(item);
        newPrice *= demandMultiplier;
        
        return clampPrice(newPrice);
    }
    
    /**
     * Calculates the new sell price after a sale to the market
     * Sell prices decrease when items are frequently sold to the market
     */
    public double calculateNewSellPrice(MarketItem item, int amountSold) {
        double currentPrice = item.getSellPrice();
        double adjustment = currentPrice * (amountSold * priceAdjustmentRate);
        double newPrice = currentPrice - adjustment;
        
        // Apply supply multiplier based on stock levels
        double supplyMultiplier = calculateSupplyMultiplier(item);
        newPrice *= supplyMultiplier;
        
        return clampPrice(newPrice);
    }
    
    /**
     * Calculates demand multiplier based on current stock levels
     * Lower stock = higher demand = higher prices
     */
    private double calculateDemandMultiplier(MarketItem item) {
        int stock = item.getStock();
        int totalBought = item.getTotalBought();
        
        if (totalBought == 0) {
            return 1.0; // No trading history, no adjustment
        }
        
        // Calculate stock ratio (lower stock = higher demand)
        double stockRatio = Math.max(0.1, (double) stock / (stock + totalBought));
        
        // Demand multiplier: 0.8 to 1.5 range
        // Low stock (high demand) = multiplier closer to 1.5
        // High stock (low demand) = multiplier closer to 0.8
        return 0.8 + (0.7 * (1.0 - stockRatio));
    }
    
    /**
     * Calculates supply multiplier based on how much has been sold to the market
     * More items sold to market = higher supply = lower sell prices
     */
    private double calculateSupplyMultiplier(MarketItem item) {
        int totalSold = item.getTotalSold();
        int totalBought = item.getTotalBought();
        
        if (totalSold + totalBought == 0) {
            return 1.0; // No trading history, no adjustment
        }
        
        // Calculate supply ratio (more sold to market = higher supply)
        double supplyRatio = (double) totalSold / (totalSold + totalBought);
        
        // Supply multiplier: 0.7 to 1.2 range
        // High supply = multiplier closer to 0.7
        // Low supply = multiplier closer to 1.2
        return 1.2 - (0.5 * supplyRatio);
    }
    
    /**
     * Calculates the optimal buy price based on market trends
     */
    public double calculateOptimalBuyPrice(MarketItem item) {
        double basePrice = item.getBuyPrice();
        
        // Factor in trading velocity (how fast the item is being traded)
        double velocity = calculateTradingVelocity(item);
        double velocityMultiplier = 1.0 + (velocity * 0.1); // Up to 10% increase for high velocity
        
        // Factor in stock scarcity
        double scarcityMultiplier = calculateScarcityMultiplier(item);
        
        double optimalPrice = basePrice * velocityMultiplier * scarcityMultiplier;
        
        return clampPrice(optimalPrice);
    }
    
    /**
     * Calculates the optimal sell price based on market trends
     */
    public double calculateOptimalSellPrice(MarketItem item) {
        double basePrice = item.getSellPrice();
        
        // Factor in market saturation (how much of this item is being sold)
        double saturation = calculateMarketSaturation(item);
        double saturationMultiplier = 1.0 - (saturation * 0.15); // Up to 15% decrease for high saturation
        
        double optimalPrice = basePrice * saturationMultiplier;
        
        return clampPrice(optimalPrice);
    }
    
    /**
     * Calculates trading velocity (transactions per time unit)
     */
    private double calculateTradingVelocity(MarketItem item) {
        long timeSinceLastUpdate = System.currentTimeMillis() - item.getLastUpdated();
        long hoursElapsed = Math.max(1, timeSinceLastUpdate / (1000 * 60 * 60)); // Convert to hours
        
        int totalTransactions = item.getTotalBought() + item.getTotalSold();
        
        return Math.min(1.0, (double) totalTransactions / hoursElapsed);
    }
    
    /**
     * Calculates scarcity multiplier based on stock levels
     */
    private double calculateScarcityMultiplier(MarketItem item) {
        int stock = item.getStock();
        
        if (stock <= 0) {
            return 2.0; // Very scarce, double the price
        } else if (stock <= 10) {
            return 1.5; // Scarce, 50% price increase
        } else if (stock <= 50) {
            return 1.2; // Somewhat scarce, 20% price increase
        } else {
            return 1.0; // Abundant, no price increase
        }
    }
    
    /**
     * Calculates market saturation (how much is being sold vs bought)
     */
    private double calculateMarketSaturation(MarketItem item) {
        int totalSold = item.getTotalSold();
        int totalBought = item.getTotalBought();
        
        if (totalSold + totalBought == 0) {
            return 0.0;
        }
        
        return Math.min(1.0, (double) totalSold / (totalSold + totalBought));
    }
    
    /**
     * Applies time-based price decay to simulate market stabilization
     */
    public void applyTimeBasedDecay(MarketItem item) {
        long timeSinceLastUpdate = System.currentTimeMillis() - item.getLastUpdated();
        long hoursElapsed = timeSinceLastUpdate / (1000 * 60 * 60);
        
        if (hoursElapsed >= 24) { // Apply decay after 24 hours of no activity
            double decayRate = 0.01 * (hoursElapsed / 24); // 1% decay per day
            decayRate = Math.min(0.1, decayRate); // Max 10% decay
            
            double currentBuyPrice = item.getBuyPrice();
            double currentSellPrice = item.getSellPrice();
            
            // Move prices towards their base values (assuming base is the average)
            double averagePrice = (currentBuyPrice + currentSellPrice) / 2;
            
            double newBuyPrice = currentBuyPrice - ((currentBuyPrice - averagePrice) * decayRate);
            double newSellPrice = currentSellPrice + ((averagePrice - currentSellPrice) * decayRate);
            
            item.setBuyPrice(clampPrice(newBuyPrice));
            item.setSellPrice(clampPrice(newSellPrice));
            item.setLastUpdated(System.currentTimeMillis());
        }
    }
    
    /**
     * Ensures price stays within configured bounds
     */
    private double clampPrice(double price) {
        return Math.max(minPrice, Math.min(maxPrice, price));
    }
    
    /**
     * Calculates the profit margin for an item
     */
    public double calculateProfitMargin(MarketItem item) {
        double buyPrice = item.getBuyPrice();
        double sellPrice = item.getSellPrice();
        
        if (sellPrice == 0) {
            return 0.0;
        }
        
        return ((buyPrice - sellPrice) / buyPrice) * 100;
    }
    
    /**
     * Suggests a balanced price adjustment for admin use
     */
    public double[] suggestPriceAdjustment(MarketItem item) {
        double optimalBuyPrice = calculateOptimalBuyPrice(item);
        double optimalSellPrice = calculateOptimalSellPrice(item);
        
        // Ensure sell price is always lower than buy price
        if (optimalSellPrice >= optimalBuyPrice) {
            optimalSellPrice = optimalBuyPrice * 0.7; // 30% margin
        }
        
        return new double[]{optimalBuyPrice, optimalSellPrice};
    }
}