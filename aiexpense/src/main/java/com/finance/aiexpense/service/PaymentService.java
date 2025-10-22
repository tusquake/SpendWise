package com.finance.aiexpense.service;

import com.finance.aiexpense.dto.PaymentRequest;
import com.finance.aiexpense.dto.PaymentResponse;
import com.finance.aiexpense.dto.SubscriptionPlanDTO;
import com.finance.aiexpense.entity.Payment;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.enums.PaymentMethod;
import com.finance.aiexpense.enums.PaymentStatus;
import com.finance.aiexpense.enums.SubscriptionTier;
import com.finance.aiexpense.payment.factory.PaymentStrategyFactory;
import com.finance.aiexpense.payment.strategy.PaymentStrategy;
import com.finance.aiexpense.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory strategyFactory;
    private final SubscriptionService subscriptionService;

    @Transactional
    public PaymentResponse createPaymentOrder(PaymentRequest request, User user) {
        log.info("Creating payment order for user: {}, amount: {}", user.getEmail(), request.getAmount());

        // Get appropriate payment strategy
        PaymentStrategy strategy = strategyFactory.getStrategy(request.getPaymentMethod());

        // Create payment order
        PaymentResponse response = strategy.createPaymentOrder(request, user.getEmail());

        // Save payment record
        Payment payment = Payment.builder()
                .user(user)
                .transactionId(response.getTransactionId())
                .orderId(response.getOrderId())
                .amount(request.getAmount())
                .currency("INR")
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .status(PaymentStatus.PENDING)
                .subscriptionTier(SubscriptionTier.valueOf(request.getSubscriptionTier()))
                .subscriptionDurationMonths(request.getDurationMonths())
                .paymentGateway(response.getPaymentGateway())
                .paymentGatewayResponse(response.getMessage())
                .build();

        paymentRepository.save(payment);

        log.info("Payment order created: {}", response.getOrderId());
        return response;
    }

    @Transactional
    public PaymentResponse verifyAndCompletePayment(
            String orderId, String paymentId, String signature, User user) {

        log.info("Verifying payment: orderId={}, paymentId={}", orderId, paymentId);

        // Find payment record
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Verify it belongs to the user
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        // Get strategy and verify
        PaymentStrategy strategy = strategyFactory.getStrategy(
                payment.getPaymentMethod().name());

        PaymentResponse verificationResponse = strategy.verifyPayment(
                orderId, paymentId, signature);

        if (verificationResponse.isSuccess()) {
            // Update payment status
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(paymentId);
            paymentRepository.save(payment);

            // Upgrade subscription
            subscriptionService.upgradeSubscription(
                    user,
                    payment.getSubscriptionTier(),
                    payment.getSubscriptionDurationMonths()
            );

            log.info("Payment verified and subscription upgraded for user: {}", user.getEmail());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.warn("Payment verification failed for orderId: {}", orderId);
        }

        return verificationResponse;
    }

    public List<Payment> getPaymentHistory(User user) {
        return paymentRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<SubscriptionPlanDTO> getAvailablePlans() {
        return Arrays.asList(
                SubscriptionPlanDTO.builder()
                        .name("FREE")
                        .monthlyPrice(0.0)
                        .features("2 AI chats/day, 100 transactions/month")
                        .aiChatsPerDay(2)
                        .transactionsLimit(100)
                        .build(),

                SubscriptionPlanDTO.builder()
                        .name("PREMIUM")
                        .monthlyPrice(9.0)
                        .features("15 AI chats/day, Unlimited transactions, Priority support")
                        .badge("BEST VALUE")
                        .aiChatsPerDay(15)
                        .transactionsLimit(-1)
                        .build(),

                SubscriptionPlanDTO.builder()
                        .name("ENTERPRISE")
                        .monthlyPrice(19.0)
                        .features("30 AI chats/day, Unlimited transactions, Dedicated support, API access")
                        .aiChatsPerDay(30)
                        .transactionsLimit(-1)
                        .build()
        );
    }
}