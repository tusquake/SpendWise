package com.finance.aiexpense.enums;

public enum SubscriptionTier {
    FREE(2, 10, 100),           // 2 AI chats/day, 10 transactions/month
    PREMIUM(15, -1, 1000),      // 50 AI chats/day, unlimited transactions
    ENTERPRISE(30, -1, 10000); // 200 AI chats/day, unlimited

    private final int dailyAIChatLimit;
    private final int monthlyTransactionLimit;
    private final int totalTransactionLimit;

    SubscriptionTier(int dailyAIChatLimit, int monthlyTransactionLimit, int totalTransactionLimit) {
        this.dailyAIChatLimit = dailyAIChatLimit;
        this.monthlyTransactionLimit = monthlyTransactionLimit;
        this.totalTransactionLimit = totalTransactionLimit;
    }

    public int getDailyAIChatLimit() {
        return dailyAIChatLimit;
    }

    public int getMonthlyTransactionLimit() {
        return monthlyTransactionLimit;
    }

    public int getTotalTransactionLimit() {
        return totalTransactionLimit;
    }
}