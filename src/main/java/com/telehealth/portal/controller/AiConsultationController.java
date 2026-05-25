package com.telehealth.portal.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AiConsultationController {

    // === REDIRECTED GATEWAY CONFIGURATIONS FOR OPENROUTER ===
    private final String OPENROUTER_API_KEY = "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"; 
    private final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    @GetMapping("/api/ai/autocomplete")
    public ResponseEntity<Map<String, String>> getAiDosageAutocomplete(@RequestParam("symptoms") String symptoms, HttpSession session) {
        Map<String, String> responseNode = new HashMap<>();
        
        // Security Check: Enforce user session boundaries
        if (session.getAttribute("loggedInUser") == null) {
            responseNode.put("suggestion", "Unauthorized session.");
            return ResponseEntity.status(401).body(responseNode);
        }

        if (symptoms == null || symptoms.trim().isEmpty()) {
            responseNode.put("suggestion", "");
            return ResponseEntity.ok(responseNode);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(OPENROUTER_API_KEY);
            
            // Optional OpenRouter ranking headers (Good practice for professional identification tracking)
            headers.set("HTTP-Referer", "http://localhost:8080"); 
            headers.set("X-Title", "Dhakne Telehealth Portal");

            // Construct strict system parameters to prevent AI hallucinations
            String systemPrompt = "You are an expert clinical medical assistant. Based on the symptoms provided, output a concise, professional prescription recommendation template consisting of standard medicines and precise dosage instructions. Use a clean, numbered list format like:\n1. Medicine Name (Dosage) - Frequency (Duration)\nDo not include introductory prose, headers, or conversational text.";
            
            // Build Unified OpenAI-Compatible Payload Schema Matrix
            Map<String, Object> requestBody = new HashMap<>();
            
            // === CHOOSE YOUR OPENROUTER MODEL SLUG HERE ===
            requestBody.put("model", "google/gemini-2.5-flash"); 
            
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", "Suggest standard medications for these symptoms: " + symptoms)
            ));
            requestBody.put("max_tokens", 150);
            requestBody.put("temperature", 0.3);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENROUTER_URL, entity, Map.class);

            // Parse response node payload from standard response blocks
            List choices = (List) response.getBody().get("choices");
            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");
            String aiSuggestionText = (String) message.get("content");

            responseNode.put("suggestion", aiSuggestionText.trim());
            return ResponseEntity.ok(responseNode);

        } catch (Exception e) {
            responseNode.put("suggestion", "Error compiling AI recommendation via OpenRouter.");
            return ResponseEntity.status(500).body(responseNode);
        }
    }
}