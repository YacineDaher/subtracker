package com.emsi.subtracker.dao.impl;

import com.emsi.subtracker.dao.SubscriptionDAO;
import com.emsi.subtracker.models.Abonnement;
import com.emsi.subtracker.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubscriptionDAOImpl implements SubscriptionDAO {

    private static final String INSERT_SQL = "INSERT INTO abonnements (nom, prix, date_debut, frequence, categorie, user_id, assigned_to_member_id, logo_url, color_hex) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM abonnements WHERE user_id = ?";

    // Keep old one for compatibility/admin
    // Keep old one for compatibility/admin
    private static final String SELECT_ALL_ADMIN_SQL = "SELECT * FROM abonnements";

    public SubscriptionDAOImpl() {
        checkAndMigrateSchema();
    }

    private void checkAndMigrateSchema() {
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement()) {

            // Check if columns exist
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "abonnements", "logo_url");
            if (!rs.next()) {
                System.out.println("🔧 Migrating database: Adding logo_url and color_hex columns...");
                stmt.execute("ALTER TABLE abonnements ADD logo_url VARCHAR(500) NULL, color_hex VARCHAR(20) NULL");
                System.out.println("✅ Database migration completed.");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Database migration check failed: " + e.getMessage());
        }
    }

    @Override
    public Abonnement save(Abonnement abonnement) {
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            setStatementParams(pstmt, abonnement);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating subscription failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    abonnement.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating subscription failed, no ID obtained.");
                }
            }
            return abonnement;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving subscription", e);
        }
    }

    @Override
    public Abonnement update(Abonnement abonnement) {
        // Update query usually doesn't need to change for user_id unless we want to
        // enforce ownership
        // Update query usually doesn't need to change for user_id unless we want to
        // enforce ownership
        String UPDATE_SQL = "UPDATE abonnements SET nom=?, prix=?, date_debut=?, frequence=?, categorie=?, assigned_to_member_id=?, logo_url=?, color_hex=? WHERE id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {

            pstmt.setString(1, abonnement.getNom());
            pstmt.setDouble(2, abonnement.getPrix());
            pstmt.setDate(3, Date.valueOf(abonnement.getDateDebut()));
            pstmt.setString(4, abonnement.getFrequence());
            pstmt.setString(5, abonnement.getCategorie());

            if (abonnement.getAssignedToMemberId() != null) {
                pstmt.setInt(6, abonnement.getAssignedToMemberId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            pstmt.setString(7, abonnement.getLogoUrl());
            pstmt.setString(8, abonnement.getColorHex());

            pstmt.setInt(9, abonnement.getId());

            pstmt.executeUpdate();
            return abonnement;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating subscription", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String DELETE_SQL = "DELETE FROM abonnements WHERE id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(DELETE_SQL)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void deleteAll(int userId) {
        String sql = "DELETE FROM abonnements WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Abonnement> findById(Integer id) {
        String SELECT_BY_ID_SQL = "SELECT * FROM abonnements WHERE id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAbonnement(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<Abonnement> findAll(int userId) {
        List<Abonnement> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_SQL)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Abonnement> findAllByAssignedMemberId(int memberId) {
        List<Abonnement> list = new ArrayList<>();
        String sql = "SELECT * FROM abonnements WHERE assigned_to_member_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAbonnement(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Abonnement> findAll() {
        List<Abonnement> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SELECT_ALL_ADMIN_SQL)) {

            while (rs.next()) {
                list.add(mapResultSetToAbonnement(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Abonnement mapResultSetToAbonnement(ResultSet rs) throws SQLException {
        // Try to get assigned_to_member_id if column exists (optional family profiles
        // feature)
        Integer assignedToMemberId = null;
        try {
            assignedToMemberId = rs.getInt("assigned_to_member_id");
            if (rs.wasNull()) {
                assignedToMemberId = null;
            }
        } catch (SQLException e) {
            // Column doesn't exist - backward compatibility mode
        }

        Abonnement sub = new Abonnement(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getDouble("prix"),
                rs.getDate("date_debut").toLocalDate(),
                rs.getString("frequence"),
                rs.getString("categorie"),
                rs.getInt("user_id"),
                assignedToMemberId);

        try {
            sub.setLogoUrl(rs.getString("logo_url"));
            sub.setColorHex(rs.getString("color_hex"));
        } catch (SQLException e) {
            // Columns might not exist if migration failed or transient issue
        }

        return sub;
    }

    private void setStatementParams(PreparedStatement pstmt, Abonnement abonnement) throws SQLException {
        pstmt.setString(1, abonnement.getNom());
        pstmt.setDouble(2, abonnement.getPrix());
        pstmt.setDate(3, Date.valueOf(abonnement.getDateDebut()));
        pstmt.setString(4, abonnement.getFrequence());
        pstmt.setString(5, abonnement.getCategorie());
        pstmt.setInt(6, abonnement.getUserId());

        if (abonnement.getAssignedToMemberId() != null) {
            pstmt.setInt(7, abonnement.getAssignedToMemberId());
        } else {
            pstmt.setNull(7, Types.INTEGER);
        }

        pstmt.setString(8, abonnement.getLogoUrl());
        pstmt.setString(9, abonnement.getColorHex());
    }
}
