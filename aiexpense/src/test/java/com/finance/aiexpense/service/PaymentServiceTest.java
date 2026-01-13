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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStrategyFactory strategyFactory;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private PaymentStrategy paymentStrategy;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;
    private Payment payment;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .subscriptionTier(SubscriptionTier.FREE)
                .build();

        paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(9.99);
        paymentRequest.setPaymentMethod("CREDIT_CARD");
        paymentRequest.setSubscriptionTier("PREMIUM");
        paymentRequest.setDurationMonths(1);

        paymentResponse = PaymentResponse.builder()
                .success(true)
                .orderId("order123")
                .paymentId("payment123")
                .transactionId("txn123")
                .amount(BigDecimal.valueOf(9.99))
                .currency("INR")
                .paymentGateway("RAZORPAY")
                .message("Order created")
                .build();

        payment = Payment.builder()
                .id(1L)
                .user(user)
                .orderId("order123")
                .transactionId("txn123")
                .amount(9.99)
                .currency("INR")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .subscriptionTier(SubscriptionTier.PREMIUM)
                .subscriptionDurationMonths(1)
                .build();
    }

    @Test
    void createPaymentOrder_success() {
        when(strategyFactory.getStrategy(anyString())).thenReturn(paymentStrategy);
        when(paymentStrategy.createPaymentOrder(any(PaymentRequest.class), anyString())).thenReturn(paymentResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse result = paymentService.createPaymentOrder(paymentRequest, user);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("order123", result.getOrderId());
        verify(strategyFactory).getStrategy("CREDIT_CARD");
        verify(paymentStrategy).createPaymentOrder(any(PaymentRequest.class), eq("test@example.com"));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void verifyAndCompletePayment_success() {
        when(paymentRepository.findByOrderId(anyString())).thenReturn(Optional.of(payment));
        when(strategyFactory.getStrategy(anyString())).thenReturn(paymentStrategy);
        when(paymentStrategy.verifyPayment(anyString(), anyString(), anyString())).thenReturn(paymentResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        doNothing().when(subscriptionService).upgradeSubscription(any(User.class), any(SubscriptionTier.class), anyInt());

        PaymentResponse result = paymentService.verifyAndCompletePayment("order123", "payment123", "signature123", user);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        verify(paymentRepository).findByOrderId("order123");
        verify(paymentStrategy).verifyPayment("order123", "payment123", "signature123");
        verify(subscriptionService).upgradeSubscription(user, SubscriptionTier.PREMIUM, 1);
    }

    @Test
    void verifyAndCompletePayment_paymentNotFound_throwsException() {
        when(paymentRepository.findByOrderId(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
                paymentService.verifyAndCompletePayment("order123", "payment123", "signature123", user));
        verify(paymentRepository).findByOrderId("order123");
        verify(paymentStrategy, never()).verifyPayment(anyString(), anyString(), anyString());
    }

    @Test
    void verifyAndCompletePayment_unauthorized_throwsException() {
        User otherUser = User.builder().id(2L).build();
        payment.setUser(otherUser);
        when(paymentRepository.findByOrderId(anyString())).thenReturn(Optional.of(payment));

        assertThrows(RuntimeException.class, () -> 
                paymentService.verifyAndCompletePayment("order123", "payment123", "signature123", user));
        verify(paymentRepository).findByOrderId("order123");
        verify(paymentStrategy, never()).verifyPayment(anyString(), anyString(), anyString());
    }

    @Test
    void verifyAndCompletePayment_verificationFailed() {
        PaymentResponse failedResponse = PaymentResponse.builder()
                .success(false)
                .message("Verification failed")
                .build();
        when(paymentRepository.findByOrderId(anyString())).thenReturn(Optional.of(payment));
        when(strategyFactory.getStrategy(anyString())).thenReturn(paymentStrategy);
        when(paymentStrategy.verifyPayment(anyString(), anyString(), anyString())).thenReturn(failedResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse result = paymentService.verifyAndCompletePayment("order123", "payment123", "signature123", user);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(subscriptionService, never()).upgradeSubscription(any(User.class), any(SubscriptionTier.class), anyInt());
    }

    @Test
    void getPaymentHistory_success() {
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(payments);

        List<Payment> result = paymentService.getPaymentHistory(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findByUserOrderByCreatedAtDesc(user);
    }

    @Test
    void getAvailablePlans_success() {
        List<SubscriptionPlanDTO> plans = paymentService.getAvailablePlans();

        assertNotNull(plans);
        assertEquals(3, plans.size());
        assertTrue(plans.stream().anyMatch(p -> p.getName().equals("FREE")));
        assertTrue(plans.stream().anyMatch(p -> p.getName().equals("PREMIUM")));
        assertTrue(plans.stream().anyMatch(p -> p.getName().equals("ENTERPRISE")));
    }
}

