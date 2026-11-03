package com.emsi.subtracker.services;

import com.emsi.subtracker.dao.FamilyMemberDAO;
import com.emsi.subtracker.dao.impl.FamilyMemberDAOImpl;
import com.emsi.subtracker.models.FamilyMember;
import com.emsi.subtracker.utils.PasswordUtil;

import java.util.List;

/**
 * Service de gestion des membres de famille.
 */
public class FamilyService {

    private final FamilyMemberDAO familyMemberDAO;
    private static final int MAX_FAMILY_MEMBERS = 5;

    public FamilyService() {
        this.familyMemberDAO = new FamilyMemberDAOImpl();
    }

    /**
     * Crée un nouveau membre de famille.
     * Vérifie la limite de 5 membres max.
     */
    public FamilyMember createMember(FamilyMember member) {
        // Vérifier limite
        int currentCount = familyMemberDAO.countByUserId(member.getUserId());
        if (currentCount >= MAX_FAMILY_MEMBERS) {
            throw new IllegalStateException("Limite maximale de " + MAX_FAMILY_MEMBERS + " membres atteinte");
        }

        // Vérifier unicité du username
        if (familyMemberDAO.findByUsername(member.getUsername()) != null) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }

        return familyMemberDAO.save(member);
    }

    /**
     * Crée plusieurs membres à la fois (inscription compte familial).
     */
    public void createMultipleMembers(int userId, List<FamilyMember> members) {
        for (FamilyMember member : members) {
            member.setUserId(userId);
            createMember(member);
        }
    }

    /**
     * Récup\u00e8re tous les membres d'une famille.
     */
    public List<FamilyMember> getFamilyMembers(int userId) {
        return familyMemberDAO.findByUserId(userId);
    }

    /**
     * Authentifie un membre de famille.
     */
    public FamilyMember authenticate(String username, String password) {
        FamilyMember member = familyMemberDAO.findByUsername(username);

        if (member != null && PasswordUtil.matches(password, member.getPassword())) {
            return member;
        }

        return null;
    }

    /**
     * Met à jour un membre.
     */
    public boolean updateMember(FamilyMember member) {
        return familyMemberDAO.update(member);
    }

    /**
     * Supprime un membre.
     */
    public boolean deleteMember(int id) {
        return familyMemberDAO.delete(id);
    }

    /**
     * Trouve un membre par ID.
     */
    public FamilyMember getMemberById(int id) {
        return familyMemberDAO.findById(id);
    }

    /**
     * Compte le nombre de membres pour un utilisateur.
     */
    public int getMemberCount(int userId) {
        return familyMemberDAO.countByUserId(userId);
    }

    /**
     * Vérifie si un utilisateur peut ajouter plus de membres.
     */
    public boolean canAddMoreMembers(int userId) {
        return familyMemberDAO.countByUserId(userId) < MAX_FAMILY_MEMBERS;
    }
}
