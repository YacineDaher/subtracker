package com.emsi.subtracker.utils;

import com.emsi.subtracker.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides pooled JDBC connections backed by HikariCP.
 *
 * <p>Connection settings are read from {@code database.properties} (see
 * {@code database.properties.example}), so no credentials live in the code.
 *
 * <p>DAOs use {@code try (Connection conn = DBConnection.getInstance().getConnection())}.
 * Closing a pooled connection returns it to the pool instead of closing the
 * physical connection, so each query no longer opens a new connection to SQL Server.
 */
public final class DBConnection {

    private static volatile DBConnection instance;

    private final HikariDataSource dataSource;

    private DBConnection() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DatabaseConfig.getUrl());
        config.setUsername(DatabaseConfig.getUsername());
        config.setPassword(DatabaseConfig.getPassword());
        config.setDriverClassName(DatabaseConfig.getProperty(
                "db.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"));

        config.setPoolName(DatabaseConfig.getProperty("hikari.poolName", "SubTrackerPool"));
        config.setMaximumPoolSize(DatabaseConfig.getIntProperty("hikari.maximumPoolSize", 10));
        config.setMinimumIdle(DatabaseConfig.getIntProperty("hikari.minimumIdle", 2));
        config.setConnectionTimeout(DatabaseConfig.getLongProperty("hikari.connectionTimeout", 30_000));
        config.setIdleTimeout(DatabaseConfig.getLongProperty("hikari.idleTimeout", 600_000));
        config.setMaxLifetime(DatabaseConfig.getLongProperty("hikari.maxLifetime", 1_800_000));
        config.setAutoCommit(Boolean.parseBoolean(DatabaseConfig.getProperty("hikari.autoCommit", "true")));

        this.dataSource = new HikariDataSource(config);
        Runtime.getRuntime().addShutdownHook(new Thread(dataSource::close));
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Borrows a connection from the pool. Always use it in a try-with-resources
     * block so it is returned to the pool.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
