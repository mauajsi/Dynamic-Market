package com.dynamicmarket.economy;

import com.dynamicmarket.DynamicMarket;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEconomy {
    
    private final DynamicMarket plugin;
    private Economy economy;
    private boolean vaultEnabled;
    
    public VaultEconomy(DynamicMarket plugin) {
        this.plugin = plugin;
        this.vaultEnabled = false;
        setupEconomy();
    }
    
    /**
     * Setup Vault economy integration
     * @return true if Vault economy was successfully setup
     */
    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin not found! Economy features will be disabled.");
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No economy plugin found! Please install an economy plugin that supports Vault.");
            return false;
        }
        
        economy = rsp.getProvider();
        vaultEnabled = economy != null;
        
        if (vaultEnabled) {
            plugin.getLogger().info("Successfully hooked into Vault economy: " + economy.getName());
        } else {
            plugin.getLogger().severe("Failed to hook into Vault economy!");
        }
        
        return vaultEnabled;
    }
    
    /**
     * Check if Vault economy is enabled and working
     * @return true if Vault economy is available
     */
    public boolean isEnabled() {
        return vaultEnabled && economy != null;
    }
    
    /**
     * Get the name of the economy plugin
     * @return economy plugin name or "Unknown" if not available
     */
    public String getEconomyName() {
        return isEnabled() ? economy.getName() : "Unknown";
    }
    
    /**
     * Get player's balance
     * @param player the player
     * @return player's balance or 0.0 if economy is disabled
     */
    public double getBalance(Player player) {
        if (!isEnabled()) {
            plugin.getLogger().warning("Attempted to get balance for " + player.getName() + " but economy is disabled!");
            return 0.0;
        }
        
        try {
            return economy.getBalance(player);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting balance for " + player.getName() + ": " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Check if player has enough money
     * @param player the player
     * @param amount the amount to check
     * @return true if player has enough money
     */
    public boolean hasBalance(Player player, double amount) {
        if (!isEnabled()) {
            return false;
        }
        
        if (amount <= 0) {
            return true;
        }
        
        try {
            return economy.has(player, amount);
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking balance for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Withdraw money from player's account
     * @param player the player
     * @param amount the amount to withdraw
     * @return true if withdrawal was successful
     */
    public boolean withdrawPlayer(Player player, double amount) {
        if (!isEnabled()) {
            plugin.getLogger().warning("Attempted to withdraw $" + amount + " from " + player.getName() + " but economy is disabled!");
            return false;
        }
        
        if (amount <= 0) {
            plugin.getLogger().warning("Attempted to withdraw invalid amount: $" + amount);
            return false;
        }
        
        try {
            EconomyResponse response = economy.withdrawPlayer(player, amount);
            
            if (response.transactionSuccess()) {
                plugin.getLogger().info("Withdrew $" + String.format("%.2f", amount) + " from " + player.getName() + 
                                      " (New balance: $" + String.format("%.2f", response.balance) + ")");
                return true;
            } else {
                plugin.getLogger().warning("Failed to withdraw $" + amount + " from " + player.getName() + 
                                          ": " + response.errorMessage);
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error withdrawing money from " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deposit money to player's account
     * @param player the player
     * @param amount the amount to deposit
     * @return true if deposit was successful
     */
    public boolean depositPlayer(Player player, double amount) {
        if (!isEnabled()) {
            plugin.getLogger().warning("Attempted to deposit $" + amount + " to " + player.getName() + " but economy is disabled!");
            return false;
        }
        
        if (amount <= 0) {
            plugin.getLogger().warning("Attempted to deposit invalid amount: $" + amount);
            return false;
        }
        
        try {
            EconomyResponse response = economy.depositPlayer(player, amount);
            
            if (response.transactionSuccess()) {
                plugin.getLogger().info("Deposited $" + String.format("%.2f", amount) + " to " + player.getName() + 
                                      " (New balance: $" + String.format("%.2f", response.balance) + ")");
                return true;
            } else {
                plugin.getLogger().warning("Failed to deposit $" + amount + " to " + player.getName() + 
                                          ": " + response.errorMessage);
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error depositing money to " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Format currency amount according to economy plugin settings
     * @param amount the amount to format
     * @return formatted currency string
     */
    public String formatCurrency(double amount) {
        if (!isEnabled()) {
            return "$" + String.format("%.2f", amount);
        }
        
        try {
            return economy.format(amount);
        } catch (Exception e) {
            plugin.getLogger().warning("Error formatting currency: " + e.getMessage());
            return "$" + String.format("%.2f", amount);
        }
    }
    
    /**
     * Get the currency name (singular)
     * @return currency name or "Dollar" if not available
     */
    public Economy getEconomy() {
        return economy;
    }
    
    public String getCurrencyNameSingular() {
        if (!isEnabled()) {
            return "Dollar";
        }
        
        try {
            return economy.currencyNameSingular();
        } catch (Exception e) {
            return "Dollar";
        }
    }
    
    /**
     * Get the currency name (plural)
     * @return currency name or "Dollars" if not available
     */
    public String getCurrencyNamePlural() {
        if (!isEnabled()) {
            return "Dollars";
        }
        
        try {
            return economy.currencyNamePlural();
        } catch (Exception e) {
            return "Dollars";
        }
    }
    
    /**
     * Check if economy supports banks
     * @return true if banks are supported
     */
    public boolean hasBankSupport() {
        return isEnabled() && economy.hasBankSupport();
    }
    
    /**
     * Give starting money to a new player
     * @param player the new player
     * @return true if starting money was given successfully
     */
    public boolean giveStartingMoney(Player player) {
        double startingMoney = plugin.getConfigManager().getDouble("starting-money");
        
        if (startingMoney <= 0) {
            return true; // No starting money configured
        }
        
        if (!isEnabled()) {
            plugin.getLogger().warning("Cannot give starting money to " + player.getName() + " - economy is disabled!");
            return false;
        }
        
        // Check if player already has money (to avoid giving starting money multiple times)
        double currentBalance = getBalance(player);
        if (currentBalance > 0) {
            plugin.getLogger().info("Player " + player.getName() + " already has money ($" + 
                                  String.format("%.2f", currentBalance) + "), skipping starting money.");
            return true;
        }
        
        boolean success = depositPlayer(player, startingMoney);
        if (success) {
            player.sendMessage(plugin.getConfigManager().getMessage("starting-money")
                .replace("{amount}", formatCurrency(startingMoney)));
            
            plugin.getLogger().info("Gave starting money of $" + String.format("%.2f", startingMoney) + 
                                  " to new player " + player.getName());
        }
        
        return success;
    }
    
    /**
     * Perform a transaction (withdraw from buyer, deposit to market/seller)
     * @param buyer the player buying
     * @param amount the transaction amount
     * @param description transaction description for logging
     * @return true if transaction was successful
     */
    public boolean performTransaction(Player buyer, double amount, String description) {
        if (!isEnabled()) {
            buyer.sendMessage("§cEconomy system is not available!");
            return false;
        }
        
        if (amount <= 0) {
            buyer.sendMessage("§cInvalid transaction amount!");
            return false;
        }
        
        if (!hasBalance(buyer, amount)) {
            buyer.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds")
                .replace("{amount}", formatCurrency(amount))
                .replace("{balance}", formatCurrency(getBalance(buyer))));
            return false;
        }
        
        boolean success = withdrawPlayer(buyer, amount);
        if (success) {
            plugin.getLogger().info("Transaction completed: " + buyer.getName() + " paid " + 
                                  formatCurrency(amount) + " for " + description);
        }
        
        return success;
    }
    
    /**
     * Perform a sell transaction (deposit to seller)
     * @param seller the player selling
     * @param amount the transaction amount
     * @param description transaction description for logging
     * @return true if transaction was successful
     */
    public boolean performSellTransaction(Player seller, double amount, String description) {
        if (!isEnabled()) {
            seller.sendMessage("§cEconomy system is not available!");
            return false;
        }
        
        if (amount <= 0) {
            seller.sendMessage("§cInvalid transaction amount!");
            return false;
        }
        
        boolean success = depositPlayer(seller, amount);
        if (success) {
            plugin.getLogger().info("Sell transaction completed: " + seller.getName() + " received " + 
                                  formatCurrency(amount) + " for " + description);
        }
        
        return success;
    }
    
    /**
     * Get economy status information
     * @return economy status string
     */
    public String getEconomyStatus() {
        if (!isEnabled()) {
            return "§cDisabled (Vault or economy plugin not found)";
        }
        
        return "§aEnabled (" + getEconomyName() + ")";
    }
    
    /**
     * Reload economy connection (useful after economy plugin reload)
     */
    public void reload() {
        plugin.getLogger().info("Reloading Vault economy connection...");
        vaultEnabled = false;
        economy = null;
        setupEconomy();
    }
    
    /**
     * Get detailed economy information for admin commands
     * @return detailed economy info
     */
    public String getDetailedEconomyInfo() {
        StringBuilder info = new StringBuilder();
        
        info.append("§7▪ §eStatus: ").append(getEconomyStatus()).append("\n");
        
        if (isEnabled()) {
            info.append("§7▪ §eEconomy Plugin: §f").append(getEconomyName()).append("\n");
            info.append("§7▪ §eCurrency (Singular): §f").append(getCurrencyNameSingular()).append("\n");
            info.append("§7▪ §eCurrency (Plural): §f").append(getCurrencyNamePlural()).append("\n");
            info.append("§7▪ §eBank Support: §f").append(hasBankSupport() ? "Yes" : "No").append("\n");
            info.append("§7▪ §eStarting Money: §f").append(formatCurrency(plugin.getConfigManager().getDouble("starting-money")));
        } else {
            info.append("§7▪ §eReason: §cVault or economy plugin not found");
            info.append("§7▪ §eRequired: §fVault + compatible economy plugin (EssentialsX, etc.)");
        }
        
        return info.toString();
    }


}