package com.telehealth.portal.repository;

import com.telehealth.portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Dynamic query parsing for credentials tracking
    Optional<User> findByEmail(String email);
    
    // Identity resolution queries for verification link matching sequences
    Optional<User> findByVerificationToken(String token);
    
    Optional<User> findByResetPasswordToken(String token);
}