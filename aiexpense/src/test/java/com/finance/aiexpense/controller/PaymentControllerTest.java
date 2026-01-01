package com.finance.aiexpense.controller;

import com.finance.aiexpense.dto.*;
import com.finance.aiexpense.entity.Payment;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.enums.PaymentMethod;
import com.finance.aiexpense.enums.PaymentStatus;
import com.finance.aiexpense.enums.SubscriptionTier;
import com.finance.aiexpense.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private User mockUser;
    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponseSuccess;
    private PaymentResponse paymentResponseFailure;
    private PaymentVerificationRequest paymentVerificationRequest;
    private Payment mockPayment;
    private SubscriptionPlanDTO mockSubscriptionPlanDTO;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .subscriptionTier(SubscriptionTier.FREE)
                .build();

        paymentRequest = new PaymentRequest(
                BigDecimal.valueOf(9.99),
                "USD",
                PaymentMethod.CREDIT_CARD,
                SubscriptionTier.PREMIUM
        );

        paymentResponseSuccess = PaymentResponse.builder()
                .success(true)
                .message("Order created")
                .orderId("order123")
                .paymentId("payment123")
                .amount(BigDecimal.valueOf(9.99))
                .currency("USD")
                .build();

        paymentResponseFailure = PaymentResponse.builder()
                .success(false)
                .message("Payment failed")
                .build();

        paymentVerificationRequest = new PaymentVerificationRequest("order123", "payment123", "signature123");

        mockPayment = Payment.builder()
                .id(1L)
                .user(mockUser)
                .orderId("order123")
                .paymentId("payment123")
                .amount(BigDecimal.valueOf(9.99))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .subscriptionTier(SubscriptionTier.PREMIUM)
                .paymentDate(LocalDateTime.now())
                .build();

        mockSubscriptionPlanDTO = SubscriptionPlanDTO.builder()
                .tier(SubscriptionTier.PREMIUM)
                .price(BigDecimal.valueOf(9.99))
                .currency("USD")
                .features(Collections.singletonList("Premium features"))
                .build();
    }

    @Test
    void createPaymentOrder_success() {
        when(paymentService.createPaymentOrder(any(PaymentRequest.class), any(User.class))).thenReturn(paymentResponseSuccess);

        ResponseEntity<ApiResponse<PaymentResponse>> responseEntity = paymentController.createPaymentOrder(paymentRequest, mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Payment order created", responseEntity.getBody().getMessage());
        assertEquals(paymentResponseSuccess, responseEntity.getBody().getData());
    }

    @Test
    void createPaymentOrder_failure() {
        when(paymentService.createPaymentOrder(any(PaymentRequest.class), any(User.class))).thenReturn(paymentResponseFailure);

        ResponseEntity<ApiResponse<PaymentResponse>> responseEntity = paymentController.createPaymentOrder(paymentRequest, mockUser);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Payment failed", responseEntity.getBody().getMessage());
    }

    @Test
    void verifyPayment_success() {
        when(paymentService.verifyAndCompletePayment(anyString(), anyString(), anyString(), any(User.class)))
                .thenReturn(paymentResponseSuccess);

        ResponseEntity<ApiResponse<PaymentResponse>> responseEntity = paymentController.verifyPayment(paymentVerificationRequest, mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Payment verified successfully", responseEntity.getBody().getMessage());
        assertEquals(paymentResponseSuccess, responseEntity.getBody().getData());
    }

    @Test
    void verifyPayment_failure() {
        when(paymentService.verifyAndCompletePayment(anyString(), anyString(), anyString(), any(User.class)))
                .thenReturn(paymentResponseFailure);

        ResponseEntity<ApiResponse<PaymentResponse>> responseEntity = paymentController.verifyPayment(paymentVerificationRequest, mockUser);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Payment failed", responseEntity.getBody().getMessage());
    }

    @Test
    void getPaymentHistory_success() {
        List<Payment> payments = Collections.singletonList(mockPayment);
        when(paymentService.getPaymentHistory(any(User.class))).thenReturn(payments);

        ResponseEntity<ApiResponse<List<Payment>>> responseEntity = paymentController.getPaymentHistory(mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(payments, responseEntity.getBody().getData());
    }

    @Test
    void getPlans_success() {
        List<SubscriptionPlanDTO> plans = Collections.singletonList(mockSubscriptionPlanDTO);
        when(paymentService.getAvailablePlans()).thenReturn(plans);

        ResponseEntity<ApiResponse<List<SubscriptionPlanDTO>>> responseEntity = paymentController.getPlans();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(plans, responseEntity.getBody().getData());
    }
}

