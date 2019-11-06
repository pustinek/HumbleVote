package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.gui.GUIManager;
import com.pustinek.humblevote.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRewards extends CommandDefault {

    CommandRewards(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommandStart() {
        return "humblevote rewards";
    }

    @Override
    public String getHelp(CommandSender target) {
        if (target.hasPermission(Permissions.RELOAD))
            return "help-rewards";
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

        Player player = (Player) sender;

        GUIManager.displayRewardsGUI().open(player);
    }
}
