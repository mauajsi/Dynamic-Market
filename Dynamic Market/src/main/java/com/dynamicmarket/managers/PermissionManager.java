package com.dynamicmarket.managers;

import com.dynamicmarket.DynamicMarket;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.Map;

public class PermissionManager {
    
    private final DynamicMarket plugin;
    private final Map<String, Permission> permissions;
    
    // Permission constants
    public static final String MARKET_USE = "market.use";
    public static final String MARKET_SELL = "market.sell";
    public static final String MARKET_BUY = "market.buy";
    
    // Admin permissions
    public static final String ADMIN_BASE = "market.admin";
    public static final String ADMIN_CATEGORY = "market.admin.category";
    public static final String ADMIN_ADD = "market.admin.add";
    public static final String ADMIN_REMOVE = "market.admin.remove";
    public static final String ADMIN_SETPRICE = "market.admin.setprice";
    public static final String ADMIN_MOVE = "market.admin.move";
    public static final String ADMIN_RESET = "market.admin.reset";
    public static final String ADMIN_SLOT = "market.admin.slot";
    public static final String ADMIN_SETTINGS = "market.admin.settings";
    public static final String ADMIN_RELOAD = "market.admin.reload";
    public static final String ADMIN_BACKUP = "market.admin.backup";
    public static final String ADMIN_RESTORE = "market.admin.restore";
    
    // Special permissions
    public static final String BYPASS_COOLDOWN = "market.bypass.cooldown";
    public static final String BYPASS_LIMITS = "market.bypass.limits";
    public static final String VIP_PRICES = "market.vip.prices";
    public static final String NOTIFICATIONS = "market.notifications";
    
    public PermissionManager(DynamicMarket plugin) {
        this.plugin = plugin;
        this.permissions = new HashMap<>();
        registerPermissions();
    }
    
    /**
     * Register all plugin permissions
     */
    private void registerPermissions() {
        // Basic user permissions
        registerPermission(MARKET_USE, "Allows access to the market", PermissionDefault.TRUE);
        registerPermission(MARKET_SELL, "Allows selling items to the market", PermissionDefault.TRUE);
        registerPermission(MARKET_BUY, "Allows buying items from the market", PermissionDefault.TRUE);
        
        // Admin permissions
        registerPermission(ADMIN_BASE, "Base admin permission (includes all admin permissions)", PermissionDefault.OP);
        registerPermission(ADMIN_CATEGORY, "Allows creation and management of market categories", PermissionDefault.OP);
        registerPermission(ADMIN_ADD, "Permits adding new items to the market", PermissionDefault.OP);
        registerPermission(ADMIN_REMOVE, "Allows removal of items from any market category", PermissionDefault.OP);
        registerPermission(ADMIN_SETPRICE, "Enables price modifications for existing items", PermissionDefault.OP);
        registerPermission(ADMIN_MOVE, "Permits moving items between different categories", PermissionDefault.OP);
        registerPermission(ADMIN_RESET, "Allows resetting the entire market to default state", PermissionDefault.OP);
        registerPermission(ADMIN_SLOT, "Allows changing category positions in GUI", PermissionDefault.OP);
        registerPermission(ADMIN_SETTINGS, "Allows modifying market settings", PermissionDefault.OP);
        registerPermission(ADMIN_RELOAD, "Allows reloading market data and configuration", PermissionDefault.OP);
        registerPermission(ADMIN_BACKUP, "Allows creating market backups", PermissionDefault.OP);
        registerPermission(ADMIN_RESTORE, "Allows restoring market from backups", PermissionDefault.OP);
        
        // Special permissions
        registerPermission(BYPASS_COOLDOWN, "Bypasses transaction cooldowns", PermissionDefault.FALSE);
        registerPermission(BYPASS_LIMITS, "Bypasses purchase and sell limits", PermissionDefault.FALSE);
        registerPermission(VIP_PRICES, "Receives VIP pricing (discounts)", PermissionDefault.FALSE);
        registerPermission(NOTIFICATIONS, "Receives market notifications and updates", PermissionDefault.OP);
        
        plugin.getLogger().info("Registered " + permissions.size() + " permissions");
    }
    
    /**
     * Register a single permission
     * @param name permission name
     * @param description permission description
     * @param defaultValue default permission value
     */
    private void registerPermission(String name, String description, PermissionDefault defaultValue) {
        try {
            Permission permission = new Permission(name, description, defaultValue);
            
            // Add to server's permission manager if not already registered
            if (plugin.getServer().getPluginManager().getPermission(name) == null) {
                plugin.getServer().getPluginManager().addPermission(permission);
            }
            
            permissions.put(name, permission);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register permission '" + name + "': " + e.getMessage());
        }
    }
    
    /**
     * Check if player has a specific permission
     * @param player the player to check
     * @param permission the permission to check
     * @return true if player has permission
     */
    public boolean hasPermission(Player player, String permission) {
        if (player == null) {
            return false;
        }
        
        // Check base admin permission first
        if (!permission.equals(ADMIN_BASE) && permission.startsWith("market.admin.")) {
            if (player.hasPermission(ADMIN_BASE)) {
                return true;
            }
        }
        
        return player.hasPermission(permission);
    }
    
