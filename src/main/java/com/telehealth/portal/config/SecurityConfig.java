package com.telehealth.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); 
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabled for smooth Thymeleaf operations
            .authorizeHttpRequests(auth -> auth
                // 🚨 ALLOW ALL AUTHENTICATION PARAMS & STATIONS PATHS CLEANLY
                .requestMatchers("/", "/login", "/register", "/verify-otp", "/verify-account", "/forgot-password", "/reset-password").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // Role-Based Access Control Boundaries
                .requestMatchers("/patient/**").hasRole("PATIENT") 
                .requestMatchers("/doctor/**").hasRole("DOCTOR")   
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login") 
                .loginProcessingUrl("/login-process") 
                .defaultSuccessUrl("/default-dashboard", true) 
                
                // 🚨 CRITICAL FIX: Simplified failure URL to avoid query-param interception crash
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}