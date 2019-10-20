package com.pustinek.humblevote.voting;

import java.util.ArrayList;

public class VoteReward {

    private String name;
    private ArrayList<String> requirements;
    private ArrayList<String> commands;
    private Integer money;
    private String playerMessage;
    private String brodcast;


    public VoteReward(String name, ArrayList<String> requirements, ArrayList<String> commands, Integer money, String playerMessage, String brodcast) {
        this.name = name;
        this.requirements = requirements;
        this.commands = commands;
        this.money = money;
        this.playerMessage = playerMessage;
        this.brodcast = brodcast;
    }


    public String getName() {
        return name;
    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public ArrayList<String> getRequirements() {
        return requirements;
    }
}
