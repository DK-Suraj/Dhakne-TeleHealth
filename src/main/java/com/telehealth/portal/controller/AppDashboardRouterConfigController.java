package com.telehealth.portal.controller;

import com.telehealth.portal.entity.User;
import com.telehealth.portal.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppDashboardRouterConfigController {

    @Autowired
    private UserRepository userRepository;

    // 🚨 THIS DIRECTLY RECOGNIZES THE '/default-dashboard' ROUTE CONTEXT
    @GetMapping("/default-dashboard")
    public String evaluateAuthenticatedUserRoleDestinations(HttpSession projectSession) {
        // Fetch current active logged-in security context session status
        Authentication activeAuthenticationState = SecurityContextHolder.getContext().getAuthentication();
        
        if (activeAuthenticationState != null && activeAuthenticationState.isAuthenticated()) {
            String loggedInUserEmailIdentifier = activeAuthenticationState.getName();
            
            // Extract the matching core database row record proxy instance
            User verifiedUser = userRepository.findByEmail(loggedInUserEmailIdentifier).orElse(null);
            
            if (verifiedUser != null) {
                // Populate session caches seamlessly to keep legacy platform views undisturbed
                projectSession.setAttribute("loggedInUser", verifiedUser);
                
                System.out.println(">>> DASHBOARD ROUTER: User authenticated. Role evaluated: " + verifiedUser.getRole().name());
                
                // Route to appropriate access domain boundary
                if (verifiedUser.getRole().name().equals("ROLE_PATIENT") || verifiedUser.getRole().name().equals("PATIENT")) {
                    return "redirect:/patient/dashboard";
                } else if (verifiedUser.getRole().name().equals("ROLE_DOCTOR") || verifiedUser.getRole().name().equals("DOCTOR")) {
                    return "redirect:/doctor/dashboard";
                }
            }
        }
        
        return "redirect:/login?error=Access authorization session trace initialization failure.";
    }
}