package com.finance.aiexpense.payment.strategy;

import com.finance.aiexpense.dto.PaymentRequest;
import com.finance.aiexpense.dto.PaymentResponse;

public interface PaymentStrategy {
    PaymentResponse createPaymentOrder(PaymentRequest request, String userId);
    PaymentResponse verifyPayment(String orderId, String paymentId, String signature);
    String getGatewayName();
    boolean supports(String paymentMethod);
}