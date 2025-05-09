package com.example.economia;

import com.example.economia.gui.SellGUI;
import com.example.economia.gui.SellGUIListener;
import org.bukkit.plugin.java.JavaPlugin;
import com.example.economia.commands.SellCommand;
import com.example.economia.commands.MarketCommand;
import com.example.economia.gui.MarketGUI;
import com.example.economia.gui.MarketGUIListener;
import net.milkbowl.vault.economy.Economy;
import java.io.File;  // Add this import
import com.example.economia.commands.MarketAdminCommand;  // Add this import

public class EconomiaPlugin extends JavaPlugin {
    private MarketManager marketManager;
    private MarketGUI marketGUI;
    private Economy economy;

    private void setupConfig() {
        // Delete existing config if it's the wrong format
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            if (getConfig().contains("prices") || !getConfig().contains("categories")) {
                configFile.delete();
                saveResource("config.yml", true);
            }
        } else {
            saveResource("config.yml", false);
        }
        
        reloadConfig();
        
        // Verify the config is correct
        if (getConfig().getConfigurationSection("categories") == null || 
            getConfig().getConfigurationSection("categories").getKeys(false).isEmpty()) {
            getLogger().warning("Config.yml is invalid or empty. Regenerating...");
            saveResource("config.yml", true);
            reloadConfig();
        }
        
        getLogger().info("Config loaded successfully with " + 
            getConfig().getConfigurationSection("categories").getKeys(false).size() + 
            " categories.");
    }

    @Override
    public void onEnable() {
        setupConfig();  // Add this line at the start
        // Save default config if it doesn't exist
        saveDefaultConfig();
        reloadConfig();

        // First, check if Vault is present
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault not found! Please install Vault.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Debug log to check if categories are loaded
        if (getConfig().getConfigurationSection("categories") == null) {
            getLogger().severe("Categories section not found in config.yml!");
            saveResource("config.yml", true); // Force save default config
            reloadConfig();
        }

        // Wait longer to ensure economy provider is ready
        getServer().getScheduler().runTaskLater(this, () -> {
            if (!setupEconomy()) {
                            getLogger().severe("No economy provider found! Please install an economy plugin (like EssentialsX).");
                            getServer().getPluginManager().disablePlugin(this);
                            return;
                        }
            
                        // Initialize MarketManager with economy
                        this.marketManager = new MarketManager(economy, this);
                        this.marketGUI = new MarketGUI(marketManager, this);
                        
                        // Debug log
                        getLogger().info("Market categories loaded: " + getConfig().getConfigurationSection("categories").getKeys(false).size());
            
                        // Register commands and events
                        getCommand("market").setExecutor(new MarketCommand(marketManager, marketGUI));
                        getCommand("sell").setExecutor(new SellCommand(marketManager));
                        getServer().getPluginManager().registerEvents(new SellGUIListener(new SellGUI(marketManager)), this);
                        getServer().getPluginManager().registerEvents(
                            new MarketGUIListener(marketManager, marketGUI, economy), 
                            this
                        );
                        
                        // In onEnable method, add this line where you register other commands
                        getCommand("marketadmin").setExecutor(new MarketAdminCommand(marketManager, this));
                        getLogger().info("DynamicMarket has been successfully enabled!");
                    }, 60L);
                }
            
                private boolean setupEconomy() {
                    if (getServer().getPluginManager().getPlugin("Vault") == null) {
                        return false;
                    }
                    org.bukkit.plugin.RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                    if (rsp == null) {
                        return false;
                    }
                    economy = rsp.getProvider();
                    return economy != null;
                }
            
                @Override
    public void onDisable() {
        if (marketManager != null) {
            marketManager.saveMarketData();
        }
        getLogger().info("DynamicMarket has been disabled!");
    }
}