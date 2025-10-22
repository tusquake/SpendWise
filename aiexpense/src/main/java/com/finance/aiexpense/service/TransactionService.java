package com.finance.aiexpense.service;

import com.finance.aiexpense.dto.TransactionDTO;
import com.finance.aiexpense.dto.TransactionRequest;
import com.finance.aiexpense.entity.Transaction;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.exception.ResourceNotFoundException;
import com.finance.aiexpense.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @CacheEvict(value = {"transactions", "userStats", "insights"}, allEntries = true)
    public TransactionDTO addTransaction(TransactionRequest request, User user) {
        Transaction transaction = Transaction.builder()
                .user(user)
                .description(request.getDescription())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(request.getCategory())
                .paymentMode(request.getPaymentMode())
                .build();

        transaction = transactionRepository.save(transaction);
        return convertToDTO(transaction);
    }

    @Cacheable(value = "transactions", key = "#user.id")
    public List<TransactionDTO> getAllTransactions(User user) {
        return transactionRepository.findByUserOrderByDateDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ Get recent N months of transactions
    public List<TransactionDTO> getRecentTransactions(User user, int months) {
        LocalDate fromDate = LocalDate.now().minusMonths(months);
        return transactionRepository.findByUserAndDateAfterOrderByDateDesc(user, fromDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ Get transactions in a date range
    public List<TransactionDTO> getTransactionsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"transactions", "userStats", "insights"}, allEntries = true)
    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        transactionRepository.delete(transaction);
    }

    // ✅ Update transaction category
    @Transactional
    @CacheEvict(value = {"transactions", "userStats", "insights"}, allEntries = true)
    public TransactionDTO updateTransactionCategory(Long id, String category, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        transaction.setCategory(category);
        transaction = transactionRepository.save(transaction);
        return convertToDTO(transaction);
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        return modelMapper.map(transaction, TransactionDTO.class);
    }
}
