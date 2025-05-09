package com.example.economia.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.example.economia.gui.SellGUI;
import com.example.economia.MarketManager;

public class SellCommand implements CommandExecutor {
    private final MarketManager marketManager;
    private final SellGUI sellGUI;

    public SellCommand(MarketManager marketManager) {
        this.marketManager = marketManager;
        this.sellGUI = new SellGUI(marketManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cQuesto comando può essere usato solo da un giocatore!");
            return true;
        }

        Player player = (Player) sender;
        sellGUI.openSellGUI(player);
        return true;
    }
}