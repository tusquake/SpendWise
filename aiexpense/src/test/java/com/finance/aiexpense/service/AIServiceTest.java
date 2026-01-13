package com.finance.aiexpense.service;

import com.finance.aiexpense.dto.AIAnalysisResponse;
import com.finance.aiexpense.dto.CategorizedTransaction;
import com.finance.aiexpense.dto.TransactionDTO;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.enums.SubscriptionTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AIService aiService;

    private User user;
    private List<TransactionDTO> transactions;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .subscriptionTier(SubscriptionTier.FREE)
                .build();

        TransactionDTO transaction1 = TransactionDTO.builder()
                .id(1L)
                .description("Grocery shopping")
                .amount(100.0)
                .date(LocalDate.now())
                .category("Groceries")
                .build();

        TransactionDTO transaction2 = TransactionDTO.builder()
                .id(2L)
                .description("Uber ride")
                .amount(50.0)
                .date(LocalDate.now())
                .category("Travel")
                .build();

        transactions = Arrays.asList(transaction1, transaction2);
    }

    @Test
    void analyzeTransactions_emptyList() {
        AIAnalysisResponse response = aiService.analyzeTransactions(Collections.emptyList());

        assertNotNull(response);
        assertTrue(response.getCategorizedTransactions().isEmpty());
        assertEquals("No transactions to analyze.", response.getSummary());
    }

    @Test
    void analyzeTransactions_nullList() {
        AIAnalysisResponse response = aiService.analyzeTransactions(null);

        assertNotNull(response);
        assertTrue(response.getCategorizedTransactions().isEmpty());
        assertEquals("No transactions to analyze.", response.getSummary());
    }

    @Test
    void analyzeTransactions_success() {
        String aiResponse = "{\"categorizedTransactions\":[{\"transaction\":\"Grocery shopping: ₹100.00\",\"category\":\"Groceries\"}],\"summary\":\"Test summary\"}";
        when(geminiService.generateContent(anyString())).thenReturn(CompletableFuture.completedFuture(aiResponse));

        AIAnalysisResponse response = aiService.analyzeTransactions(transactions);

        assertNotNull(response);
        verify(geminiService).generateContent(anyString());
    }

    @Test
    void analyzeTransactions_fallbackOnException() {
        when(geminiService.generateContent(anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("AI service error")));

        AIAnalysisResponse response = aiService.analyzeTransactions(transactions);

        assertNotNull(response);
        assertFalse(response.getCategorizedTransactions().isEmpty());
        assertNotNull(response.getSummary());
    }

    @Test
    void generateInsights_noTransactions() {
        when(transactionService.getRecentTransactions(any(User.class), anyInt())).thenReturn(Collections.emptyList());

        String insights = aiService.generateInsights(user);

        assertEquals("No transaction data available for insights.", insights);
        verify(transactionService).getRecentTransactions(user, 3);
    }

    @Test
    void generateInsights_success() {
        when(transactionService.getRecentTransactions(any(User.class), anyInt())).thenReturn(transactions);
        String aiResponse = "Financial insights";
        when(geminiService.generateContent(anyString())).thenReturn(CompletableFuture.completedFuture(aiResponse));

        String insights = aiService.generateInsights(user);

        assertNotNull(insights);
        verify(transactionService).getRecentTransactions(user, 3);
        verify(geminiService).generateContent(anyString());
    }

    @Test
    void generateInsights_fallbackOnException() {
        when(transactionService.getRecentTransactions(any(User.class), anyInt())).thenReturn(transactions);
        when(geminiService.generateContent(anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Error")));

        String insights = aiService.generateInsights(user);

        assertNotNull(insights);
        assertTrue(insights.contains("spent"));
    }

    @Test
    void chatWithAI_success() {
        when(transactionService.getAllTransactions(any(User.class))).thenReturn(transactions);
        String aiResponse = "Chat response";
        when(geminiService.generateContent(anyString())).thenReturn(CompletableFuture.completedFuture(aiResponse));

        String response = aiService.chatWithAI("What did I spend on?", user);

        assertNotNull(response);
        assertEquals("Chat response", response);
        verify(transactionService).getAllTransactions(user);
        verify(geminiService).generateContent(anyString());
    }

    @Test
    void chatWithAI_fallbackOnException() {
        when(transactionService.getAllTransactions(any(User.class))).thenReturn(transactions);
        when(geminiService.generateContent(anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Error")));

        String response = aiService.chatWithAI("Question", user);

        assertNotNull(response);
        assertTrue(response.contains("trouble processing"));
    }
}

