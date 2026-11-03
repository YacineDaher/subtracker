package com.emsi.subtracker.dao;

import com.emsi.subtracker.models.User;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User create(User user);

    boolean checkCredentials(String username, String password);

    void update(User user);

    void delete(int userId);

    Optional<User> findById(int id);
}
