package com.emsi.subtracker.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() {}

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(12));
    }

    public static boolean matches(String plain, String hash) {
        try {
            return plain != null && hash != null && BCrypt.checkpw(plain, hash);
        } catch (IllegalArgumentException e) {
            return false; // hash invalide (ancien mot de passe en clair)
        }
    }
}