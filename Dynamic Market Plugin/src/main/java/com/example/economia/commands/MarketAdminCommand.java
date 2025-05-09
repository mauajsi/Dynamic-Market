package com.example.economia.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import com.example.economia.MarketManager;
import org.bukkit.plugin.Plugin;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class MarketAdminCommand implements CommandExecutor, TabCompleter {
    private final MarketManager marketManager;
    private final Plugin plugin;

    public MarketAdminCommand(MarketManager marketManager, Plugin plugin) {
        this.marketManager = marketManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length < 1) {
            if (player.hasPermission("market.admin")) {
                sendHelp(player);
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "addcategory":
                if (!player.hasPermission("market.admin.category")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to add categories.");
                    return true;
                }
                handleAddCategory(player, args);
                break;

            case "add":
                if (!player.hasPermission("market.admin.add")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to add items.");
                    return true;
                }
                handleAddWithHeldItem(player, args);
                break;

            case "remove":
                if (!player.hasPermission("market.admin.remove")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to remove items.");
                    return true;
                }
                handleRemove(player, args);
                break;

            case "setprice":
                if (!player.hasPermission("market.admin.setprice")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to change prices.");
                    return true;
                }
                handleSetPrice(player, args);
                break;

            case "move":
                if (!player.hasPermission("market.admin.move")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to move items.");
                    return true;
                }
                handleMove(player, args);
                break;

            case "reset":
                if (!player.hasPermission("market.admin.reset")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to reset the market.");
                    return true;
                }
                handleReset(player);
                break;

            default:
                if (player.hasPermission("market.admin")) {
                    sendHelp(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                }
                break;
        }

        return true;
    }

    private void handleAddCategory(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /marketadmin addcategory <name> <icon>");
            return;
        }

        String categoryName = args[1];
        Material icon = Material.getMaterial(args[2].toUpperCase());
        if (icon == null) {
            player.sendMessage(ChatColor.RED + "Invalid icon material!");
            return;
        }

        if (marketManager.addCategory(categoryName, icon)) {
            player.sendMessage(ChatColor.GREEN + "Category added successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Category already exists or invalid name!");
        }
    }

    private void handleAddWithHeldItem(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /marketadmin add <category> <buy_price> [sell_price]");
            return;
        }

        // Reconstruct category name from args
        StringBuilder categoryBuilder = new StringBuilder();
        int i;
        for (i = 1; i < args.length; i++) {
            if (isNumeric(args[i])) {
                break;
            }
            if (categoryBuilder.length() > 0) {
                categoryBuilder.append(" ");
            }
            categoryBuilder.append(args[i]);
        }
        String category = categoryBuilder.toString();

        // Check if we found the price argument
        if (i >= args.length) {
            player.sendMessage(ChatColor.RED + "Usage: /marketadmin add <category> <buy_price> [sell_price]");
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item to add it to the market!");
            return;
        }

        try {
            double buyPrice = Double.parseDouble(args[i]);
            double sellPrice = (i + 1) < args.length ? Double.parseDouble(args[i + 1]) : buyPrice * 0.75;

            if (marketManager.addItem(category, heldItem.getType(), buyPrice, sellPrice)) {
                player.sendMessage(ChatColor.GREEN + "Item added successfully!");
            } else {
                player.sendMessage(ChatColor.RED + "Invalid category or item already exists!");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid price format.");
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void handleRemove(Player player, String[] args) {
        String category = args[1];
        Material material = Material.getMaterial(args[2].toUpperCase());
        if (material == null) {
            player.sendMessage(ChatColor.RED + "Invalid item: " + args[2]);
            return;
        }

        if (marketManager.removeItem(category, material)) {
            player.sendMessage(ChatColor.GREEN + "Item removed successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Item not found in that category.");
        }
    }

    private void handleSetPrice(Player player, String[] args) {
        String category = args[1];
        Material material = Material.getMaterial(args[2].toUpperCase());
        if (material == null) {
            player.sendMessage(ChatColor.RED + "Invalid item: " + args[2]);
            return;
        }

        try {
            double buyPrice = Double.parseDouble(args[3]);
            double sellPrice = args.length > 4 ? Double.parseDouble(args[4]) : buyPrice * 0.75;

            if (marketManager.updateItemPrices(category, material, buyPrice, sellPrice)) {
                player.sendMessage(ChatColor.GREEN + "Prices updated successfully!");
            } else {
                player.sendMessage(ChatColor.RED + "Item not found in that category.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid price format.");
        }
    }

    private void handleMove(Player player, String[] args) {
        String fromCategory = args[1];
        String toCategory = args[2];
        Material material = Material.getMaterial(args[3].toUpperCase());
        if (material == null) {
            player.sendMessage(ChatColor.RED + "Invalid item: " + args[3]);
            return;
        }

        if (marketManager.moveItem(fromCategory, toCategory, material)) {
            player.sendMessage(ChatColor.GREEN + "Item moved successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to move item. Check if categories exist and item is in the source category.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("add", "remove", "setprice", "move", "addcategory", "reset"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || 
                args[0].equalsIgnoreCase("remove") || 
                args[0].equalsIgnoreCase("setprice")) {
                completions.addAll(marketManager.getCategories());
            }
        }
        
        return completions;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Market Admin Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/marketadmin addcategory <name> <icon>");
        player.sendMessage(ChatColor.YELLOW + "/marketadmin add <category> <buy_price> [sell_price]");
        player.sendMessage(ChatColor.YELLOW + "/marketadmin remove <category> <item>");
        player.sendMessage(ChatColor.YELLOW + "/marketadmin setprice <category> <item> <buy_price> [sell_price]");
        player.sendMessage(ChatColor.YELLOW + "/marketadmin move <from_category> <to_category> <item>");
        player.sendMessage(ChatColor.YELLOW + "/marketadmin reset " + ChatColor.GRAY + "- Reset market to default");
    }

    private void handleReset(Player player) {
        if (marketManager.resetMarket()) {
            player.sendMessage(ChatColor.GREEN + "Market has been reset to default configuration!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to reset market. Check console for errors.");
        }
    }
}