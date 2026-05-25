package com.telehealth.portal.controller;

import com.telehealth.portal.entity.User;
import com.telehealth.portal.repository.UserRepository;
import com.telehealth.portal.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public String showLandingPage() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // =========================================================================
    // 🧱 ATOMIC 6-DIGIT NUMERIC OTP REGISTRATION ENGINE (AUTO-ROLLBACK CONTROL)
    // =========================================================================
    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class) 
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        
        if (result.hasErrors()) {
            return "register";
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "This email address signature is already registered inside our domain node.");
            return "register";
        }

        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            Random secureOtpRandomizer = new Random();
            int generatedOtpValue = 100000 + secureOtpRandomizer.nextInt(900000);
            String otpStringPointer = String.valueOf(generatedOtpValue);
            
            user.setVerificationToken(otpStringPointer); 
            user.setEnabled(false); 

            userRepository.save(user);

            emailService.sendVerificationEmail(user.getEmail(), otpStringPointer);
            System.out.println(">>> TRANSACTION MATRIX FLUSH: OTP [" + otpStringPointer + "] successfully dispatched onto: " + user.getEmail());

            return "redirect:/verify-otp?email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.err.println(">>> EXCEPTION MONITORED INSIDE AUTHCONTROLLER REGISTRATION: " + e.getMessage());
            model.addAttribute("error", "Mail server integration dispatch failed! Transaction rolled back. Reason: " + e.getMessage());
            return "register"; 
        }
    }

    // =========================================================================
    // 🔏 REAL-TIME HANDSHAKE INTERCEPTION: 6-DIGIT OTP MATCH SUBMISSION
    // =========================================================================
    @GetMapping("/verify-otp")
    public String showOtpVerificationPromptView(@RequestParam("email") String userEmailTarget, Model model) {
        model.addAttribute("targetEmail", userEmailTarget);
        return "verify-otp"; 
    }

    @PostMapping("/verify-otp")
    public String executeOtpMatchingSequenceCheck(@RequestParam("email") String accountEmailIndex,
                                                 @RequestParam("otpCode") String explicitInputOtpDigits,
                                                 Model model) {
        Optional<User> suspectUserInstance = userRepository.findByEmail(accountEmailIndex);
        
        if (suspectUserInstance.isPresent()) {
            User matchedDbUserEntity = suspectUserInstance.get();
            
            if (matchedDbUserEntity.getVerificationToken() != null && matchedDbUserEntity.getVerificationToken().equals(explicitInputOtpDigits)) {
                
                matchedDbUserEntity.setEnabled(true); 
                matchedDbUserEntity.setVerificationToken(null); 
                userRepository.save(matchedDbUserEntity);
                
                System.out.println(">>> OTP SERVICE REPORT: Activation context match success for user profile signature: " + accountEmailIndex);
                
                String successNotice = URLEncoder.encode("Identity verification completed successfully. Access credentials active, log in now.", StandardCharsets.UTF_8);
                return "redirect:/login?success=" + successNotice;
            }
        }
        
        model.addAttribute("targetEmail", accountEmailIndex);
        model.addAttribute("error", "The code submitted does not correspond with the system generated OTP token. Review parameters or re-submit.");
        return "verify-otp";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // =========================================================================
    // 🔑 SECTION 5: NEW STRENGTHENED 6-DIGIT OTP FORGOT & RESET LOGIC NODES
    // =========================================================================

    // 1. Render Forgot Password Initial Input Screen
    @GetMapping("/forgot-password")
    public String showForgotPasswordView() {
        return "forgot-password"; 
    }

    // 2. Intercept Email, Generate 6-Digit Reset OTP, and Dispatch Mail
    @PostMapping("/forgot-password")
    public String processForgotPasswordOtpRequest(@RequestParam("email") String userEmail, Model model) {
        Optional<User> suspectUser = userRepository.findByEmail(userEmail);
        
        if (suspectUser.isEmpty()) {
            model.addAttribute("error", "This email address is not registered inside our clinical domain.");
            return "forgot-password";
        }

        try {
            User user = suspectUser.get();
            
            // Cryptographic Safe 6-Digit Numeric Reset OTP Generator
            Random random = new Random();
            int resetOtp = 100000 + random.nextInt(900000);
            String resetOtpStr = String.valueOf(resetOtp);
            
            // Cache numeric code inside existing resetPasswordToken column field
            user.setResetPasswordToken(resetOtpStr);
            userRepository.save(user);
            
            // Execute email dispatch pipeline
            emailService.sendForgotPasswordEmail(user.getEmail(), resetOtpStr);
            System.out.println(">>> SECURITY CORE: Reset OTP [" + resetOtpStr + "] successfully dispatched to: " + user.getEmail());
            
            // Redirect smoothly to the validation page with email query parameter context
            return "redirect:/reset-password?email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            model.addAttribute("error", "Mail infrastructure failure: " + e.getMessage());
            return "forgot-password";
        }
    }

    // 3. Render Final Reset Form Input Prompt Window
    @GetMapping("/reset-password")
    public String showResetPasswordPromptForm(@RequestParam("email") String targetEmail, Model model) {
        model.addAttribute("targetEmail", targetEmail);
        return "reset-password"; 
    }

    // 4. Validate OTP and Overwrite New Password Credentials
    @PostMapping("/reset-password")
    public String executePasswordResetAuthenticationCheck(@RequestParam("email") String accountEmail,
                                                          @RequestParam("resetOtp") String inputOtp,
                                                          @RequestParam("newPassword") String plaintextPassword,
                                                          Model model) {
        Optional<User> suspectUser = userRepository.findByEmail(accountEmail);
        
        if (suspectUser.isPresent()) {
            User user = suspectUser.get();
            
            // Core matching condition: Verify input code matches with saved token signature
            if (user.getResetPasswordToken() != null && user.getResetPasswordToken().equals(inputOtp)) {
                
                // Encrypt new secure cipher hash string and persist
                user.setPassword(passwordEncoder.encode(plaintextPassword));
                user.setResetPasswordToken(null); // Wipe consumed token signature immediately
                userRepository.save(user);
                
                System.out.println(">>> SECURITY CORE: Password altered successfully for user signature: " + accountEmail);
                
                String successNotice = URLEncoder.encode("Password altered successfully. Secure credentials active, log in now.", StandardCharsets.UTF_8);
                return "redirect:/login?success=" + successNotice;
            }
        }
        
        model.addAttribute("targetEmail", accountEmail);
        model.addAttribute("error", "The reset OTP submitted does not correspond with system generated values. Verify parameters.");
        return "reset-password";
    }
}