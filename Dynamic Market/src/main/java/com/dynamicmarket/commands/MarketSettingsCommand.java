package com.dynamicmarket.commands;

import com.dynamicmarket.DynamicMarket;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class MarketSettingsCommand implements CommandExecutor {
    
    private final DynamicMarket plugin;
    
    public MarketSettingsCommand(DynamicMarket plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check admin permission
        if (!sender.hasPermission("market.admin.settings")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            showAllSettings(sender);
            return true;
        }
        
        if (args.length == 1) {
            String setting = args[0].toLowerCase();
            
            if (setting.equals("help") || setting.equals("?")) {
                sendHelpMessage(sender);
                return true;
            }
            
            // Show specific setting value
            showSetting(sender, setting);
            return true;
        }
        
        if (args.length >= 2) {
            String setting = args[0].toLowerCase();
            String value = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            
            setSetting(sender, setting, value);
            return true;
        }
        
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§c§l=== MARKET SETTINGS HELP ===");
        sender.sendMessage("§e/marketsettings §7- Show all current settings");
        sender.sendMessage("§e/marketsettings <setting> §7- Show specific setting value");
        sender.sendMessage("§e/marketsettings <setting> <value> §7- Change setting value");
        sender.sendMessage("");
        sender.sendMessage("§7Available settings:");
        sender.sendMessage("§e• price-adjustment-rate §7- Price change percentage per trade (0.1-10.0)");
        sender.sendMessage("§e• min-price §7- Minimum item price (0.01-100.0)");
        sender.sendMessage("§e• max-price §7- Maximum item price (100.0-10000.0)");
        sender.sendMessage("§e• auto-save-interval §7- Auto-save interval in minutes (1-60)");
        sender.sendMessage("§e• default-stock §7- Default stock for new items (1-1000)");
        sender.sendMessage("§e• max-stock §7- Maximum stock per item (100-10000)");
        sender.sendMessage("§e• price-decay-rate §7- Price decay rate per hour (0.0-1.0)");
        sender.sendMessage("§e• demand-multiplier §7- Demand effect on prices (0.5-3.0)");
        sender.sendMessage("§e• supply-multiplier §7- Supply effect on prices (0.5-3.0)");
        sender.sendMessage("§e• confirmation-threshold §7- Price threshold for confirmation GUI");
        sender.sendMessage("§e• starting-money §7- Starting money for new players");
        sender.sendMessage("§e• market-title §7- Main market GUI title");
        sender.sendMessage("§e• category-title §7- Category GUI title format");
        sender.sendMessage("§e• sell-title §7- Sell GUI title");
        sender.sendMessage("§c§l=============================");
        sender.sendMessage("");
    }
    
    private void showAllSettings(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§c§l=== CURRENT MARKET SETTINGS ===");
        
        // Economic settings
        sender.sendMessage("§7§l▪ Economic Settings:");
        sender.sendMessage("  §e• price-adjustment-rate: §f" + plugin.getConfigManager().getConfig().getDouble("price-adjustment-rate"));
        sender.sendMessage("  §e• min-price: §f$" + plugin.getConfigManager().getConfig().getDouble("min-price"));
        sender.sendMessage("  §e• max-price: §f$" + plugin.getConfigManager().getConfig().getDouble("max-price"));
        sender.sendMessage("  §e• price-decay-rate: §f" + plugin.getConfigManager().getConfig().getDouble("price-decay-rate"));
        sender.sendMessage("  §e• demand-multiplier: §f" + plugin.getConfigManager().getConfig().getDouble("demand-multiplier"));
        sender.sendMessage("  §e• supply-multiplier: §f" + plugin.getConfigManager().getConfig().getDouble("supply-multiplier"));
        
        // Stock settings
        sender.sendMessage("");
        sender.sendMessage("§7§l▪ Stock Settings:");
        sender.sendMessage("  §e• default-stock: §f" + plugin.getConfigManager().getConfig().getInt("default-stock"));
        sender.sendMessage("  §e• max-stock: §f" + plugin.getConfigManager().getConfig().getInt("max-stock"));
        
        // System settings
        sender.sendMessage("");
        sender.sendMessage("§7§l▪ System Settings:");
        sender.sendMessage("  §e• auto-save-interval: §f" + plugin.getConfigManager().getConfig().getInt("auto-save-interval") + " minutes");
        sender.sendMessage("  §e• confirmation-threshold: §f$" + plugin.getConfigManager().getConfig().getDouble("confirmation-threshold"));
        sender.sendMessage("  §e• starting-money: §f$" + plugin.getConfigManager().getConfig().getDouble("starting-money"));
        
        // GUI settings
        sender.sendMessage("");
        sender.sendMessage("§7§l▪ GUI Settings:");
        sender.sendMessage("  §e• market-title: §f" + plugin.getConfigManager().getConfig().getString("gui.market-title"));
        sender.sendMessage("  §e• category-title: §f" + plugin.getConfigManager().getConfig().getString("gui.category-title"));
        sender.sendMessage("  §e• sell-title: §f" + plugin.getConfigManager().getConfig().getString("gui.sell-title"));
        
        sender.sendMessage("§c§l===============================");
        sender.sendMessage("§7Use §e/marketsettings <setting> <value> §7to change settings.");
        sender.sendMessage("");
    }
    
    private void showSetting(CommandSender sender, String setting) {
        Object value = getSettingValue(setting);
        
        if (value == null) {
            sender.sendMessage("§cUnknown setting: " + setting);
            sender.sendMessage("§7Use §e/marketsettings help §7to see available settings.");
            return;
        }
        
        String description = getSettingDescription(setting);
        String formattedValue = formatSettingValue(setting, value);
        
        sender.sendMessage("");
        sender.sendMessage("§c§l=== SETTING INFO ===");
        sender.sendMessage("§7▪ §eSetting: §f" + setting);
        sender.sendMessage("§7▪ §eValue: §f" + formattedValue);
        sender.sendMessage("§7▪ §eDescription: §f" + description);
        sender.sendMessage("§c§l====================");
        sender.sendMessage("");
    }
    
    private void setSetting(CommandSender sender, String setting, String value) {
        Object oldValue = getSettingValue(setting);
        
        if (oldValue == null) {
            sender.sendMessage("§cUnknown setting: " + setting);
            sender.sendMessage("§7Use §e/marketsettings help §7to see available settings.");
            return;
        }
        
        // Validate and set the new value
        if (!validateAndSetSetting(sender, setting, value)) {
            return;
        }
        
        // Save configuration
        plugin.getConfigManager().saveConfig();
        
        // Get new value for confirmation
        Object newValue = getSettingValue(setting);
        String formattedOldValue = formatSettingValue(setting, oldValue);
        String formattedNewValue = formatSettingValue(setting, newValue);
        
        sender.sendMessage("§a§lSetting updated successfully!");
        sender.sendMessage("§7▪ §eSetting: §f" + setting);
        sender.sendMessage("§7▪ §eOld Value: §c" + formattedOldValue);
        sender.sendMessage("§7▪ §eNew Value: §a" + formattedNewValue);
        
        // Special handling for certain settings
        handleSpecialSettings(sender, setting);
        
        plugin.getLogger().info("Setting '" + setting + "' changed from '" + oldValue + "' to '" + newValue + "' by " + sender.getName());
    }
    
    private Object getSettingValue(String setting) {
        switch (setting.toLowerCase()) {
            case "price-adjustment-rate":
                return plugin.getConfigManager().getConfig().getDouble("price-adjustment-rate");
            case "min-price":
                return plugin.getConfigManager().getConfig().getDouble("min-price");
            case "max-price":
                return plugin.getConfigManager().getConfig().getDouble("max-price");
            case "auto-save-interval":
                return plugin.getConfigManager().getConfig().getInt("auto-save-interval");
            case "default-stock":
                return plugin.getConfigManager().getConfig().getInt("default-stock");
            case "max-stock":
                return plugin.getConfigManager().getConfig().getInt("max-stock");
            case "price-decay-rate":
                return plugin.getConfigManager().getConfig().getDouble("price-decay-rate");
            case "demand-multiplier":
                return plugin.getConfigManager().getConfig().getDouble("demand-multiplier");
            case "supply-multiplier":
                return plugin.getConfigManager().getConfig().getDouble("supply-multiplier");
            case "confirmation-threshold":
                return plugin.getConfigManager().getConfig().getDouble("confirmation-threshold");
            case "starting-money":
                return plugin.getConfigManager().getConfig().getDouble("starting-money");
            case "market-title":
                return plugin.getConfigManager().getConfig().getString("gui.market-title");
            case "category-title":
                return plugin.getConfigManager().getConfig().getString("gui.category-title");
            case "sell-title":
                return plugin.getConfigManager().getConfig().getString("gui.sell-title");
            default:
                return null;
        }
    }
    
    private String getSettingDescription(String setting) {
        switch (setting.toLowerCase()) {
            case "price-adjustment-rate":
                return "Percentage by which prices change per trade (0.1-10.0)";
            case "min-price":
                return "Minimum price any item can have (0.01-100.0)";
            case "max-price":
                return "Maximum price any item can have (100.0-10000.0)";
            case "auto-save-interval":
                return "How often market data is automatically saved in minutes (1-60)";
            case "default-stock":
                return "Default stock amount for newly added items (1-1000)";
            case "max-stock":
                return "Maximum stock any item can have (100-10000)";
            case "price-decay-rate":
                return "Rate at which prices decay towards base price per hour (0.0-1.0)";
            case "demand-multiplier":
                return "How much demand affects price increases (0.5-3.0)";
            case "supply-multiplier":
                return "How much supply affects price decreases (0.5-3.0)";
            case "confirmation-threshold":
                return "Price threshold above which confirmation GUI is shown";
            case "starting-money":
                return "Amount of money new players start with";
            case "market-title":
                return "Title displayed in the main market GUI";
            case "category-title":
                return "Title format for category GUIs ({category} placeholder)";
            case "sell-title":
                return "Title displayed in the sell GUI";
            default:
                return "Unknown setting";
        }
    }
    
    private String formatSettingValue(String setting, Object value) {
        switch (setting.toLowerCase()) {
            case "price-adjustment-rate":
                return value + "%";
            case "min-price":
            case "max-price":
            case "confirmation-threshold":
            case "starting-money":
                return "$" + value;
            case "auto-save-interval":
                return value + " minutes";
            default:
                return value.toString();
        }
    }
    
    private boolean validateAndSetSetting(CommandSender sender, String setting, String value) {
        try {
            switch (setting.toLowerCase()) {
                case "price-adjustment-rate":
                    double adjustmentRate = Double.parseDouble(value);
                    if (adjustmentRate < 0.1 || adjustmentRate > 10.0) {
                        sender.sendMessage("§cPrice adjustment rate must be between 0.1 and 10.0!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("price-adjustment-rate", adjustmentRate);
                    return true;
                    
                case "min-price":
                    double minPrice = Double.parseDouble(value);
                    if (minPrice < 0.01 || minPrice > 100.0) {
                        sender.sendMessage("§cMinimum price must be between 0.01 and 100.0!");
                        return false;
                    }
                    double currentMaxPrice = plugin.getConfigManager().getConfig().getDouble("max-price");
                    if (minPrice >= currentMaxPrice) {
                        sender.sendMessage("§cMinimum price must be less than maximum price ($" + currentMaxPrice + ")!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("min-price", minPrice);
                    return true;
                    
                case "max-price":
                    double maxPrice = Double.parseDouble(value);
                    if (maxPrice < 100.0 || maxPrice > 10000.0) {
                        sender.sendMessage("§cMaximum price must be between 100.0 and 10000.0!");
                        return false;
                    }
                    double currentMinPrice = plugin.getConfigManager().getConfig().getDouble("min-price");
                    if (maxPrice <= currentMinPrice) {
                        sender.sendMessage("§cMaximum price must be greater than minimum price ($" + currentMinPrice + ")!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("max-price", maxPrice);
                    return true;
                    
                case "auto-save-interval":
                    int saveInterval = Integer.parseInt(value);
                    if (saveInterval < 1 || saveInterval > 60) {
                        sender.sendMessage("§cAuto-save interval must be between 1 and 60 minutes!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("auto-save-interval", saveInterval);
                    return true;
                    
                case "default-stock":
                    int defaultStock = Integer.parseInt(value);
                    if (defaultStock < 1 || defaultStock > 1000) {
                        sender.sendMessage("§cDefault stock must be between 1 and 1000!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("default-stock", defaultStock);
                    return true;
                    
                case "max-stock":
                    int maxStock = Integer.parseInt(value);
                    if (maxStock < 100 || maxStock > 10000) {
                        sender.sendMessage("§cMaximum stock must be between 100 and 10000!");
                        return false;
                    }
                    int currentDefaultStock = plugin.getConfigManager().getConfig().getInt("default-stock");
                    if (maxStock < currentDefaultStock) {
                        sender.sendMessage("§cMaximum stock must be greater than or equal to default stock (" + currentDefaultStock + ")!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("max-stock", maxStock);
                    return true;
                    
                case "price-decay-rate":
                    double decayRate = Double.parseDouble(value);
                    if (decayRate < 0.0 || decayRate > 1.0) {
                        sender.sendMessage("§cPrice decay rate must be between 0.0 and 1.0!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("price-decay-rate", decayRate);
                    return true;
                    
                case "demand-multiplier":
                    double demandMultiplier = Double.parseDouble(value);
                    if (demandMultiplier < 0.5 || demandMultiplier > 3.0) {
                        sender.sendMessage("§cDemand multiplier must be between 0.5 and 3.0!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("demand-multiplier", demandMultiplier);
                    return true;
                    
                case "supply-multiplier":
                    double supplyMultiplier = Double.parseDouble(value);
                    if (supplyMultiplier < 0.5 || supplyMultiplier > 3.0) {
                        sender.sendMessage("§cSupply multiplier must be between 0.5 and 3.0!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("supply-multiplier", supplyMultiplier);
                    return true;
                    
                case "confirmation-threshold":
                    double threshold = Double.parseDouble(value);
                    if (threshold < 0) {
                        sender.sendMessage("§cConfirmation threshold cannot be negative!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("confirmation-threshold", threshold);
                    return true;
                    
                case "starting-money":
                    double startingMoney = Double.parseDouble(value);
                    if (startingMoney < 0) {
                        sender.sendMessage("§cStarting money cannot be negative!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("starting-money", startingMoney);
                    return true;
                    
                case "market-title":
                    if (value.trim().isEmpty()) {
                        sender.sendMessage("§cMarket title cannot be empty!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("gui.market-title", value.replace("&", "§"));
                    return true;
                    
                case "category-title":
                    if (value.trim().isEmpty()) {
                        sender.sendMessage("§cCategory title cannot be empty!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("gui.category-title", value.replace("&", "§"));
                    return true;
                    
                case "sell-title":
                    if (value.trim().isEmpty()) {
                        sender.sendMessage("§cSell title cannot be empty!");
                        return false;
                    }
                    plugin.getConfigManager().getConfig().set("gui.sell-title", value.replace("&", "§"));
                    return true;
                    
                default:
                    sender.sendMessage("§cUnknown setting: " + setting);
                    return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number format: " + value);
            return false;
        }
    }
    
    private void handleSpecialSettings(CommandSender sender, String setting) {
        switch (setting.toLowerCase()) {
            case "auto-save-interval":
                sender.sendMessage("§7§lNote: §7Auto-save scheduler will be restarted with new interval.");
                // Restart scheduler with new interval
                if (plugin.getMarketScheduler() != null) {
                    plugin.getMarketScheduler().stopTasks();
                    plugin.getMarketScheduler().startTasks();
                }
                break;
                
            case "price-adjustment-rate":
            case "min-price":
            case "max-price":
            case "price-decay-rate":
            case "demand-multiplier":
            case "supply-multiplier":
                sender.sendMessage("§7§lNote: §7Price changes will take effect on next trade.");
                break;
                
            case "market-title":
            case "category-title":
            case "sell-title":
                sender.sendMessage("§7§lNote: §7GUI title changes will be visible when GUIs are reopened.");
                break;
        }
    }
}