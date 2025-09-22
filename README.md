
# DynamicMarket 

 A Minecraft economy plugin that introduces an interactive marketplace with dynamic prices that automatically adjust based on supply and demand. 
 
#  ✨ Features 
###  📈 Dynamic pricing that adapts to trading activity 
###  🗂️ Category-based organization with customizable GUI slots 
###  🖥️ Intuitive GUIs for buying and selling 
###  🔧 Full admin controls for market management 
###  💰 Vault economy integration 
  
#  📦 Dependencies 

###  Vault (required) 
 Compatible with any Vault-supported economy plugin 
 Works with Minecraft 1.13+ 
 
#  🕹️ Commands 

### Player Commands 
 /market → Opens the market GUI to buy items 
 /sell → Opens the market GUI to sell items 
### Admin Commands 
 /marketadmin addcategory <name> <icon> → Create a new category 
 
 /marketadmin add <category> <price> → Add the item in hand to the market 
 
 /marketadmin remove <category> <item> → Remove an item from the market 
 
 /marketadmin setprice <category> <item> <buy_price> [sell_price] → Set custom prices 
 
 /marketadmin move <from> <to> <item> → Move items between categories 
 
 /marketadmin reset → Reset the market to default 
 
 /marketadmin stock <category> <item> <amount> → Set item stock 
 
 /marketadmin addstock <category> <item> <amount> → Increase item stock 
 
 /marketadmin removestock <category> <item> <amount> → Decrease item stock 
 
 /marketsettings <setting> <value> → Change market settings in-game 
 
 /setslot <category> <slot> → Set GUI slot position for a category 
 
 /marketadmin setslot <category> <item> <slot> → Set GUI slot position for an item in a category
 
 
#  🔑 Permissions 

 market.admin.category → Create & manage categories 
 market.admin.add → Add new items 
 market.admin.remove → Remove items from categories 
 market.admin.setprice → Change item prices 
 market.admin.move → Move items between categories 
 market.admin.reset → Reset the market 
 market.admin.stock → Set item stock 
 market.admin.addstock → Increase item stock 
 market.admin.removestock → Decrease item stock 
 market.admin.slot → Change GUI category positions 
 market.admin → Grants all admin permissions 
 
#  📊 Pricing System

 Prices auto-adjust based on trade volume 
 Buy prices increase when items are bought frequently 
 Sell prices decrease when items are sold frequently 
 Adjustment rate: 0.5% per traded item 
 Prices are capped between 0.1 and 1000.0 (by default, you can change it in the config.yml)
