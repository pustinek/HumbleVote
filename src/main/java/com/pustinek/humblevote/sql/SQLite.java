package com.pustinek.humblevote.sql;

import com.pustinek.humblevote.Main;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;

public class SQLite extends Database {

    public SQLite(Main plugin) {
        super(plugin);
    }

    @Override
    HikariDataSource getDataSource() {
        Main.debug("Get data source called");
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Main.error("Failed to Initialize SQLite driver");
            Main.debug("Failed to Initialize SQLite driver");
            Main.error(e);
            return null;
        }

        File folder = plugin.getDataFolder();
        File dbFile = new File(folder, "votes.db");

        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException ex) {
                Main.error("Failed to create database file");
                Main.debug("Failed to create database file");
                Main.error(ex);
                return null;
            }
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:sqlite:" + dbFile));
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    @Override
    String getQueryCreateTableVoteStatistics() {
        return "CREATE TABLE IF NOT EXISTS " + tableVoteStatistics + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "playerId VARCHAR(36) NOT NULL,"
                + "lastUsername VARCHAR(36) NOT NULL,"
                + "total TINYTEXT NOT NULL,"
                + "statistics TEXT NOT NULL)";
    }

    @Override
    String getQueryCreateTableQueuedVotes() {
        return "CREATE TABLE IF NOT EXISTS " + tableQueuedVotes + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "address TINYTEXT NOT NULL,"
                + "serviceName TINYTEXT NOT NULL,"
                + "username TINYTEXT NOT NULL,"
                + "localTimestamp TINYTEXT NOT NULL,"
                + "timestamp TINYTEXT NOT NULL)";
    }

    @Override
    String getQueryCreateTableRewards() {
        return "CREATE TABLE IF NOT EXISTS " + tableRewards + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "playerID VARCHAR(36) NOT NULL,"
                + "playerName TINYTEXT NOT NULL,"
                + "rewardID TINYTEXT NOT NULL,"
                + "timestamp TINYTEXT NOT NULL)";
    }


}
