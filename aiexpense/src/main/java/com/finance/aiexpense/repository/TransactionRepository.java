package com.finance.aiexpense.repository;

import com.finance.aiexpense.entity.Transaction;
import com.finance.aiexpense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserOrderByDateDesc(User user);

    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(
            User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
            "AND t.date >= :startDate ORDER BY t.date DESC")
    List<Transaction> findRecentTransactions(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user " +
            "AND t.date BETWEEN :startDate AND :endDate")
    Double getTotalSpendingByPeriod(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY t.category")
    List<Object[]> getSpendingByCategory(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Transaction> findByUserAndDateAfterOrderByDateDesc(User user, LocalDate fromDate);
}