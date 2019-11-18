package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.gui.GUIManager;
import com.pustinek.humblevote.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandVoting extends CommandDefault {

    CommandVoting(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommandStart() {
        return "humblevote voting";
    }

    @Override
    public String getHelp(CommandSender target) {
        if (target.hasPermission(Permissions.VOTING))
            return "help-voting";
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.VOTING)){
           Main.message(sender, "permission-insufficient");
            return;
        }

        if(!(sender instanceof Player)) {
            Main.message(sender, "cmd-onlyByPlayer");
            return;
        }

        GUIManager.displayVotingGUI().open((Player) sender);


    }
}
