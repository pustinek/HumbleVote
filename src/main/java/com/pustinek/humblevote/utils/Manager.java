package com.pustinek.humblevote.utils;

public abstract class Manager {

    /**
     * Called at shutdown of the plugin.
     */
    public void shutdown() {
        // To override by extending classes
    }

    /**
     * Called on reload of config files
     */
    public void reload() {

    }


}
