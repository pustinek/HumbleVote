package com.pustinek.humblevote.sql;

import com.pustinek.humblevote.Main;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL extends Database {
    public MySQL(Main plugin) {
        super(plugin);
    }

    @Override
    HikariDataSource getDataSource() {

        Main.debug("Get data source in MySQL called");
        return null;
    }

    /**
     * Sends an asynchronous ping to the database
     */
    public void ping() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                     Statement s = con.createStatement()) {
                    Main.debug("Pinging to MySQL server...");
                    s.execute("/* ping */ SELECT 1");
                } catch (SQLException ex) {
                    Main.error("Failed to ping to MySQL server. Trying to reconnect...");
                    Main.debug("Failed to ping to MySQL server. Trying to reconnect...");
                    connect(null);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    String getQueryCreateTableVoteStatistics() {
        return null;
    }

    @Override
    String getQueryCreateTableQueuedVotes() {
        return null;
    }
}
