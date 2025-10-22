package com.finance.aiexpense.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.aiexpense.config.GeminiConfig;
import com.finance.aiexpense.exception.AIServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @CircuitBreaker(name = "geminiAI", fallbackMethod = "generateContentFallback")
    @TimeLimiter(name = "geminiAI")
    @Cacheable(value = "aiResponses", key = "#prompt.hashCode()")
    public CompletableFuture<String> generateContent(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = String.format("%s/models/%s:generateContent?key=%s",
                        geminiConfig.getBaseUrl(),
                        geminiConfig.getModel(),
                        geminiConfig.getApiKey());

                Map<String, Object> requestBody = buildRequest(prompt);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                return extractTextFromResponse(response.getBody());

            } catch (Exception e) {
                log.error("Gemini API call failed", e);
                throw new AIServiceException("AI service is temporarily unavailable", e);
            }
        });
    }

    // Fallback method when circuit is open
    public CompletableFuture<String> generateContentFallback(String prompt, Exception e) {
        log.warn("Circuit breaker activated. Using fallback response. Error: {}", e.getMessage());

        String fallbackMessage =
                "⚠️ AI service is temporarily unavailable. " +
                        "We're working to restore it. Please try again in a few moments. " +
                        "Your request has been noted.";

        return CompletableFuture.completedFuture(fallbackMessage);
    }

    private Map<String, Object> buildRequest(String prompt) {
        Map<String, Object> request = new HashMap<>();

        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();

        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        parts.add(part);

        content.put("parts", parts);
        contents.add(content);

        request.put("contents", contents);

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 2048);

        request.put("generationConfig", generationConfig);

        return request;
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

            return "No response generated";

        } catch (Exception e) {
            log.error("Failed to parse Gemini response", e);
            return "Error parsing AI response";
        }
    }
}