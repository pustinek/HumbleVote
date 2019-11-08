package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.gui.GUIManager;
import com.pustinek.humblevote.utils.Permissions;
import com.pustinek.humblevote.voteStatistics.constants.TOP_VOTES_STATS_TYPE;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTop extends CommandDefault {


    CommandTop(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommandStart() {
        return "humblevote top";
    }

    @Override
    public String getHelp(CommandSender target) {
        return "help-top";
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

        Player player = (Player) sender;

        TOP_VOTES_STATS_TYPE voteStats = TOP_VOTES_STATS_TYPE.TOTAL;
        if(args.length > 1 && args[1].equalsIgnoreCase("m") ) {
            voteStats = TOP_VOTES_STATS_TYPE.MONTH;
        }

        GUIManager.displayTopVotersGUI(voteStats).open(player);
    }
}
