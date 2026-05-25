package com.telehealth.portal.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Catches generic exceptions thrown anywhere across any controller layer
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        // Print the stack trace in your Eclipse console for debugging purposes
        ex.printStackTrace();
        
        // Pass a clean message to our custom error page layout
        model.addAttribute("errorMessage", "System Error: " + ex.getMessage());
        return "error"; // Resolves to templates/error.html
    }
    
    // You can add highly specific handlers here later, like RuntimeException or NullPointerException
}