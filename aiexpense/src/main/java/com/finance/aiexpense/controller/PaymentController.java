package com.finance.aiexpense.controller;

import com.finance.aiexpense.dto.*;
import com.finance.aiexpense.entity.Payment;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Payments", description = "Payment and subscription management")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create payment order")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPaymentOrder(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user) {

        PaymentResponse response = paymentService.createPaymentOrder(request, user);

        if (response.isSuccess()) {
            return ResponseEntity.ok(
                    ApiResponse.success("Payment order created", response)
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(response.getMessage())
            );
        }
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request,
            @AuthenticationPrincipal User user) {

        PaymentResponse response = paymentService.verifyAndCompletePayment(
                request.getOrderId(),
                request.getPaymentId(),
                request.getSignature(),
                user
        );

        if (response.isSuccess()) {
            return ResponseEntity.ok(
                    ApiResponse.success("Payment verified successfully", response)
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(response.getMessage())
            );
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get payment history")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentHistory(
            @AuthenticationPrincipal User user) {

        List<Payment> payments = paymentService.getPaymentHistory(user);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/plans")
    @Operation(summary = "Get subscription plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDTO>>> getPlans() {
        List<SubscriptionPlanDTO> plans = paymentService.getAvailablePlans();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
}