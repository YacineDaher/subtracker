package com.emsi.subtracker.models;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;

    // Nouveaux champs pour gestion famille
    private String accountType = "individual"; // "individual" ou "family"
    private boolean isFamilyMember = false;
    private Integer parentUserId = null; // Si membre, ID du parent
    private String profilePicture = null; // Path or URL to profile picture

    public User() {
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(int id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public boolean isFamilyMember() {
        return isFamilyMember;
    }

    public void setFamilyMember(boolean familyMember) {
        isFamilyMember = familyMember;
    }

    public Integer getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(Integer parentUserId) {
        this.parentUserId = parentUserId;
    }

    public boolean isFamilyAccount() {
        return "family".equals(accountType);
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", accountType='" + accountType + '\'' +
                '}';
    }
}