    /**
     * Check if player can use the market
     * @param player the player to check
     * @return true if player can use market
     */
    public boolean canUseMarket(Player player) {
        return hasPermission(player, MARKET_USE);
    }
    
    /**
     * Check if player can buy items
     * @param player the player to check
     * @return true if player can buy items
     */
    public boolean canBuyItems(Player player) {
        return hasPermission(player, MARKET_BUY) && canUseMarket(player);
    }
    
    /**
     * Check if player can sell items
     * @param player the player to check
     * @return true if player can sell items
     */
    public boolean canSellItems(Player player) {
        return hasPermission(player, MARKET_SELL) && canUseMarket(player);
    }
    
    /**
     * Check if player is an admin
     * @param player the player to check
     * @return true if player has admin permissions
     */
    public boolean isAdmin(Player player) {
        return hasPermission(player, ADMIN_BASE);
    }
    
    /**
     * Check if player can manage categories
     * @param player the player to check
     * @return true if player can manage categories
     */
    public boolean canManageCategories(Player player) {
        return hasPermission(player, ADMIN_CATEGORY);
    }
    
    /**
     * Check if player can add items to market
     * @param player the player to check
     * @return true if player can add items
     */
    public boolean canAddItems(Player player) {
        return hasPermission(player, ADMIN_ADD);
    }
    
    /**
     * Check if player can remove items from market
     * @param player the player to check
     * @return true if player can remove items
     */
    public boolean canRemoveItems(Player player) {
        return hasPermission(player, ADMIN_REMOVE);
    }
    
    /**
     * Check if player can set item prices
     * @param player the player to check
     * @return true if player can set prices
     */
    public boolean canSetPrices(Player player) {
        return hasPermission(player, ADMIN_SETPRICE);
    }
    
    /**
     * Check if player can move items between categories
     * @param player the player to check
     * @return true if player can move items
     */
    public boolean canMoveItems(Player player) {
        return hasPermission(player, ADMIN_MOVE);
    }
    
    /**
     * Check if player can reset the market
     * @param player the player to check
     * @return true if player can reset market
     */
    public boolean canResetMarket(Player player) {
        return hasPermission(player, ADMIN_RESET);
    }
    
    /**
     * Check if player can manage GUI slots
     * @param player the player to check
     * @return true if player can manage slots
     */
    public boolean canManageSlots(Player player) {
        return hasPermission(player, ADMIN_SLOT);
    }
    
    /**
     * Check if player can modify settings
     * @param player the player to check
     * @return true if player can modify settings
     */
    public boolean canModifySettings(Player player) {
        return hasPermission(player, ADMIN_SETTINGS);
    }
    
    /**
     * Check if player can reload market data
     * @param player the player to check
     * @return true if player can reload
     */
    public boolean canReload(Player player) {
        return hasPermission(player, ADMIN_RELOAD);
    }
    
    /**
     * Check if player can create backups
     * @param player the player to check
     * @return true if player can create backups
     */
    public boolean canCreateBackups(Player player) {
        return hasPermission(player, ADMIN_BACKUP);
    }
    
    /**
     * Check if player can restore from backups
     * @param player the player to check
     * @return true if player can restore
     */
    public boolean canRestoreBackups(Player player) {
        return hasPermission(player, ADMIN_RESTORE);
    }
    
    /**
     * Check if player can bypass cooldowns
     * @param player the player to check
     * @return true if player can bypass cooldowns
     */
    public boolean canBypassCooldown(Player player) {
        return hasPermission(player, BYPASS_COOLDOWN);
    }
    
    /**
     * Check if player can bypass limits
     * @param player the player to check
     * @return true if player can bypass limits
     */
    public boolean canBypassLimits(Player player) {
        return hasPermission(player, BYPASS_LIMITS);
    }
    
    /**
     * Check if player has VIP pricing
     * @param player the player to check
     * @return true if player has VIP pricing
     */
    public boolean hasVipPricing(Player player) {
        return hasPermission(player, VIP_PRICES);
    }
    
    /**
     * Check if player should receive notifications
     * @param player the player to check
     * @return true if player should receive notifications
     */
    public boolean shouldReceiveNotifications(Player player) {
        return hasPermission(player, NOTIFICATIONS);
    }
    
    /**
     * Get VIP discount percentage for player
     * @param player the player
     * @return discount percentage (0.0 to 1.0)
     */
    public double getVipDiscount(Player player) {
        if (!hasVipPricing(player)) {
            return 0.0;
        }
        
        // Check for specific VIP levels
        if (player.hasPermission("market.vip.diamond")) {
            return 0.15; // 15% discount
        } else if (player.hasPermission("market.vip.gold")) {
            return 0.10; // 10% discount
        } else if (player.hasPermission("market.vip.silver")) {
            return 0.05; // 5% discount
        } else if (hasVipPricing(player)) {
            return 0.03; // 3% default VIP discount
        }
        
        return 0.0;
    }
    
