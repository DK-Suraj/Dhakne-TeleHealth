package com.telehealth.portal.controller;

import com.telehealth.portal.entity.User;
import com.telehealth.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
public class SecurityIdentityController {

    @Autowired
    private UserRepository userRepository;

    // =========================================================================
    // 🏥 SECTION 1: ACCOUNT EMAIL VERIFICATION (TOKEN BACKUP NODE)
    // =========================================================================
    
    // Intercept Account Activation Links clicked by user from inbox
    @GetMapping("/verify-account")
    public String executeVerificationTokenVerification(@RequestParam("token") String verificationToken) {
        Optional<User> potentialUser = userRepository.findByVerificationToken(verificationToken);
        
        if (potentialUser.isPresent()) {
            User targetUser = potentialUser.get();
            targetUser.setEnabled(true); // 🚨 UNLOCKED: Account is now verified and enabled
            targetUser.setVerificationToken(null); // Wipe consumed token from database
            userRepository.save(targetUser);
            
            String successMsg = URLEncoder.encode("Your clinical account validation completed successfully. Log in now.", StandardCharsets.UTF_8);
            return "redirect:/login?success=" + successMsg;
        }
        
        String errorMsg = URLEncoder.encode("Invalid or expired account activation token link.", StandardCharsets.UTF_8);
        return "redirect:/login?error=" + errorMsg;
    }

    // =========================================================================
    // 🔒 🚨 DELEGATION NOTICE: SECTION 2 FORGOT & RESET LIFE CYCLES 
    // HAVE BEEN MOVED AS SECURE 6-DIGIT OTP PATTERNS INSIDE AuthController.java
    // =========================================================================
}