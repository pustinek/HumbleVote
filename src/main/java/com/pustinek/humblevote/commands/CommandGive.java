package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import org.bukkit.command.CommandSender;

public class CommandGive extends CommandDefault {

    CommandGive(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommandStart() {
        return null;
    }

    @Override
    public String getHelp(CommandSender target) {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
