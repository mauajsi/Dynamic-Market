package com.example.economia.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SetSlotCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;

    public SetSlotCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("market.admin.slot")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to change category slots!");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setslot <category> <slot>");
            sender.sendMessage(ChatColor.YELLOW + "Slot numbers: 0-53 (6 rows × 9 columns)");
            sender.sendMessage(ChatColor.GRAY + "Compatible with Minecraft 1.13+");
            return true;
        }

        String category = args[0];
        int slot;

        try {
            slot = Integer.parseInt(args[1]);
            if (slot < 0 || slot > 53) {
                sender.sendMessage(ChatColor.RED + "Slot must be between 0 and 53!");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Please enter a valid slot number!");
            return true;
        }

        ConfigurationSection categories = plugin.getConfig().getConfigurationSection("categories");
        if (categories == null || !categories.contains(category)) {
            sender.sendMessage(ChatColor.RED + "Category '" + category + "' not found!");
            return true;
        }

        // Set the slot for the category
        plugin.getConfig().set("categories." + category + ".slot", slot);
        plugin.saveConfig();
        plugin.reloadConfig();

        sender.sendMessage(ChatColor.GREEN + "Category '" + category + "' has been moved to slot " + slot);
        
        // Show row and column information
        int row = (slot / 9) + 1;
        int column = (slot % 9) + 1;
        sender.sendMessage(ChatColor.YELLOW + "Position: Row " + row + ", Column " + column);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("market.admin.slot")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            // First argument: return all category names
            ConfigurationSection categories = plugin.getConfig().getConfigurationSection("categories");
            if (categories != null) {
                return new ArrayList<>(categories.getKeys(false))
                    .stream()
                    .filter(category -> category.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            }
        } else if (args.length == 2) {
            // Second argument: suggest some common slot numbers (e.g., 10, 12, 14, 16)
            List<String> slots = new ArrayList<>();
            slots.add("10");
            slots.add("12");
            slots.add("14");
            slots.add("16");
            return slots.stream()
                .filter(slot -> slot.startsWith(args[1]))
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}