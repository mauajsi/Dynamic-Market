package com.dynamicmarket.commands;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class SetSlotCommand implements CommandExecutor {

    private final DynamicMarket plugin;

    public SetSlotCommand(DynamicMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check admin permission
        if (!sender.hasPermission("market.admin.slot")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            showSlotUsage(sender);
            return true;
        }

        if (args.length == 1) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("help") || subCommand.equals("?")) {
                sendHelpMessage(sender);
                return true;
            }

            if (subCommand.equals("list") || subCommand.equals("show")) {
                showAllSlots(sender);
                return true;
            }

            if (subCommand.equals("auto") || subCommand.equals("autoassign")) {
                autoAssignSlots(sender);
                return true;
            }

            // Show specific category slot
            showCategorySlot(sender, args[0]);
            return true;
        }

        if (args.length >= 2) {
            String categoryName = args[0];
            String slotStr = args[1];

            setSlot(sender, categoryName, slotStr);
            return true;
        }

        return true;
    }

    private void showSlotUsage(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§c§l=== SETSLOT COMMAND ===");
        sender.sendMessage("§e/setslot <category> <slot> §7- Set category GUI slot");
        sender.sendMessage("§e/setslot <category> §7- Show current slot for category");
        sender.sendMessage("§e/setslot list §7- Show all category slots");
        sender.sendMessage("§e/setslot auto §7- Auto-assign slots to all categories");
        sender.sendMessage("§e/setslot help §7- Show detailed help");
        sender.sendMessage("§c§l======================");
        sender.sendMessage("");
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§c§l=== SETSLOT HELP ===");
        sender.sendMessage("§7This command manages GUI slot positions for market categories.");
        sender.sendMessage("");
        sender.sendMessage("§eCommands:");
        sender.sendMessage("§7▪ §e/setslot <category> <slot> §7- Set specific slot for category");
        sender.sendMessage("§7▪ §e/setslot <category> §7- Show current slot for category");
        sender.sendMessage("§7▪ §e/setslot list §7- List all categories and their slots");
        sender.sendMessage("§7▪ §e/setslot auto §7- Automatically assign slots to all categories");
        sender.sendMessage("");
        sender.sendMessage("§eSlot Information:");
        sender.sendMessage("§7▪ Valid slots: §f0-53 §7(GUI inventory slots)");
        sender.sendMessage("§7▪ Recommended slots: §f10-16, 19-25, 28-34, 37-43 §7(center area)");
        sender.sendMessage("§7▪ Avoid slots: §f0-8, 9, 17, 18, 26, 27, 35, 36, 44, 45-53 §7(borders)");
        sender.sendMessage("");
        sender.sendMessage("§eGUI Layout (54 slots):");
        sender.sendMessage("§7▪ Row 1: §f0-8 §7(top border)");
        sender.sendMessage("§7▪ Row 2: §f9-17 §7(9=border, 10-16=usable, 17=border)");
        sender.sendMessage("§7▪ Row 3: §f18-26 §7(18=border, 19-25=usable, 26=border)");
        sender.sendMessage("§7▪ Row 4: §f27-35 §7(27=border, 28-34=usable, 35=border)");
        sender.sendMessage("§7▪ Row 5: §f36-44 §7(36=border, 37-43=usable, 44=border)");
        sender.sendMessage("§7▪ Row 6: §f45-53 §7(bottom border)");
        sender.sendMessage("");
        sender.sendMessage("§eExamples:");
        sender.sendMessage("§7▪ §e/setslot Blocks 10 §7- Set Blocks category to slot 10");
        sender.sendMessage("§7▪ §e/setslot Tools 11 §7- Set Tools category to slot 11");
        sender.sendMessage("§7▪ §e/setslot Food 12 §7- Set Food category to slot 12");
        sender.sendMessage("§c§l====================");
        sender.sendMessage("");
    }

    private void showAllSlots(CommandSender sender) {
Map<String, MarketCategory> categories = new HashMap<>(plugin.getMarketManager().getAllCategories().stream()
    .collect(java.util.stream.Collectors.toMap(MarketCategory::getName, category -> category)));

        if (categories.isEmpty()) {
            sender.sendMessage("§cNo categories found!");
            return;
        }

        sender.sendMessage("");
        sender.sendMessage("§c§l=== CATEGORY SLOTS ===");

        // Show categories sorted by slot
        categories.values().stream()
                .sorted((a, b) -> Integer.compare(a.getGuiSlot(), b.getGuiSlot()))
                .forEach(category -> {
                    String slotStatus = isValidSlot(category.getGuiSlot()) ? "§a" : "§c";
                    String slotInfo = slotStatus + category.getGuiSlot();

                    if (!isValidSlot(category.getGuiSlot())) {
                        slotInfo += " §7(INVALID)";
                    } else if (isRecommendedSlot(category.getGuiSlot())) {
                        slotInfo += " §7(RECOMMENDED)";
                    } else if (isBorderSlot(category.getGuiSlot())) {
                        slotInfo += " §7(BORDER)";
                    }

                    sender.sendMessage("§7▪ §e" + category.getName() + ": §7Slot " + slotInfo);
                });

        // Check for slot conflicts
        checkSlotConflicts(sender, categories);

        sender.sendMessage("§c§l======================");
        sender.sendMessage("§7Use §e/setslot <category> <slot> §7to change slots.");
        sender.sendMessage("");
    }

    private void showCategorySlot(CommandSender sender, String categoryName) {
        if (!plugin.getMarketManager().categoryExists(categoryName)) {
            sender.sendMessage("§cCategory '" + categoryName + "' does not exist!");
            return;
        }

        MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
        int slot = category.getGuiSlot();

        sender.sendMessage("");
        sender.sendMessage("§c§l=== CATEGORY SLOT INFO ===");
        sender.sendMessage("§7▪ §eCategory: §f" + categoryName);
        sender.sendMessage("§7▪ §eCurrent Slot: §f" + slot);

        if (!isValidSlot(slot)) {
            sender.sendMessage("§7▪ §eStatus: §cINVALID SLOT");
            sender.sendMessage("§7▪ §eRecommendation: §7Use slot 0-53");
        } else if (isRecommendedSlot(slot)) {
            sender.sendMessage("§7▪ §eStatus: §aRECOMMENDED");
        } else if (isBorderSlot(slot)) {
            sender.sendMessage("§7▪ §eStatus: §eBORDER SLOT");
            sender.sendMessage("§7▪ §eRecommendation: §7Consider using center slots (10-16, 19-25, 28-34, 37-43)");
        } else {
            sender.sendMessage("§7▪ §eStatus: §aVALID");
        }

        int row = slot / 9;
        int col = slot % 9;
        sender.sendMessage("§7▪ §ePosition: §fRow " + (row + 1) + ", Column " + (col + 1));

        // Check for conflicts
        checkSingleSlotConflict(sender, categoryName, slot);

        sender.sendMessage("§c§l=========================");
        sender.sendMessage("");
    }

    private void setSlot(CommandSender sender, String categoryName, String slotStr) {
        if (!plugin.getMarketManager().categoryExists(categoryName)) {
            sender.sendMessage("§cCategory '" + categoryName + "' does not exist!");
            return;
        }

        int slot;
        try {
            slot = Integer.parseInt(slotStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid slot number: " + slotStr);
            sender.sendMessage("§7Slot must be a number between 0 and 53.");
            return;
        }

        if (!isValidSlot(slot)) {
            sender.sendMessage("§cInvalid slot: " + slot);
            sender.sendMessage("§7Slot must be between 0 and 53.");
            return;
        }

        String conflictCategory = findSlotConflict(categoryName, slot);
        if (conflictCategory != null) {
            sender.sendMessage("§c§lWARNING: Slot " + slot + " is already used by category '" + conflictCategory + "'!");
            sender.sendMessage("§7Consider using a different slot.");
        }

        MarketCategory category = plugin.getMarketManager().getCategory(categoryName);
        int oldSlot = category.getGuiSlot();

        category.setGuiSlot(slot);
        plugin.getMarketManager().saveMarketData();

        sender.sendMessage("§a§lSlot updated successfully!");
        sender.sendMessage("§7▪ §eCategory: §f" + categoryName);
        sender.sendMessage("§7▪ §eOld Slot: §c" + oldSlot);
        sender.sendMessage("§7▪ §eNew Slot: §a" + slot);
    }

    private void autoAssignSlots(CommandSender sender) {
Map<String, MarketCategory> categories = new HashMap<>(plugin.getMarketManager().getAllCategories().stream()
        .collect(java.util.stream.Collectors.toMap(MarketCategory::getName, category -> category)));
        int slot = 10;

        for (MarketCategory category : categories.values()) {
            category.setGuiSlot(slot);
            slot++;
            if (slot % 9 == 0) slot++; // skip borders
        }

        plugin.getMarketManager().saveMarketData();
        sender.sendMessage("§aAll slots have been automatically assigned!");
        showAllSlots(sender);
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot <= 53;
    }

    private boolean isRecommendedSlot(int slot) {
        return (slot >= 10 && slot <= 16) || (slot >= 19 && slot <= 25) ||
               (slot >= 28 && slot <= 34) || (slot >= 37 && slot <= 43);
    }

    private boolean isBorderSlot(int slot) {
        return slot == 0 || slot == 8 || slot == 9 || slot == 17 || slot == 18 ||
               slot == 26 || slot == 27 || slot == 35 || slot == 36 || slot == 44 ||
               (slot >= 45 && slot <= 53);
    }

    private String findSlotConflict(String excludeCategory, int slot) {
        for (MarketCategory category : plugin.getMarketManager().getAllCategories()) {
            String categoryName = category.getName();
            // This line appears to be referencing a non-existent entry variable
            // Remove or replace with actual category value

            if (!categoryName.equals(excludeCategory) && category.getGuiSlot() == slot) {
                return categoryName;
            }
        }
        return null;
    }

    private void checkSlotConflicts(CommandSender sender, Map<String, MarketCategory> categories) {
        boolean hasConflicts = false;

        for (Map.Entry<String, MarketCategory> entry1 : categories.entrySet()) {
            for (Map.Entry<String, MarketCategory> entry2 : categories.entrySet()) {
                if (!entry1.getKey().equals(entry2.getKey()) &&
                    entry1.getValue().getGuiSlot() == entry2.getValue().getGuiSlot()) {

                    if (!hasConflicts) {
                        sender.sendMessage("");
                        sender.sendMessage("§c§lSLOT CONFLICTS DETECTED:");
                        hasConflicts = true;
                    }

                    sender.sendMessage("§7▪ §cSlot " + entry1.getValue().getGuiSlot() + ": §e" +
                                       entry1.getKey() + " §7and §e" + entry2.getKey());
                }
            }
        }

        if (hasConflicts) {
            sender.sendMessage("§7Use §e/setslot auto §7to automatically resolve conflicts.");
        }
    }

    private void checkSingleSlotConflict(CommandSender sender, String categoryName, int slot) {
        String conflict = findSlotConflict(categoryName, slot);
        if (conflict != null) {
            sender.sendMessage("§c§lWARNING: This slot conflicts with category '" + conflict + "'!");
        }
    }
}
