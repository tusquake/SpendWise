package com.finance.aiexpense.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CircuitBreakerHealthIndicator implements HealthIndicator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Health health() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("geminiAI");

        CircuitBreaker.State state = circuitBreaker.getState();

        if (state == CircuitBreaker.State.OPEN || state == CircuitBreaker.State.FORCED_OPEN) {
            return Health.down()
                    .withDetail("circuitBreaker", "OPEN")
                    .withDetail("message", "AI service is currently unavailable")
                    .build();
        }

        return Health.up()
                .withDetail("circuitBreaker", state.toString())
                .build();
    }
}