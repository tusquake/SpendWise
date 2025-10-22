package com.finance.aiexpense.repository;

import com.finance.aiexpense.entity.Payment;
import com.finance.aiexpense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByUserOrderByCreatedAtDesc(User user);

    Optional<Payment> findByOrderId(String orderId);
}