package com.emsi.subtracker.services;

import com.emsi.subtracker.dao.UserDAO;
import com.emsi.subtracker.dao.impl.UserDAOImpl;
import com.emsi.subtracker.models.FamilyMember;
import com.emsi.subtracker.models.User;
import com.emsi.subtracker.utils.PasswordUtil;

public class UserService {
    private final UserDAO userDAO;
    private final FamilyService familyService;

    public UserService() {
        this.userDAO = new UserDAOImpl();
        this.familyService = new FamilyService();
    }

    /**
     * Authentifie un utilisateur ou un membre de famille.
     * Cherche d'abord dans users, puis dans family_members.
     */
    public Object authenticateAny(String username, String password) {
        // 1. Essayer d'abord comme utilisateur principal
        User user = authenticate(username, password);
        if (user != null) {
            return user;
        }

        // 2. Essayer comme membre de famille
        FamilyMember member = familyService.authenticate(username, password);
        if (member != null) {
            return member;
        }

        return null;
    }

    /**
     * Authentifie uniquement un utilisateur principal.
     */
    public User authenticate(String username, String password) {
        if (userDAO.checkCredentials(username, password)) {
            return userDAO.findByUsername(username).orElse(null);
        }
        return null;
    }

    /**
     * Enregistre un nouvel utilisateur.
     *
     * @param accountType "individual" ou "family"
     */
    public User register(String username, String email, String password, String accountType) throws Exception {
        if (userDAO.findByUsername(username).isPresent()) {
            throw new Exception("Username already exists");
        }
        if (userDAO.findByEmail(email).isPresent()) {
            throw new Exception("Email already exists");
        }

        User newUser = new User(username, PasswordUtil.hash(password), email);
        newUser.setAccountType(accountType);
        return userDAO.create(newUser);
    }

    /**
     * Enregistre un utilisateur individuel (par défaut).
     */
    public User register(String username, String email, String password) throws Exception {
        return register(username, email, password, "individual");
    }

    public void updateUser(User user) throws Exception {
        // Optional: Check if new username/email already exists if they were changed
        userDAO.update(user);
    }

    public void changePassword(User user, String newPassword) {
        user.setPassword(PasswordUtil.hash(newPassword));
        userDAO.update(user);
    }

    public void deleteUser(User user) {
        userDAO.delete(user.getId());
    }

    public User getUserById(int id) {
        return userDAO.findById(id).orElse(null);
    }
}