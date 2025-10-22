//package com.finance.aiexpense.payment.strategy;
//
//import com.finance.aiexpense.dto.PaymentRequest;
//import com.finance.aiexpense.dto.PaymentResponse;
//import com.razorpay.Order;
//import com.razorpay.RazorpayClient;
//import com.razorpay.RazorpayException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class RazorpayStrategy implements PaymentStrategy {
//
//    @Value("${razorpay.key.id}")
//    private String razorpayKeyId;
//
//    @Value("${razorpay.key.secret}")
//    private String razorpayKeySecret;
//
//    @Override
//    public PaymentResponse processPayment(PaymentRequest request) {
//        try {
//            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
//
//            // Create order
//            JSONObject orderRequest = new JSONObject();
//            orderRequest.put("amount", request.getAmount() * 100); // Amount in paise
//            orderRequest.put("currency", "INR");
//            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
//
//            Order order = razorpay.orders.create(orderRequest);
//
//            log.info("Razorpay order created: {}", order.get("id"));
//
//            return PaymentResponse.builder()
//                    .success(true)
//                    .transactionId(order.get("id"))
//                    .orderId(order.get("id"))
//                    .amount(request.getAmount())
//                    .currency("INR")
//                    .paymentMethod("RAZORPAY")
//                    .message("Order created successfully")
//                    .build();
//
//        } catch (RazorpayException e) {
//            log.error("Razorpay payment failed", e);
//            return PaymentResponse.builder()
//                    .success(false)
//                    .message("Payment failed: " + e.getMessage())
//                    .build();
//        }
//    }
//
//    @Override
//    public String getPaymentMethodName() {
//        return "RAZORPAY";
//    }
//
//    @Override
//    public boolean supports(String paymentMethod) {
//        return "DEBIT_CARD".equalsIgnoreCase(paymentMethod) ||
//                "CREDIT_CARD".equalsIgnoreCase(paymentMethod) ||
//                "UPI".equalsIgnoreCase(paymentMethod) ||
//                "NET_BANKING".equalsIgnoreCase(paymentMethod);
//    }
//}