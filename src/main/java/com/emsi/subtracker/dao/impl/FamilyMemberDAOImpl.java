package com.emsi.subtracker.dao.impl;

import com.emsi.subtracker.dao.FamilyMemberDAO;
import com.emsi.subtracker.models.FamilyMember;
import com.emsi.subtracker.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation DAO pour les membres de famille.
 */
public class FamilyMemberDAOImpl implements FamilyMemberDAO {

    @Override
    public FamilyMember save(FamilyMember member) {
        String sql = "INSERT INTO family_members (user_id, name, username, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, member.getUserId());
            stmt.setString(2, member.getName());
            stmt.setString(3, member.getUsername());
            stmt.setString(4, member.getPassword());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        member.setId(generatedKeys.getInt(1));
                    }
                }
            }

            return member;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving family member", e);
        }
    }

    @Override
    public FamilyMember findById(int id) {
        String sql = "SELECT * FROM family_members WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<FamilyMember> findByUserId(int userId) {
        String sql = "SELECT * FROM family_members WHERE user_id = ? ORDER BY name";
        List<FamilyMember> members = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return members;
    }

    @Override
    public FamilyMember findByUsername(String username) {
        String sql = "SELECT * FROM family_members WHERE username = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean update(FamilyMember member) {
        String sql = "UPDATE family_members SET name = ?, username = ?, password = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, member.getName());
            stmt.setString(2, member.getUsername());
            stmt.setString(3, member.getPassword());
            stmt.setInt(4, member.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM family_members WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int countByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM family_members WHERE user_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Mappe un ResultSet vers un objet FamilyMember.
     */
    private FamilyMember mapResultSet(ResultSet rs) throws SQLException {
        FamilyMember member = new FamilyMember();
        member.setId(rs.getInt("id"));
        member.setUserId(rs.getInt("user_id"));
        member.setName(rs.getString("name"));
        member.setUsername(rs.getString("username"));
        member.setPassword(rs.getString("password"));

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            member.setCreatedAt(timestamp.toLocalDateTime());
        }

        return member;
    }
}
