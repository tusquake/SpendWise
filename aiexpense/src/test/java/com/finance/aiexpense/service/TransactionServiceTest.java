package com.finance.aiexpense.service;

import com.finance.aiexpense.dto.TransactionDTO;
import com.finance.aiexpense.dto.TransactionRequest;
import com.finance.aiexpense.entity.Transaction;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.enums.SubscriptionTier;
import com.finance.aiexpense.exception.ResourceNotFoundException;
import com.finance.aiexpense.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private TransactionRequest transactionRequest;
    private Transaction transaction;
    private TransactionDTO transactionDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .subscriptionTier(SubscriptionTier.FREE)
                .build();

        transactionRequest = new TransactionRequest();
        transactionRequest.setDescription("Grocery shopping");
        transactionRequest.setAmount(100.0);
        transactionRequest.setDate(LocalDate.now());
        transactionRequest.setCategory("Groceries");
        transactionRequest.setPaymentMode("Credit Card");

        transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .description("Grocery shopping")
                .amount(100.0)
                .date(LocalDate.now())
                .category("Groceries")
                .paymentMode("Credit Card")
                .build();

        transactionDTO = new TransactionDTO();
        transactionDTO.setId(1L);
        transactionDTO.setDescription("Grocery shopping");
        transactionDTO.setAmount(100.0);
        transactionDTO.setDate(LocalDate.now());
        transactionDTO.setCategory("Groceries");
    }

    @Test
    void addTransaction_success() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDTO.class))).thenReturn(transactionDTO);

        TransactionDTO result = transactionService.addTransaction(transactionRequest, user);

        assertNotNull(result);
        assertEquals(transactionDTO.getId(), result.getId());
        verify(transactionRepository).save(any(Transaction.class));
        verify(modelMapper).map(any(Transaction.class), eq(TransactionDTO.class));
    }

    @Test
    void getAllTransactions_success() {
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findByUserOrderByDateDesc(user)).thenReturn(transactions);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDTO.class))).thenReturn(transactionDTO);

        List<TransactionDTO> result = transactionService.getAllTransactions(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findByUserOrderByDateDesc(user);
    }

    @Test
    void getRecentTransactions_success() {
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findByUserAndDateAfterOrderByDateDesc(any(User.class), any(LocalDate.class)))
                .thenReturn(transactions);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDTO.class))).thenReturn(transactionDTO);

        List<TransactionDTO> result = transactionService.getRecentTransactions(user, 3);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findByUserAndDateAfterOrderByDateDesc(any(User.class), any(LocalDate.class));
    }

    @Test
    void getTransactionsByDateRange_success() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findByUserAndDateBetweenOrderByDateDesc(any(User.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(transactions);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDTO.class))).thenReturn(transactionDTO);

        List<TransactionDTO> result = transactionService.getTransactionsByDateRange(user, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findByUserAndDateBetweenOrderByDateDesc(any(User.class), eq(startDate), eq(endDate));
    }

    @Test
    void deleteTransaction_success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        doNothing().when(transactionRepository).delete(any(Transaction.class));

        transactionService.deleteTransaction(1L, user);

        verify(transactionRepository).findById(1L);
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_notFound_throwsException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(1L, user));
        verify(transactionRepository).findById(1L);
        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    @Test
    void deleteTransaction_unauthorized_throwsException() {
        User otherUser = User.builder().id(2L).build();
        Transaction otherUserTransaction = Transaction.builder().id(1L).user(otherUser).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(otherUserTransaction));

        assertThrows(RuntimeException.class, () -> transactionService.deleteTransaction(1L, user));
        verify(transactionRepository).findById(1L);
        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    @Test
    void updateTransactionCategory_success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDTO.class))).thenReturn(transactionDTO);

        TransactionDTO result = transactionService.updateTransactionCategory(1L, "Food", user);

        assertNotNull(result);
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(transaction);
        assertEquals("Food", transaction.getCategory());
    }

    @Test
    void updateTransactionCategory_notFound_throwsException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.updateTransactionCategory(1L, "Food", user));
        verify(transactionRepository).findById(1L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void updateTransactionCategory_unauthorized_throwsException() {
        User otherUser = User.builder().id(2L).build();
        Transaction otherUserTransaction = Transaction.builder().id(1L).user(otherUser).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(otherUserTransaction));

        assertThrows(RuntimeException.class, () -> transactionService.updateTransactionCategory(1L, "Food", user));
        verify(transactionRepository).findById(1L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}

