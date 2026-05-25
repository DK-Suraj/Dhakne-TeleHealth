package com.telehealth.portal.service.impl;

import com.telehealth.portal.entity.User;
import com.telehealth.portal.repository.UserRepository;
import com.telehealth.portal.service.UserService;
import com.telehealth.portal.util.PasswordUtil; // <-- IMPORT THIS
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email address is already registered!");
        }

        // 1. SECURE STORAGE: Hash the password before saving to MySQL
        String secureHash = PasswordUtil.hashPassword(user.getPassword());
        user.setPassword(secureHash);

        return userRepository.save(user);
    }

    @Override
    public User loginUser(String email, String password) {
        // Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email address or user does not exist."));

        // 2. SECURE VERIFICATION: Match plain password input with stored DB hash
        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new RuntimeException("Incorrect password. Please try again.");
        }

        return user;
    }
}