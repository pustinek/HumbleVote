package com.pustinek.humblevote.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MonthChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();


    public HandlerList getHandlers() {
        return handlers;
    }


}
