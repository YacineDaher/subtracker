package com.emsi.subtracker.dao.impl;

import com.emsi.subtracker.dao.UserDAO;
import com.emsi.subtracker.models.User;
import com.emsi.subtracker.utils.DBConnection;
import com.emsi.subtracker.utils.PasswordUtil;

import java.sql.*;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    public UserDAOImpl() {
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement()) {

            String sql1 = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'account_type') "
                    + "BEGIN ALTER TABLE users ADD account_type VARCHAR(20) DEFAULT 'individual'; END";
            stmt.execute(sql1);

            String sql2 = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'profile_picture') "
                    + "BEGIN ALTER TABLE users ADD profile_picture VARCHAR(255) NULL; END";
            stmt.execute(sql2);

        } catch (SQLException e) {
            System.err.println("Migration warning: " + e.getMessage());
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (username, password, email, account_type, profile_picture) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            // Default to individual if null
            stmt.setString(4, user.getAccountType() != null ? user.getAccountType() : "individual");
            stmt.setString(5, user.getProfilePicture());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user", e);
        }
        return user;
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        Optional<User> user = findByUsername(username);
        return user.isPresent() && PasswordUtil.matches(password, user.get().getPassword());    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, account_type = ?, profile_picture = ? WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getAccountType());
            stmt.setString(5, user.getProfilePicture());
            stmt.setInt(6, user.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    @Override
    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"));

        try {
            String type = rs.getString("account_type");
            if (type != null) {
                user.setAccountType(type);
            }
        } catch (SQLException e) {
        }
        try {
            String pic = rs.getString("profile_picture");
            if (pic != null) {
                user.setProfilePicture(pic);
            }
        } catch (SQLException e) {
        }
        return user;
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
