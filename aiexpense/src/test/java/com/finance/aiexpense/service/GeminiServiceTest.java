package com.finance.aiexpense.service;

import com.finance.aiexpense.config.GeminiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private GeminiConfig geminiConfig;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        when(geminiConfig.getBaseUrl()).thenReturn("https://generativelanguage.googleapis.com/v1beta");
        when(geminiConfig.getModel()).thenReturn("gemini-pro");
        when(geminiConfig.getApiKey()).thenReturn("test-api-key");
    }

    @Test
    void generateContent_success() throws Exception {
        String responseBody = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Test response\"}]}}]}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        CompletableFuture<String> future = geminiService.generateContent("Test prompt");
        String result = future.join();

        assertNotNull(result);
        assertEquals("Test response", result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void generateContent_noResponse() throws Exception {
        String responseBody = "{\"candidates\":[]}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        CompletableFuture<String> future = geminiService.generateContent("Test prompt");
        String result = future.join();

        assertNotNull(result);
        assertEquals("No response generated", result);
    }

    @Test
    void generateContent_exception_throwsAIServiceException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        CompletableFuture<String> future = geminiService.generateContent("Test prompt");

        assertThrows(Exception.class, () -> future.join());
    }

    @Test
    void generateContentFallback_returnsFallbackMessage() {
        CompletableFuture<String> future = geminiService.generateContentFallback("Test prompt", new RuntimeException("Error"));
        String result = future.join();

        assertNotNull(result);
        assertTrue(result.contains("AI service is temporarily unavailable"));
    }
}

