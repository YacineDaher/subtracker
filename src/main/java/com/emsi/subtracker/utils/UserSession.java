package com.emsi.subtracker.utils;

import com.emsi.subtracker.models.User;

public class UserSession {

    private static UserSession instance;

    private User user;
    private com.emsi.subtracker.models.FamilyMember familyMember;
    private String currency = "DH"; // Default currency

    private UserSession() {
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.familyMember = null; // Clear family member when setting user
    }

    public com.emsi.subtracker.models.FamilyMember getFamilyMember() {
        return familyMember;
    }

    public void setFamilyMember(com.emsi.subtracker.models.FamilyMember familyMember) {
        this.familyMember = familyMember;
        this.user = null; // Clear user when setting family member
    }

    /**
     * Retourne true si connecté en tant que membre de famille.
     */
    public boolean isFamilyMember() {
        return familyMember != null;
    }

    /**
     * Récupère l'ID de l'utilisateur (principal ou parent du membre).
     */
    public int getUserId() {
        if (user != null) {
            return user.getId();
        } else if (familyMember != null) {
            return familyMember.getUserId(); // ID du parent
        }
        return 0;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void cleanUserSession() {
        user = null;
        familyMember = null;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "user=" + user +
                '}';
    }
}
