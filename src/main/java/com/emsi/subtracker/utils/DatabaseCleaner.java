package com.emsi.subtracker.utils;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseCleaner {

    public static void reset() {
        System.out.println("Starting Database Cleanup...");
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement()) {

            // Delete in correct order to respect FK
            System.out.println("Deleting Subscriptions...");
            stmt.executeUpdate("DELETE FROM abonnements");

            System.out.println("Deleting Family Members...");
            stmt.executeUpdate("DELETE FROM family_members");

            System.out.println("Deleting Users...");
            stmt.executeUpdate("DELETE FROM users");

            System.out.println("✅ Database Cleanup Completed Successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error during cleanup: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
