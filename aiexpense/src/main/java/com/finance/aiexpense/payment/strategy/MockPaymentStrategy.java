package com.finance.aiexpense.payment.strategy;

import com.finance.aiexpense.dto.PaymentRequest;
import com.finance.aiexpense.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Profile({"dev", "test"})
public class MockPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResponse createPaymentOrder(PaymentRequest request, String userId) {
        log.info("Mock payment: {} INR for user {}", request.getAmount(), userId);

        String txnId = "MOCK_" + UUID.randomUUID().toString().substring(0, 8);

        return PaymentResponse.builder()
                .success(true)
                .transactionId(txnId)
                .orderId(txnId)
                .amount(request.getAmount())
                .currency("INR")
                .paymentMethod(request.getPaymentMethod())
                .paymentGateway("MOCK")
                .message("Mock payment successful (DEV MODE)")
                .build();
    }

    @Override
    public PaymentResponse verifyPayment(String orderId, String paymentId, String signature) {
        log.info("Mock verification: orderId={}", orderId);
        return PaymentResponse.builder()
                .success(true)
                .transactionId(paymentId)
                .orderId(orderId)
                .message("Mock verification successful")
                .build();
    }

    @Override
    public String getGatewayName() {
        return "MOCK";
    }

    @Override
    public boolean supports(String paymentMethod) {
        return true; // Supports all methods in dev mode
    }
}