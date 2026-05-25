package com.telehealth.portal.service;

import com.telehealth.portal.entity.User;

public interface UserService {
    // Saves a new user to the system (Patient/Doctor/Admin)
    User registerUser(User user);
    
    // Validates credentials and logs a user in
    User loginUser(String email, String password);
}