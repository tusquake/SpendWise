package com.finance.aiexpense.controller;

import com.finance.aiexpense.dto.*;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.enums.SubscriptionTier;
import com.finance.aiexpense.service.AIService;
import com.finance.aiexpense.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AIControllerTest {

    @Mock
    private AIService aiService;

    @Mock
    private RateLimiterService rateLimiterService;

    @InjectMocks
    private AIController aiController;

    private User mockUser;
    private AIAnalysisRequest aiAnalysisRequest;
    private AIAnalysisResponse aiAnalysisResponse;
    private ChatRequest chatRequest;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .subscriptionTier(SubscriptionTier.FREE)
                .build();

        aiAnalysisRequest = new AIAnalysisRequest();
        aiAnalysisRequest.setTransactions(Collections.singletonList("expense 1"));

        aiAnalysisResponse = new AIAnalysisResponse(
                Collections.singletonList(new CategorizedTransaction("expense 1", "category")), "summary");

        chatRequest = new ChatRequest();
        chatRequest.setQuery("Hello AI");
    }

    @Test
    void analyzeTransactions_success() {
        when(aiService.analyzeTransactions(any(List.class))).thenReturn(aiAnalysisResponse);

        ResponseEntity<ApiResponse<AIAnalysisResponse>> responseEntity = aiController.analyzeTransactions(aiAnalysisRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Analysis completed", responseEntity.getBody().getMessage());
        assertEquals(aiAnalysisResponse, responseEntity.getBody().getData());
    }

    @Test
    void getInsights_success() {
        String insights = "Financial insights";
        when(aiService.generateInsights(any(User.class))).thenReturn(insights);

        ResponseEntity<ApiResponse<String>> responseEntity = aiController.getInsights(mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Insights generated", responseEntity.getBody().getMessage());
        assertEquals(insights, responseEntity.getBody().getData());
    }

    @Test
    void chatWithAI_success() {
        String aiResponse = "AI chat response";
        int remainingChats = 5;

        doNothing().when(rateLimiterService).checkAndIncrementAIChatLimit(any(User.class));
        when(aiService.chatWithAI(anyString(), any(User.class))).thenReturn(aiResponse);
        when(rateLimiterService.getRemainingAIChats(any(User.class))).thenReturn(remainingChats);

        ResponseEntity<ApiResponse<Map<String, Object>>> responseEntity = aiController.chatWithAI(chatRequest, mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Chat response generated", responseEntity.getBody().getMessage());

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("response", aiResponse);
        expectedData.put("remainingChats", remainingChats);
        expectedData.put("subscriptionTier", mockUser.getSubscriptionTier().name());

        assertEquals(expectedData, responseEntity.getBody().getData());
    }

    @Test
    void getChatLimit_success() {
        int remainingChats = 5;
        int totalChats = SubscriptionTier.FREE.getDailyAIChatLimit();

        when(rateLimiterService.getRemainingAIChats(any(User.class))).thenReturn(remainingChats);

        ResponseEntity<ApiResponse<Map<String, Object>>> responseEntity = aiController.getChatLimit(mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("remaining", remainingChats);
        expectedData.put("total", totalChats);
        expectedData.put("subscriptionTier", mockUser.getSubscriptionTier().name());
        expectedData.put("isPremium", mockUser.isPremium());

        assertEquals(expectedData, responseEntity.getBody().getData());
    }
}

