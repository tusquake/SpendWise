package com.finance.aiexpense.service;

import com.finance.aiexpense.dto.*;
import com.finance.aiexpense.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final GeminiService geminiService;
    private final TransactionService transactionService;

    public AIAnalysisResponse analyzeTransactions(List<TransactionDTO> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return AIAnalysisResponse.builder()
                    .categorizedTransactions(new ArrayList<>())
                    .summary("No transactions to analyze.")
                    .build();
        }

        String transactionText = transactions.stream()
                .map(t -> String.format("%s: ₹%.2f", t.getDescription(), t.getAmount()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format(
                "Analyze and categorize these financial transactions. " +
                        "For each transaction, assign ONE category from: Food, Travel, Groceries, " +
                        "Shopping, Entertainment, Utilities, Healthcare, Education, Others.\n\n" +
                        "Transactions:\n%s\n\n" +
                        "Provide response in this exact JSON format:\n" +
                        "{\n" +
                        "  \"categorizedTransactions\": [\n" +
                        "    {\"transaction\": \"description amount\", \"category\": \"Category\"}\n" +
                        "  ],\n" +
                        "  \"summary\": \"Brief 2-sentence spending summary\"\n" +
                        "}",
                transactionText
        );

        try {
            String aiResponse = geminiService.generateContent(prompt).join(); // ✅ fixed
            return parseAIResponse(aiResponse, transactions);
        } catch (Exception e) {
            log.error("AI analysis failed, using fallback", e);
            return createFallbackAnalysis(transactions);
        }
    }

    public String generateInsights(User user) {
        List<TransactionDTO> recentTransactions = transactionService.getRecentTransactions(user, 3);

        if (recentTransactions.isEmpty()) {
            return "No transaction data available for insights.";
        }

        double totalSpending = recentTransactions.stream()
                .mapToDouble(TransactionDTO::getAmount)
                .sum();

        Map<String, Double> categoryTotals = recentTransactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : "Others",
                        Collectors.summingDouble(TransactionDTO::getAmount)
                ));

        String categoryBreakdown = categoryTotals.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(e -> String.format("%s: ₹%.2f", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "You are a financial advisor. Analyze this spending data from the last 3 months:\n\n" +
                        "Total Spending: ₹%.2f\n" +
                        "Category Breakdown: %s\n\n" +
                        "Provide:\n" +
                        "1. Key spending insights\n" +
                        "2. Prediction for next month\n" +
                        "3. One actionable saving tip\n\n" +
                        "Keep response to 3-4 sentences, professional and helpful.",
                totalSpending, categoryBreakdown
        );

        try {
            return geminiService.generateContent(prompt).join(); // ✅ fixed
        } catch (Exception e) {
            log.error("Insight generation failed", e);
            return generateFallbackInsight(totalSpending, categoryTotals);
        }
    }

    public String chatWithAI(String query, User user) {
        List<TransactionDTO> transactions = transactionService.getAllTransactions(user);

        String context = buildTransactionContext(transactions);

        String prompt = String.format(
                "You are a helpful personal finance assistant. Answer the user's question " +
                        "based on their transaction data. Be concise and friendly.\n\n" +
                        "Transaction Summary:\n%s\n\n" +
                        "User Question: %s\n\n" +
                        "Provide a clear, helpful answer in 2-3 sentences.",
                context, query
        );

        try {
            return geminiService.generateContent(prompt).join(); // ✅ fixed
        } catch (Exception e) {
            log.error("Chat failed", e);
            return "I'm having trouble processing your request. Please try asking in a different way.";
        }
    }

    private String buildTransactionContext(List<TransactionDTO> transactions) {
        if (transactions.isEmpty()) {
            return "No transactions available.";
        }

        double total = transactions.stream()
                .mapToDouble(TransactionDTO::getAmount)
                .sum();

        Map<String, Double> categoryTotals = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : "Others",
                        Collectors.summingDouble(TransactionDTO::getAmount)
                ));

        StringBuilder context = new StringBuilder();
        context.append(String.format("Total Transactions: %d\n", transactions.size()));
        context.append(String.format("Total Spending: ₹%.2f\n", total));
        context.append("Category Breakdown:\n");
        categoryTotals.forEach((cat, amt) ->
                context.append(String.format("  - %s: ₹%.2f\n", cat, amt)));

        return context.toString();
    }

    private AIAnalysisResponse parseAIResponse(String content, List<TransactionDTO> transactions) {
        try {
            int jsonStart = content.indexOf("{");
            int jsonEnd = content.lastIndexOf("}") + 1;

            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = content.substring(jsonStart, jsonEnd);
                com.fasterxml.jackson.databind.ObjectMapper mapper =
                        new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(jsonStr, AIAnalysisResponse.class);
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI JSON response, using fallback");
        }

        return createFallbackAnalysis(transactions);
    }

    private AIAnalysisResponse createFallbackAnalysis(List<TransactionDTO> transactions) {
        List<CategorizedTransaction> categorized = transactions.stream()
                .map(t -> CategorizedTransaction.builder()
                        .transaction(t.getDescription() + " ₹" + t.getAmount())
                        .category(predictCategory(t.getDescription()))
                        .build())
                .collect(Collectors.toList());

        double total = transactions.stream()
                .mapToDouble(TransactionDTO::getAmount)
                .sum();

        Map<String, Double> categoryTotals = categorized.stream()
                .collect(Collectors.groupingBy(
                        CategorizedTransaction::getCategory,
                        Collectors.summingDouble(ct -> {
                            String amountStr = ct.getTransaction()
                                    .replaceAll("[^0-9.]", "");
                            return Double.parseDouble(amountStr);
                        })
                ));

        String topCategory = categoryTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        String summary = String.format(
                "Total spending: ₹%.2f across %d transactions. " +
                        "Your highest expense category is %s (₹%.2f).",
                total, transactions.size(), topCategory,
                categoryTotals.getOrDefault(topCategory, 0.0)
        );

        return AIAnalysisResponse.builder()
                .categorizedTransactions(categorized)
                .summary(summary)
                .build();
    }

    private String predictCategory(String description) {
        String lower = description.toLowerCase();
        if (lower.contains("uber") || lower.contains("ola") || lower.contains("taxi") ||
                lower.contains("bus") || lower.contains("train"))
            return "Travel";
        if (lower.contains("zomato") || lower.contains("swiggy") || lower.contains("restaurant") ||
                lower.contains("food") || lower.contains("cafe") || lower.contains("pizza"))
            return "Food";
        if (lower.contains("amazon") || lower.contains("flipkart") || lower.contains("myntra") ||
                lower.contains("shopping"))
            return "Shopping";
        if (lower.contains("bazaar") || lower.contains("grocery") || lower.contains("supermarket") ||
                lower.contains("vegetables") || lower.contains("dmart"))
            return "Groceries";
        if (lower.contains("netflix") || lower.contains("spotify") || lower.contains("prime") ||
                lower.contains("movie") || lower.contains("game"))
            return "Entertainment";
        if (lower.contains("electricity") || lower.contains("water") || lower.contains("gas") ||
                lower.contains("bill") || lower.contains("recharge"))
            return "Utilities";
        if (lower.contains("hospital") || lower.contains("doctor") || lower.contains("medicine") ||
                lower.contains("pharmacy") || lower.contains("clinic"))
            return "Healthcare";
        if (lower.contains("school") || lower.contains("course") || lower.contains("book") ||
                lower.contains("tuition") || lower.contains("education"))
            return "Education";
        return "Others";
    }

    private String generateFallbackInsight(double total, Map<String, Double> categories) {
        String topCategory = categories.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        double topAmount = categories.getOrDefault(topCategory, 0.0);
        double percentage = (topAmount / total) * 100;

        return String.format(
                "Over the last 3 months, you've spent ₹%.2f in total. " +
                        "Your highest expense category is %s, accounting for %.1f%% of your spending. " +
                        "Consider setting a monthly budget of ₹%.2f for %s to better control expenses.",
                total, topCategory, percentage, topAmount * 0.8, topCategory
        );
    }
}
