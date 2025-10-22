package com.finance.aiexpense.controller;

import com.finance.aiexpense.dto.*;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.service.AIService;
import com.finance.aiexpense.service.RateLimiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "AI Analysis", description = "AI-powered financial analysis endpoints")
public class AIController {

    private final AIService aiService;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze and categorize transactions using AI")
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> analyzeTransactions(
            @Valid @RequestBody AIAnalysisRequest request) {
        AIAnalysisResponse analysis = aiService.analyzeTransactions(request.getTransactions());
        return ResponseEntity.ok(ApiResponse.success("Analysis completed", analysis));
    }

    @GetMapping("/insights")
    @Operation(summary = "Get AI-generated financial insights")
    public ResponseEntity<ApiResponse<String>> getInsights(
            @AuthenticationPrincipal User user) {
        String insights = aiService.generateInsights(user);
        return ResponseEntity.ok(ApiResponse.success("Insights generated", insights));
    }

    @PostMapping("/chatbot")
    @Operation(summary = "Chat with AI finance assistant (Rate limited)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chatWithAI(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal User user) {

        // Check rate limit before processing
        rateLimiterService.checkAndIncrementAIChatLimit(user);

        String response = aiService.chatWithAI(request.getQuery(), user);
        int remaining = rateLimiterService.getRemainingAIChats(user);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("response", response);
        responseData.put("remainingChats", remaining);
        responseData.put("subscriptionTier", user.getSubscriptionTier().name());

        return ResponseEntity.ok(ApiResponse.success("Chat response generated", responseData));
    }

    @GetMapping("/chat-limit")
    @Operation(summary = "Get remaining AI chat requests")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChatLimit(
            @AuthenticationPrincipal User user) {

        int remaining = rateLimiterService.getRemainingAIChats(user);
        int total = user.getSubscriptionTier().getDailyAIChatLimit();

        Map<String, Object> limitInfo = new HashMap<>();
        limitInfo.put("remaining", remaining);
        limitInfo.put("total", total);
        limitInfo.put("subscriptionTier", user.getSubscriptionTier().name());
        limitInfo.put("isPremium", user.isPremium());

        return ResponseEntity.ok(ApiResponse.success(limitInfo));
    }
}
