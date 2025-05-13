package com.example.economia.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MarketSettingsCommand implements CommandExecutor {
    private final Plugin plugin;

    public MarketSettingsCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("market.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /marketsettings <setting> <value>");
            sender.sendMessage(ChatColor.YELLOW + "Available settings: min_price, max_price, price_change_factor");
            return true;
        }

        String setting = args[1];
        double value;
        try {
            value = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Please enter a valid number!");
            return true;
        }

        switch (setting.toLowerCase()) {
            case "min_price":
                plugin.getConfig().set("market_settings.min_price", value);
                break;
            case "max_price":
                plugin.getConfig().set("market_settings.max_price", value);
                break;
            case "price_change_factor":
                plugin.getConfig().set("market_settings.price_change_factor", value);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid setting! Available settings: min_price, max_price, price_change_factor");
                return true;
        }

        plugin.saveConfig();
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Market setting " + setting + " has been updated to " + value);
        return true;
    }
}