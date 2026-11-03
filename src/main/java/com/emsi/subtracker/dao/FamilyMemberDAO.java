package com.emsi.subtracker.dao;

import com.emsi.subtracker.models.FamilyMember;
import java.util.List;

/**
 * Interface DAO pour la gestion des membres de famille.
 */
public interface FamilyMemberDAO {

    /**
     * Crée un nouveau membre de famille.
     */
    FamilyMember save(FamilyMember member);

    /**
     * Trouve un membre par son ID.
     */
    FamilyMember findById(int id);

    /**
     * Trouve tous les membres d'un utilisateur principal.
     */
    List<FamilyMember> findByUserId(int userId);

    /**
     * Trouve un membre par son username (pour authentification).
     */
    FamilyMember findByUsername(String username);

    /**
     * Met à jour un membre existant.
     */
    boolean update(FamilyMember member);

    /**
     * Supprime un membre.
     */
    boolean delete(int id);

    /**
     * Compte le nombre de membres pour un utilisateur (max 5).
     */
    int countByUserId(int userId);
}
