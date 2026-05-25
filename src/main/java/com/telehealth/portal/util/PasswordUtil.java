package com.telehealth.portal.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Converts plain text password into a secure cryptographic hash
    public static String hashPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    // Matches the plain password entered during login against the stored database hash
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}