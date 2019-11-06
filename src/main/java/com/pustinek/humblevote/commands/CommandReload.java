package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Permissions;
import org.bukkit.command.CommandSender;

public class CommandReload extends CommandDefault {


    CommandReload(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommandStart() {
        return "humblevote reload";
    }

    @Override
    public String getHelp(CommandSender target) {
        if (target.hasPermission(Permissions.RELOAD))
            return "help-reload";
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.RELOAD)) return;
            Main.getConfigManager().reloadConfig();
            Main.getVoteSitesManager().loadVotingSites();
            Main.getRewardManager().loadRewardsConfig();
            Main.reloadManagers();
            Main.message(sender, "reload-success");

    }
}
