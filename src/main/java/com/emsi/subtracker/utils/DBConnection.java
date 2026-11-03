package com.emsi.subtracker.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    // TODO: Update these with your local SQL Server details
    // Updated with robust connection string
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=subtracker_db;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "password";

    private DBConnection() {
        try {
            // Load the driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to SQL Server successfully.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database Connection Failed: " + e.getMessage(), e);
        }
    }

    public static DBConnection getInstance() {
        try {
            if (instance == null || instance.getConnection().isClosed()) {
                instance = new DBConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
