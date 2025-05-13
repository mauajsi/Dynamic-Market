# Vault and Essential X required


# DynamicMarket

A dynamic economy plugin for Minecraft servers that creates an interactive market system with automatic price adjustments based on supply and demand.

## Features
- Dynamic pricing system that adjusts based on trading activity
- Category-based item organization with customizable GUI slots
- User-friendly GUI interfaces
- Comprehensive admin controls
- Vault economy integration

## Commands

### Player Commands
- `/market` - Opens the market GUI to buy items
- `/sell` - Opens GUI to sell items to the market

### Admin Commands
- `/marketadmin addcategory <name> <icon>` - Creates a new market category
- `/marketadmin add <category> <price>` - Adds the item you're holding to the market
- `/marketadmin remove <category> <item>` - Removes an item from the market
- `/marketadmin setprice <category> <item> <buy_price> [sell_price]` - Sets custom prices
- `/marketadmin move <from> <to> <item>` - Moves items between categories
- `/marketadmin reset` - Resets market to default configuration
- `/marketsettings <setting> <value>` - Modify market settings in-game
- `/setslot <category> <slot>` - Set GUI slot position for a category

## Permissions
- `market.admin.category` - Allows creation and management of market categories
- `market.admin.add` - Permits adding new items to the market
- `market.admin.remove` - Allows removal of items from any market category
- `market.admin.setprice` - Enables price modifications for existing items
- `market.admin.move` - Permits moving items between different categories
- `market.admin.reset` - Allows resetting the entire market to default state
- `market.admin.slot` - Allows changing category positions in GUI
- `market.admin` - Parent permission that includes all admin permissions

## Price System
- Prices automatically adjust based on trading volume
- Buy prices increase when items are frequently purchased
- Sell prices decrease when items are frequently sold
- 0.5% price adjustment per item traded
- Prices are capped between 0.1 and 1000.0

## Dependencies
- Requires Vault for economy support
- Compatible with any economy plugin that supports Vault
- Minecraft 1.13+

## Installation
1. Place the plugin JAR in your server's plugins folder
2. Install Vault and EssentialX
3. Restart your server
4. Configure categories and items using admin commands
5. Adjust permissions as needed in your permissions plugin
