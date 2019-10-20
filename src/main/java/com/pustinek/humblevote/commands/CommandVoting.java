package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.gui.GUIManager;
import com.pustinek.humblevote.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandVoting extends CommandDefault {
    private final Main plugin;

    public CommandVoting(Main plugin) {

        this.plugin = plugin;
    }

    @Override
    public String getCommandStart() {
        return "humblevote voting";
    }

    @Override
    public String getHelp(CommandSender target) {
        if (target.hasPermission(Permissions.VOTING))
            return "/hvote voting - view website where you can vote";
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.VOTING)){
            sender.sendMessage("You don't have permission to view voting sites");
            return;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage("Command can only be executed  in-game");
            return;
        }

        GUIManager.displayVotingGUI().open((Player) sender);


    }
}
