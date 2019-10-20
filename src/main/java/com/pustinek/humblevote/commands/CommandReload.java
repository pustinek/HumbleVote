package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Permissions;
import org.bukkit.command.CommandSender;

public class CommandReload extends CommandDefault {
    private final Main plugin;

    public CommandReload(Main plugin) {

        this.plugin = plugin;
    }

    @Override
    public String getCommandStart() {
        return "humblevote reload";
    }

    @Override
    public String getHelp(CommandSender target) {
        if (target.hasPermission(Permissions.RELOAD))
            return "/reload - reload configs";
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission(Permissions.RELOAD))
            Main.getConfigManager().reloadConfig();
            Main.getVoteSitesManager().loadVotingSites();

    }
}
