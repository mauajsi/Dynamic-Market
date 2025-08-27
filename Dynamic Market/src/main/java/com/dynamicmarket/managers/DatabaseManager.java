package com.dynamicmarket.managers;

import com.dynamicmarket.DynamicMarket;
import com.dynamicmarket.data.MarketCategory;
import com.dynamicmarket.data.MarketItem;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class DatabaseManager {
    
    private final DynamicMarket plugin;
    private Connection connection;
    private final String databasePath;
    
    public DatabaseManager(DynamicMarket plugin) {
        this.plugin = plugin;
        this.databasePath = plugin.getDataFolder().getAbsolutePath() + File.separator + "market.db";
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            // Create plugin data folder if it doesn't exist
            plugin.getDataFolder().mkdirs();
            
            // Connect to the database using SQLite JDBC driver
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            
            // Create tables
            createTables();
            plugin.getLogger().info("Database initialized successfully!");
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }
    
    private void createTables() throws SQLException {
        // Create categories table
        String createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                name TEXT PRIMARY KEY,
                icon TEXT NOT NULL,
                display_name TEXT NOT NULL,
                description TEXT,
                gui_slot INTEGER NOT NULL
            )
            """;
        
        // Create items table
        String createItemsTable = """
            CREATE TABLE IF NOT EXISTS market_items (
                id TEXT PRIMARY KEY,
                category TEXT NOT NULL,
                material TEXT NOT NULL,
                display_name TEXT,
                lore TEXT,
                buy_price REAL NOT NULL,
                sell_price REAL NOT NULL,
                stock INTEGER NOT NULL,
                total_sold INTEGER DEFAULT 0,
                total_bought INTEGER DEFAULT 0,
                last_updated INTEGER NOT NULL,
                FOREIGN KEY (category) REFERENCES categories(name)
            )
            """;
        
        // Create transaction history table
        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_uuid TEXT NOT NULL,
                player_name TEXT NOT NULL,
                item_id TEXT NOT NULL,
                category TEXT NOT NULL,
                transaction_type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                price REAL NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createCategoriesTable);
            stmt.execute(createItemsTable);
            stmt.execute(createTransactionsTable);
        }
    }
    
    // Category database operations

    
    public void deleteCategory(String name) {
        String sql = "DELETE FROM categories WHERE name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            
            // Also delete all items in this category
            deleteItemsByCategory(name);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete category: " + name, e);
        }
    }
    
    // Item database operations
    public void saveItem(MarketItem item) {
        String sql = """
            INSERT OR REPLACE INTO market_items 
            (id, category, material, display_name, lore, buy_price, sell_price, stock, total_sold, total_bought, last_updated) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getCategory());
            pstmt.setString(3, item.getMaterial().name());
            pstmt.setString(4, item.getDisplayName());
            pstmt.setString(5, item.getLore() != null ? String.join("\n", item.getLore()) : null);
            pstmt.setDouble(6, item.getBuyPrice());
            pstmt.setDouble(7, item.getSellPrice());
            pstmt.setInt(8, item.getStock());
            pstmt.setInt(9, item.getTotalSold());
            pstmt.setInt(10, item.getTotalBought());
            pstmt.setLong(11, item.getLastUpdated());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save market item: " + item.getId(), e);
        }
    }
    
    public void deleteItem(String itemId) {
        String sql = "DELETE FROM market_items WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete market item: " + itemId, e);
        }
    }
    
    public void deleteItemsByCategory(String category) {
        String sql = "DELETE FROM market_items WHERE category = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete items for category: " + category, e);
        }
    }
    
    // Transaction logging
    public void logTransaction(String playerUuid, String playerName, String itemId, String category, 
                              String transactionType, int amount, double price) {
        String sql = """
            INSERT INTO transactions 
            (player_uuid, player_name, item_id, category, transaction_type, amount, price, timestamp) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            pstmt.setString(2, playerName);
            pstmt.setString(3, itemId);
            pstmt.setString(4, category);
            pstmt.setString(5, transactionType);
            pstmt.setInt(6, amount);
            pstmt.setDouble(7, price);
            pstmt.setLong(8, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to log transaction", e);
        }
    }
    
    // Load data from database
public List<MarketCategory> loadCategories() {
    List<MarketCategory> categories = new ArrayList<>();
    String sql = "SELECT * FROM categories";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        
        while (rs.next()) {
            String name = rs.getString("name");
            Material icon = Material.valueOf(rs.getString("icon"));
            String displayName = rs.getString("display_name");
            String description = rs.getString("description");
            int guiSlot = rs.getInt("gui_slot");
            
            List<String> descriptionList = description != null ? Collections.singletonList(description) : new ArrayList<>();
            MarketCategory category = new MarketCategory(name, icon, displayName, descriptionList, guiSlot);
            categories.add(category);
        }
    } catch (SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to load categories", e);
    }
    
    return categories;
}

public List<MarketItem> loadItems() {
    List<MarketItem> items = new ArrayList<>();
    String sql = "SELECT * FROM market_items";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        
        while (rs.next()) {
            String id = rs.getString("id");
            Material material = Material.valueOf(rs.getString("material"));
            String displayName = rs.getString("display_name");
            String loreStr = rs.getString("lore");
            List<String> lore = loreStr != null ? List.of(loreStr.split("\n")) : null;
            double buyPrice = rs.getDouble("buy_price");
            double sellPrice = rs.getDouble("sell_price");
            int stock = rs.getInt("stock");
            int totalSold = rs.getInt("total_sold");
            int totalBought = rs.getInt("total_bought");
            long lastUpdated = rs.getLong("last_updated");
            
            String category = rs.getString("category");
            MarketItem item = new MarketItem(id, material, displayName, lore, buyPrice, sellPrice, stock, category);
            item.setDisplayName(displayName);
            item.setLore(lore);
            item.setBuyPrice(buyPrice);
            item.setSellPrice(sellPrice);
            item.setStock(stock);
            item.setTotalSold(totalSold);
            item.setTotalBought(totalBought);
            item.setLastUpdated(lastUpdated);
            
            items.add(item);
        }
    } catch (SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to load items", e);
    }
    
    return items;
}

// Statistics and analytics
    public int getTotalTransactions() {
        String sql = "SELECT COUNT(*) FROM transactions";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get total transactions", e);
        }
        
        return 0;
    }
    
    public List<String> getTopSellingItems(int limit) {
        String sql = """
            SELECT item_id, SUM(amount) as total_sold 
            FROM transactions 
            WHERE transaction_type = 'BUY' 
            GROUP BY item_id 
            ORDER BY total_sold DESC 
            LIMIT ?
            """;
        
        List<String> topItems = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    topItems.add(rs.getString("item_id") + " (" + rs.getInt("total_sold") + " sold)");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get top selling items", e);
        }
        
        return topItems;
    }
    
    // Database maintenance
    public void cleanOldTransactions(long olderThanMillis) {
        String sql = "DELETE FROM transactions WHERE timestamp < ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, System.currentTimeMillis() - olderThanMillis);
            int deleted = pstmt.executeUpdate();
            
            if (deleted > 0) {
                plugin.getLogger().info("Cleaned " + deleted + " old transaction records.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to clean old transactions", e);
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }
    
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    public Connection getConnection() {
        return connection;
    }











    public void resetDatabase() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS transactions");
            stmt.execute("DROP TABLE IF EXISTS market_items");
            stmt.execute("DROP TABLE IF EXISTS categories");
            
            createTables();
            plugin.getLogger().info("Database has been reset successfully!");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reset database", e);
        }
    }

    public void saveCategory(MarketCategory category) {
        String sql = "INSERT OR REPLACE INTO categories (name, icon, display_name, description, gui_slot) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getIcon().name());
            pstmt.setString(3, category.getDisplayName());
            pstmt.setString(4, category.getDescription() != null && !category.getDescription().isEmpty() ? 
                           String.join("\n", category.getDescription()) : null);
            pstmt.setInt(5, category.getGuiSlot());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save category: " + category.getName(), e);
        }
    }
}