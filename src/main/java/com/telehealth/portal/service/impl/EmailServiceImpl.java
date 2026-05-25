package com.telehealth.portal.service.impl;

import com.telehealth.portal.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender systemMailSender;

    @Override
    public void sendVerificationEmail(String recipientEmail, String generatedOTP) {
        SimpleMailMessage coreMailPayload = new SimpleMailMessage();
        coreMailPayload.setTo(recipientEmail);
        coreMailPayload.setSubject("🏥 Secure OTP Verification - Dhakne TeleHealth Portal");
        coreMailPayload.setText("Hello Patient,\n\n"
                + "Thank you for registering with Dhakne Healthcare Group. "
                + "Your account activation 6-digit security code is:\n\n"
                + "👉  " + generatedOTP + "  👈\n\n"
                + "Please input this token inside the verification portal field prompt immediately to unlock your profile access node.\n\n"
                + "Regards,\nIdentity Desk Operations");
        
        systemMailSender.send(coreMailPayload);
    }

    @Override
    public void sendForgotPasswordEmail(String recipientEmail, String targetResetLink) {
        SimpleMailMessage coreMailPayload = new SimpleMailMessage();
        coreMailPayload.setTo(recipientEmail);
        coreMailPayload.setSubject("🔒 Security Reset Protocol - Password Token Initialization");
        coreMailPayload.setText("Hello User,\n\nA request was initiated to re-configure the security credentials linked onto your profile.\n"
                + "Click the validation reference token link down below to establish a new password credential:\n"
                + targetResetLink + "\n\nRegards,\nSecurity Operations Center");
        
        systemMailSender.send(coreMailPayload);
    }   
}