    /**
     * Apply VIP discount to price
     * @param player the player
     * @param originalPrice the original price
     * @return discounted price
     */
    public double applyVipDiscount(Player player, double originalPrice) {
        double discount = getVipDiscount(player);
        if (discount > 0) {
            return originalPrice * (1.0 - discount);
        }
        return originalPrice;
    }
    
    /**
     * Get player's permission level description
     * @param player the player
     * @return permission level description
     */
    public String getPermissionLevel(Player player) {
        if (isAdmin(player)) {
            return "§cAdmin";
        } else if (hasVipPricing(player)) {
            if (player.hasPermission("market.vip.diamond")) {
                return "§bDiamond VIP";
            } else if (player.hasPermission("market.vip.gold")) {
                return "§6Gold VIP";
            } else if (player.hasPermission("market.vip.silver")) {
                return "§7Silver VIP";
            } else {
                return "§eVIP";
            }
        } else if (canUseMarket(player)) {
            return "§aPlayer";
        } else {
            return "§cNo Access";
        }
    }
    
    /**
     * Send permission denied message to player
     * @param player the player
     * @param permission the permission that was denied
     */
    public void sendPermissionDenied(Player player, String permission) {
        String message = plugin.getConfigManager().getMessage("no-permission");
        
        // Add specific permission info for admins
        if (isAdmin(player)) {
            message += "\n§7Missing permission: §e" + permission;
        }
        
        player.sendMessage(message);
    }
    
    /**
     * Get all registered permissions
     * @return map of permission names to Permission objects
     */
    public Map<String, Permission> getAllPermissions() {
        return new HashMap<>(permissions);
    }
    
    /**
     * Get permission information for admin commands
     * @param player the player requesting info
     * @return formatted permission information
     */
    public String getPermissionInfo(Player player) {
        StringBuilder info = new StringBuilder();
        
        info.append("§c§l=== PERMISSION INFO ===").append("\n");
        info.append("§7▪ §ePlayer: §f").append(player.getName()).append("\n");
        info.append("§7▪ §eLevel: ").append(getPermissionLevel(player)).append("\n");
        
        if (hasVipPricing(player)) {
            double discount = getVipDiscount(player) * 100;
            info.append("§7▪ §eVIP Discount: §a").append(String.format("%.1f%%", discount)).append("\n");
        }
        
        info.append("\n§7Basic Permissions:");
        info.append("\n§7▪ §eMarket Access: ").append(canUseMarket(player) ? "§aYes" : "§cNo");
        info.append("\n§7▪ §eBuy Items: ").append(canBuyItems(player) ? "§aYes" : "§cNo");
        info.append("\n§7▪ §eSell Items: ").append(canSellItems(player) ? "§aYes" : "§cNo");
        
        if (isAdmin(player)) {
            info.append("\n\n§7Admin Permissions:");
            info.append("\n§7▪ §eManage Categories: ").append(canManageCategories(player) ? "§aYes" : "§cNo");
            info.append("\n§7▪ §eAdd Items: ").append(canAddItems(player) ? "§aYes" : "§cNo");
            info.append("\n§7▪ §eRemove Items: ").append(canRemoveItems(player) ? "§aYes" : "§cNo");
            info.append("\n§7▪ §eSet Prices: ").append(canSetPrices(player) ? "§aYes" : "§cNo");
            info.append("\n§7▪ §eMove Items: ").append(canMoveItems(player) ? "§aYes" : "§cNo");
            info.append("\n§7▪ §eManage Slots: ").append(canManageSlots(player) ? "§aYes" : "§cNo");
            info.append("\n§7▪ §eModify Settings: ").append(canModifySettings(player) ? "§aYes" : "§cNo");
            info.append("\n§7▪ §eReset Market: ").append(canResetMarket(player) ? "§aYes" : "§cNo");
        }
        
        info.append("\n\n§7Special Permissions:");
        info.append("\n§7▪ §eBypass Cooldown: ").append(canBypassCooldown(player) ? "§aYes" : "§cNo");
        info.append("\n§7▪ §eBypass Limits: ").append(canBypassLimits(player) ? "§aYes" : "§cNo");
        info.append("\n§7▪ §eReceive Notifications: ").append(shouldReceiveNotifications(player) ? "§aYes" : "§cNo");
        
        info.append("\n§c§l======================");
        
        return info.toString();
    }
    
    /**
     * Unregister all permissions (for plugin disable)
     */
    public void unregisterPermissions() {
        for (String permissionName : permissions.keySet()) {
            try {
                plugin.getServer().getPluginManager().removePermission(permissionName);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to unregister permission '" + permissionName + "': " + e.getMessage());
            }
        }
        permissions.clear();
        plugin.getLogger().info("Unregistered all permissions");
    }
}