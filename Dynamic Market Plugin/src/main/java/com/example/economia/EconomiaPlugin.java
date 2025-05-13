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
import com.example.economia.commands.SetSlotCommand;  // Add this import at the top with other imports

public class EconomiaPlugin extends JavaPlugin {
    private MarketManager marketManager;
    private MarketGUI marketGUI;
    private Economy economy;

    private void setupConfig() {
        // Delete existing config if it's the wrong format
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            if (getConfig().contains("prices") || 
                !getConfig().contains("categories") || 
                !getConfig().contains("categories.\"Ores and Minerals\".slot")) {  // Check for slot configuration
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
        setupConfig();
        saveDefaultConfig();
        reloadConfig();

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault not found! Please install Vault.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Debug log to check if categories are loaded
        if (getConfig().getConfigurationSection("categories") == null) {
            getLogger().severe("Categories section not found in config.yml!");
            saveResource("config.yml", true);
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
            
            // Register all commands
            getCommand("market").setExecutor(new MarketCommand(marketManager, marketGUI));
            getCommand("sell").setExecutor(new SellCommand(marketManager));
            getCommand("marketadmin").setExecutor(new MarketAdminCommand(marketManager, this));
            // Register commands
            SetSlotCommand setSlotCommand = new SetSlotCommand(this);
            getCommand("setslot").setExecutor(setSlotCommand);
            getCommand("setslot").setTabCompleter(setSlotCommand);
            
            // Register events
            getServer().getPluginManager().registerEvents(new SellGUIListener(new SellGUI(marketManager)), this);
            getServer().getPluginManager().registerEvents(
                new MarketGUIListener(marketManager, marketGUI, economy), 
                this
            );
            
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