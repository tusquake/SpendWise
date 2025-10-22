package com.finance.aiexpense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {
    private String name;
    private Double monthlyPrice;
    private String features;
    private String badge; // "BEST VALUE", "MOST POPULAR", etc.
    private int aiChatsPerDay;
    private int transactionsLimit;
}