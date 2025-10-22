package com.finance.aiexpense.payment.factory;

import com.finance.aiexpense.payment.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final List<PaymentStrategy> paymentStrategies;

    public PaymentStrategy getStrategy(String paymentMethod) {
        return paymentStrategies.stream()
                .filter(strategy -> strategy.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No payment strategy found for method: " + paymentMethod));
    }

    public List<String> getSupportedGateways() {
        return paymentStrategies.stream()
                .map(PaymentStrategy::getGatewayName)
                .toList();
    }
}
