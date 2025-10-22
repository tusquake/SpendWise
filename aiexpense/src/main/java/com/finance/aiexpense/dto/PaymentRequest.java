package com.finance.aiexpense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // UPI, DEBIT_CARD, CREDIT_CARD, NET_BANKING

    @NotBlank(message = "Subscription tier is required")
    private String subscriptionTier; // PREMIUM, ENTERPRISE

    @NotNull(message = "Duration in months is required")
    @Positive(message = "Duration must be positive")
    private Integer durationMonths;

    // Optional: specific payment gateway
    private String gateway; // RAZORPAY, PHONEPE (if null, auto-select)
}