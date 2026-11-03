package com.emsi.subtracker.services;

import com.emsi.subtracker.dao.FamilyMemberDAO;
import com.emsi.subtracker.dao.impl.FamilyMemberDAOImpl;
import com.emsi.subtracker.models.FamilyMember;
import com.emsi.subtracker.utils.PasswordUtil;

import java.util.List;


public class FamilyService {

    private final FamilyMemberDAO familyMemberDAO;
    private static final int MAX_FAMILY_MEMBERS = 5;

    public FamilyService() {
        this.familyMemberDAO = new FamilyMemberDAOImpl();
    }

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


    public void createMultipleMembers(int userId, List<FamilyMember> members) {
        for (FamilyMember member : members) {
            member.setUserId(userId);
            createMember(member);
        }
    }

    public List<FamilyMember> getFamilyMembers(int userId) {
        return familyMemberDAO.findByUserId(userId);
    }


    public FamilyMember authenticate(String username, String password) {
        FamilyMember member = familyMemberDAO.findByUsername(username);

        if (member != null && PasswordUtil.matches(password, member.getPassword())) {
            return member;
        }

        return null;
    }


    public boolean updateMember(FamilyMember member) {
        return familyMemberDAO.update(member);
    }

    public boolean deleteMember(int id) {
        return familyMemberDAO.delete(id);
    }

    public FamilyMember getMemberById(int id) {
        return familyMemberDAO.findById(id);
    }

    public int getMemberCount(int userId) {
        return familyMemberDAO.countByUserId(userId);
    }

    public boolean canAddMoreMembers(int userId) {
        return familyMemberDAO.countByUserId(userId) < MAX_FAMILY_MEMBERS;
    }
}
