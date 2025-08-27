package com.dynamicmarket.commands;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.gui.SellGUI;
import com.dynamicmarket.managers.SellHandler;
import com.dynamicmarket.data.MarketItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellCommand implements CommandExecutor {
    
    private final DynamicMarket plugin;
    
    public SellCommand(DynamicMarket plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check permission
        if (!player.hasPermission("market.sell")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }
        
        // Check if Vault economy is available
        if (plugin.getVaultEconomy() == null) {
            player.sendMessage("§cEconomy system is not available! Please contact an administrator.");
            return true;
        }
        
        // Handle different arguments
        if (args.length == 0) {
            // Open sell GUI
            openSellGUI(player);
            return true;
        }
        
        // Handle subcommands
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
            case "?":
                sendHelpMessage(player);
                break;
                
            case "hand":
            case "item":
                sellItemInHand(player, args);
                break;
                
            case "all":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /sell all <item_type>");
                    return true;
                }
                sellAllOfType(player, args[1]);
                break;
                
            case "inventory":
            case "inv":
                sellAllSellableItems(player);
                break;
                
            case "price":
            case "value":
                if (args.length < 2) {
                    showHandItemValue(player);
                } else {
                    showItemValue(player, args[1]);
                }
                break;
                
            case "list":
                listSellableItems(player);
                break;
                
            default:
                // Try to parse as item type and amount
                parseAndSellItem(player, args);
                break;
        }
        
        return true;
    }
    
    private void openSellGUI(Player player) {
        try {
            SellGUI sellGUI = new SellGUI(plugin, player);
            sellGUI.open();
            
        } catch (Exception e) {
            player.sendMessage("§cFailed to open sell GUI! Please try again or contact an administrator.");
            plugin.getLogger().severe("Failed to open sell GUI for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l=== SELL HELP ===");
        player.sendMessage("§e/sell §7- Open the sell GUI");
        player.sendMessage("§e/sell help §7- Show this help message");
        player.sendMessage("§e/sell hand [amount] §7- Sell item in your hand");
        player.sendMessage("§e/sell all <item> §7- Sell all items of a type");
        player.sendMessage("§e/sell inventory §7- Sell all sellable items");
        player.sendMessage("§e/sell price [item] §7- Check sell value");
        player.sendMessage("§e/sell list §7- List all sellable items");
        player.sendMessage("");
        player.sendMessage("§7Examples:");
        player.sendMessage("§7▪ §e/sell hand 32 §7- Sell 32 of the item in hand");
        player.sendMessage("§7▪ §e/sell all diamond §7- Sell all diamonds");
        player.sendMessage("§7▪ §e/sell price §7- Check value of item in hand");
        player.sendMessage("");
        player.sendMessage("§7§oTip: Only items that exist in the market can be sold!");
        player.sendMessage("§6§l================");
        player.sendMessage("");
    }
    
    private void sellItemInHand(Player player, String[] args) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        
        if (handItem == null || handItem.getType() == Material.AIR) {
            player.sendMessage("§cYou must be holding an item to sell!");
            return;
        }
        
        // Find market item
        MarketItem marketItem = plugin.getMarketManager().findItemByMaterial(handItem.getType());
        if (marketItem == null) {
            player.sendMessage("§cThis item cannot be sold to the market!");
            player.sendMessage("§7Only items that exist in the market can be sold.");
            return;
        }
        
        // Determine amount to sell
        int amount = handItem.getAmount();
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0) {
                    player.sendMessage("§cAmount must be greater than 0!");
                    return;
                }
                if (amount > handItem.getAmount()) {
                    player.sendMessage("§cYou only have " + handItem.getAmount() + " of this item!");
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid amount: " + args[1]);
                return;
            }
        }
        
        // Process sale
        SellHandler sellHandler = new SellHandler(plugin);
        sellHandler.processSale(player, marketItem, amount);
    }
    
    private void sellAllOfType(Player player, String itemType) {
        Material material;
        try {
            material = Material.valueOf(itemType.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid item type: " + itemType);
            return;
        }
        
        MarketItem marketItem = plugin.getMarketManager().findItemByMaterial(material);
        if (marketItem == null) {
            player.sendMessage("§cThis item type cannot be sold to the market!");
            return;
        }
        
        SellHandler sellHandler = new SellHandler(plugin);
        int maxAmount = sellHandler.getMaxSellableAmount(player, marketItem);
        
        if (maxAmount <= 0) {
            player.sendMessage("§cYou don't have any " + itemType.toLowerCase() + " to sell!");
            return;
        }
        
        sellHandler.processSale(player, marketItem, maxAmount);
    }
    
    private void sellAllSellableItems(Player player) {
        SellHandler sellHandler = new SellHandler(plugin);
        double totalEarned = 0.0;
        int totalItemsSold = 0;
        int itemTypesSold = 0;
        
        // Get all unique materials in player inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            
            MarketItem marketItem = plugin.getMarketManager().findItemByMaterial(item.getType());
            if (marketItem != null) {
                int maxAmount = sellHandler.getMaxSellableAmount(player, marketItem);
                if (maxAmount > 0) {
                    double earned = sellHandler.processSaleQuiet(player, marketItem, maxAmount);
                    if (earned > 0) {
                        totalEarned += earned;
                        totalItemsSold += maxAmount;
                        itemTypesSold++;
                    }
                }
            }
        }
        
        if (totalEarned > 0) {
            player.sendMessage("§a§lSold all sellable items!");
            player.sendMessage("§7▪ §eItem types sold: §f" + itemTypesSold);
            player.sendMessage("§7▪ §eTotal items sold: §f" + totalItemsSold);
            player.sendMessage("§7▪ §eTotal earned: §a$" + String.format("%.2f", totalEarned));
        } else {
            player.sendMessage("§cYou don't have any sellable items!");
            player.sendMessage("§7Only items that exist in the market can be sold.");
        }
    }
    
    private void showHandItemValue(Player player) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        
        if (handItem == null || handItem.getType() == Material.AIR) {
            player.sendMessage("§cYou must be holding an item to check its value!");
            return;
        }
        
        showItemValueDetails(player, handItem);
    }
    
    private void showItemValue(Player player, String itemType) {
        Material material;
        try {
            material = Material.valueOf(itemType.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid item type: " + itemType);
            return;
        }
        
        ItemStack item = new ItemStack(material, 1);
        showItemValueDetails(player, item);
    }
    
    private void showItemValueDetails(Player player, ItemStack item) {
        MarketItem marketItem = plugin.getMarketManager().findItemByMaterial(item.getType());
        
        if (marketItem == null) {
            player.sendMessage("§cThis item cannot be sold to the market!");
            return;
        }
        
        SellHandler sellHandler = new SellHandler(plugin);
        int playerAmount = sellHandler.getMaxSellableAmount(player, marketItem);
        
        String itemName = marketItem.getDisplayName() != null ? 
            marketItem.getDisplayName() : 
            item.getType().name().toLowerCase().replace("_", " ");
        
        player.sendMessage("");
        player.sendMessage("§6§l=== ITEM VALUE: " + itemName.toUpperCase() + " ===");
        player.sendMessage("§7▪ §eSell price: §a$" + String.format("%.2f", marketItem.getSellPrice()) + " each");
        player.sendMessage("§7▪ §eYou have: §f" + playerAmount + " items");
        
        if (playerAmount > 0) {
            double totalValue = marketItem.getSellPrice() * playerAmount;
            player.sendMessage("§7▪ §eTotal value: §a$" + String.format("%.2f", totalValue));
            
            // Show different quantity values
            player.sendMessage("");
            player.sendMessage("§7§l▪ Sell Values:");
            player.sendMessage("§7  1x: §a$" + String.format("%.2f", marketItem.getSellPrice()));
            if (playerAmount >= 16) {
                player.sendMessage("§7  16x: §a$" + String.format("%.2f", marketItem.getSellPrice() * 16));
            }
            if (playerAmount >= 64) {
                player.sendMessage("§7  64x: §a$" + String.format("%.2f", marketItem.getSellPrice() * 64));
            }
            player.sendMessage("§7  All (" + playerAmount + "x): §a$" + String.format("%.2f", totalValue));
        } else {
            player.sendMessage("§7▪ §cYou don't have any of this item!");
        }
        
        player.sendMessage("§6§l" + "=".repeat(15 + itemName.length()));
        player.sendMessage("");
    }
    
    private void listSellableItems(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l=== YOUR SELLABLE ITEMS ===");
        
        SellHandler sellHandler = new SellHandler(plugin);
        boolean foundAny = false;
        double totalValue = 0.0;
        
        // Check each unique item type in inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            
            MarketItem marketItem = plugin.getMarketManager().findItemByMaterial(item.getType());
            if (marketItem != null) {
                int amount = sellHandler.getMaxSellableAmount(player, marketItem);
                if (amount > 0) {
                    foundAny = true;
                    double itemValue = marketItem.getSellPrice() * amount;
                    totalValue += itemValue;
                    
                    String itemName = marketItem.getDisplayName() != null ? 
                        marketItem.getDisplayName() : 
                        item.getType().name().toLowerCase().replace("_", " ");
                    
                    player.sendMessage("§7▪ §e" + itemName + " §7x" + amount + " = §a$" + String.format("%.2f", itemValue));
                }
            }
        }
        
        if (!foundAny) {
            player.sendMessage("§cYou don't have any sellable items!");
            player.sendMessage("§7Only items that exist in the market can be sold.");
        } else {
            player.sendMessage("");
            player.sendMessage("§7▪ §eTotal value: §a$" + String.format("%.2f", totalValue));
            player.sendMessage("§7Use §e/sell inventory §7to sell everything at once!");
        }
        
        player.sendMessage("§6§l=========================");
        player.sendMessage("");
    }
    
    private void parseAndSellItem(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /sell <item_type> <amount>");
            return;
        }
        
        // Parse item type
        Material material;
        try {
            material = Material.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid item type: " + args[0]);
            return;
        }
        
        // Parse amount
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                player.sendMessage("§cAmount must be greater than 0!");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount: " + args[1]);
            return;
        }
        
        // Find market item
        MarketItem marketItem = plugin.getMarketManager().findItemByMaterial(material);
        if (marketItem == null) {
            player.sendMessage("§cThis item type cannot be sold to the market!");
            return;
        }
        
        // Check if player has enough
        SellHandler sellHandler = new SellHandler(plugin);
        if (!sellHandler.hasEnoughItems(player, marketItem, amount)) {
            int available = sellHandler.getMaxSellableAmount(player, marketItem);
            player.sendMessage("§cYou don't have enough of this item!");
            player.sendMessage("§7You have: " + available + ", trying to sell: " + amount);
            return;
        }
        
        // Process sale
        sellHandler.processSale(player, marketItem, amount);
    }
}