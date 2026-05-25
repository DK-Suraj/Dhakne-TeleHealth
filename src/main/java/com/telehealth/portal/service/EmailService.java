package com.telehealth.portal.service;

public interface EmailService {
    void sendVerificationEmail(String recipientEmail, String targetTokenLink);
    void sendForgotPasswordEmail(String recipientEmail, String targetResetLink);
}