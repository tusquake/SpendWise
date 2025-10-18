package com.finance.aiexpense.controller;

import com.finance.aiexpense.dto.*;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "AI Analysis", description = "AI-powered financial analysis endpoints")
public class AIController {

    private final AIService aiService;

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
    @Operation(summary = "Chat with AI finance assistant")
    public ResponseEntity<ApiResponse<String>> chatWithAI(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal User user) {
        String response = aiService.chatWithAI(request.getQuery(), user);
        return ResponseEntity.ok(ApiResponse.success("Chat response generated", response));
    }
}