package com.example.economia.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.example.economia.MarketManager;
import com.example.economia.gui.MarketGUI;

public class MarketCommand implements CommandExecutor {
    private MarketManager marketManager;
    private MarketGUI marketGUI;

    public MarketCommand(MarketManager marketManager, MarketGUI marketGUI) {
        this.marketManager = marketManager;
        this.marketGUI = marketGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        marketGUI.openMainMenu(player);
        return true;
    }
}