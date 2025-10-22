package com.finance.aiexpense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private boolean success;
    private String transactionId;
    private String orderId;
    private String clientSecret; // For Stripe
    private String checkoutUrl; // For PhonePe redirect
    private String razorpayKeyId; // For Razorpay frontend
    private Double amount;
    private String currency;
    private String paymentMethod;
    private String paymentGateway;
    private String message;
}