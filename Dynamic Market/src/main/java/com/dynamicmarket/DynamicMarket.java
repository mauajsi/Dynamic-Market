package com.dynamicmarket;

import com.dynamicmarket.commands.*;
import com.dynamicmarket.config.ConfigManager;
import com.dynamicmarket.data.DataManager;
import com.dynamicmarket.economy.VaultEconomy;
import com.dynamicmarket.listeners.*;
import com.dynamicmarket.market.MarketManager;
import com.dynamicmarket.managers.PermissionManager;
import com.dynamicmarket.utils.MarketScheduler;
import org.bukkit.plugin.java.JavaPlugin;

public class DynamicMarket extends JavaPlugin {
    
    private static DynamicMarket instance;
    private VaultEconomy vaultEconomy;
    private MarketManager marketManager;
    private ConfigManager configManager;
    private DataManager dataManager;
    private com.dynamicmarket.managers.PermissionManager permissionManager;
    private MarketScheduler marketScheduler;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Check for Vault dependency
        if (!setupVault()) {
            getLogger().severe("Vault not found! This plugin requires Vault to function.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.vaultEconomy = new VaultEconomy(this);
        this.permissionManager = new PermissionManager(this);
        this.marketManager = new MarketManager(this);
        this.marketScheduler = new MarketScheduler(this);
        
        // Check economy setup
        if (!vaultEconomy.isEnabled()) {
            getLogger().severe("No economy plugin found! Please install an economy plugin that supports Vault.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Start scheduler
        marketScheduler.startTasks();
        
        getLogger().info("DynamicMarket has been enabled!");
        getLogger().info("Economy: " + vaultEconomy.getEconomyName());
        getLogger().info("Permissions registered: " + permissionManager.getAllPermissions().size());
    }
    
    @Override
    public void onDisable() {
        if (marketScheduler != null) {
            marketScheduler.stopTasks();
        }
        
        if (dataManager != null) {
            dataManager.saveAllData();
        }
        
        if (permissionManager != null) {
            permissionManager.unregisterPermissions();
        }
        
        getLogger().info("DynamicMarket has been disabled!");
    }
    
    /**
     * Setup Vault dependency
     * @return true if Vault is available
     */
    private boolean setupVault() {
        return getServer().getPluginManager().getPlugin("Vault") != null;
    }
    
    private void registerCommands() {
        getCommand("market").setExecutor(new MarketCommand(this));
        getCommand("sell").setExecutor(new SellCommand(this));
        MarketAdminCommand marketAdminCommand = new MarketAdminCommand(this);
        getCommand("marketadmin").setExecutor(marketAdminCommand);
        getCommand("marketadmin").setTabCompleter(marketAdminCommand);
        getCommand("marketsettings").setExecutor(new MarketSettingsCommand(this));
        getCommand("setslot").setExecutor(new SetSlotCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new com.dynamicmarket.listeners.InventoryClickListener(this), this);
    }
    
    public static DynamicMarket getInstance() {
        return instance;
    }
    
    public VaultEconomy getVaultEconomy() {
        return vaultEconomy;
    }
    
    public MarketManager getMarketManager() {
        return marketManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
    
    public MarketScheduler getMarketScheduler() {
        return marketScheduler;
    }

	public Object getDatabaseManager() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getDatabaseManager'");
	}

    public VaultEconomy getEconomy() {
        return vaultEconomy;
    }

}