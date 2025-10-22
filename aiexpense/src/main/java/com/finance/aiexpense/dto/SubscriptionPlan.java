package com.finance.aiexpense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionPlan {
    private String name;
    private Double monthlyPrice;
    private String features;
}