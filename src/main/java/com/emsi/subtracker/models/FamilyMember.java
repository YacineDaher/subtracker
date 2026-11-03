package com.emsi.subtracker.models;

import java.time.LocalDateTime;

/**
 * Représente un membre de famille (sous-utilisateur).
 * Permet à un compte principal de gérer plusieurs profils.
 */
public class FamilyMember {

    private int id;
    private int userId; // ID du compte principal
    private String name;
    private String username;
    private String password;
    private LocalDateTime createdAt;

    // Constructeurs
    public FamilyMember() {
        this.createdAt = LocalDateTime.now();
    }

    public FamilyMember(int userId, String name, String username, String password) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name; // Pour affichage dans ComboBox
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FamilyMember that = (FamilyMember) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
