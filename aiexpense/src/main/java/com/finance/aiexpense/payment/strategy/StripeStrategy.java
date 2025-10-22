//package com.finance.aiexpense.payment.strategy;
//
//import com.finance.aiexpense.dto.PaymentRequest;
//import com.finance.aiexpense.dto.PaymentResponse;
//import com.stripe.Stripe;
//import com.stripe.exception.StripeException;
//import com.stripe.model.PaymentIntent;
//import com.stripe.param.PaymentIntentCreateParams;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class StripeStrategy implements PaymentStrategy {
//
//    @Value("${stripe.api.key}")
//    private String stripeApiKey;
//
//    @Override
//    public PaymentResponse processPayment(PaymentRequest request) {
//        try {
//            Stripe.apiKey = stripeApiKey;
//
//            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
//                    .setAmount((long) (request.getAmount() * 100)) // Amount in cents
//                    .setCurrency("inr")
//                    .setDescription("Premium Subscription - " + request.getSubscriptionTier())
//                    .build();
//
//            PaymentIntent intent = PaymentIntent.create(params);
//
//            log.info("Stripe payment intent created: {}", intent.getId());
//
//            return PaymentResponse.builder()
//                    .success(true)
//                    .transactionId(intent.getId())
//                    .clientSecret(intent.getClientSecret())
//                    .amount(request.getAmount())
//                    .currency("INR")
//                    .paymentMethod("STRIPE")
//                    .message("Payment intent created successfully")
//                    .build();
//
//        } catch (StripeException e) {
//            log.error("Stripe payment failed", e);
//            return PaymentResponse.builder()
//                    .success(false)
//                    .message("Payment failed: " + e.getMessage())
//                    .build();
//        }
//    }
//
//    @Override
//    public String getPaymentMethodName() {
//        return "STRIPE";
//    }
//
//    @Override
//    public PaymentResponse createPaymentOrder(PaymentRequest request, String userId) {
//        return null;
//    }
//
//    @Override
//    public PaymentResponse verifyPayment(String orderId, String paymentId, String signature) {
//        return null;
//    }
//
//    @Override
//    public String getGatewayName() {
//        return "";
//    }
//
//    @Override
//    public boolean supports(String paymentMethod) {
//        return "CREDIT_CARD".equalsIgnoreCase(paymentMethod) ||
//                "DEBIT_CARD".equalsIgnoreCase(paymentMethod);
//    }
//}
