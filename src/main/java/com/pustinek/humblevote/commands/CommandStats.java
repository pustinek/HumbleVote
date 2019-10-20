package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStats  extends CommandDefault{


    private final Main plugin;

    public CommandStats(Main plugin) {

        this.plugin = plugin;
    }

    @Override
    public String getCommandStart() {
        return "humblevote stats";
    }

    @Override
    public String getHelp(CommandSender target) {
        return "humblevote stats - check player statistics";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.VOTING)){
            sender.sendMessage("You don't have permission to view voting statistics");
            return;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage("Command can only be executed  in-game");
            return;
        }

        Player player = (Player) sender;

        sender.sendMessage("your total votes: " + Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId()).getTotalVoteCount());
    }
}
