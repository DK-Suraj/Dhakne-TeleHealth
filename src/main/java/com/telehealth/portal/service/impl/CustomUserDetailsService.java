package com.telehealth.portal.service.impl;

import com.telehealth.portal.entity.User;
import com.telehealth.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String dbRole = user.getRole().name();
        String authorityName;

        // Strict filter logic to prevent duplicate 'ROLE_ROLE_' prefix configurations
        if (dbRole.startsWith("ROLE_")) {
            authorityName = dbRole; // Keep it as is if it already contains the prefix
        } else {
            authorityName = "ROLE_" + dbRole; // Apply prefix only if it is missing
        }

        System.out.println(">>> SECURITY CONTEXT BUILDER: Compiling Session Signature for [" + email + "] with Authority: " + authorityName);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(), // Crucial: The user account must be explicitly enabled to pass authentication
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(authorityName))
        );
    }
}