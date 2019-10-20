package com.pustinek.humblevote.commands;


import com.pustinek.humblevote.Main;
import org.bukkit.command.CommandSender;

/*
* Example of implemented command
* */
public class CommandTest extends CommandDefault {
    private final Main plugin;

    public CommandTest(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCommandStart() {
        return "humblevote test";
    }

    @Override
    public String getHelp(CommandSender target) {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if(args.length < 2) {
            sender.sendMessage("specify command");
            return;
        }
        switch (args[1]) {

            case "get":
                if(args.length < 3) return;
                Main.debug("GET - command called");
                Main.getVoteManager().getAllPlayerQueuedVotes(args[2]);
                break;

            case"db":
                if(args.length < 3) return;
                Main.debug("DB - command called");
                Main.getVoteManager().votesFromCacheToDB(args[2]);
                break;
        }




    }
}
