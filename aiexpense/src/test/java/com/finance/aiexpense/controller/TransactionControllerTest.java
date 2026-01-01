package com.finance.aiexpense.controller;

import com.finance.aiexpense.dto.ApiResponse;
import com.finance.aiexpense.dto.TransactionDTO;
import com.finance.aiexpense.dto.TransactionRequest;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.enums.SubscriptionTier;
import com.finance.aiexpense.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private User mockUser;
    private TransactionRequest transactionRequest;
    private TransactionDTO transactionDTO;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .subscriptionTier(SubscriptionTier.FREE)
                .build();

        transactionRequest = new TransactionRequest(
                "Grocery shopping",
                BigDecimal.valueOf(50.00),
                "Food",
                LocalDate.now()
        );

        transactionDTO = new TransactionDTO(
                1L,
                "Grocery shopping",
                BigDecimal.valueOf(50.00),
                "Food",
                LocalDate.now(),
                mockUser.getId()
        );
    }

    @Test
    void addTransaction_success() {
        when(transactionService.addTransaction(any(TransactionRequest.class), any(User.class))).thenReturn(transactionDTO);

        ResponseEntity<ApiResponse<TransactionDTO>> responseEntity = transactionController.addTransaction(transactionRequest, mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Transaction added successfully", responseEntity.getBody().getMessage());
        assertEquals(transactionDTO, responseEntity.getBody().getData());
    }

    @Test
    void getAllTransactions_success() {
        List<TransactionDTO> transactions = Collections.singletonList(transactionDTO);
        when(transactionService.getAllTransactions(any(User.class))).thenReturn(transactions);

        ResponseEntity<ApiResponse<List<TransactionDTO>>> responseEntity = transactionController.getAllTransactions(mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(transactions, responseEntity.getBody().getData());
    }

    @Test
    void getTransactionsByDateRange_success() {
        List<TransactionDTO> transactions = Collections.singletonList(transactionDTO);
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        when(transactionService.getTransactionsByDateRange(any(User.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(transactions);

        ResponseEntity<ApiResponse<List<TransactionDTO>>> responseEntity = transactionController.getTransactionsByDateRange(startDate, endDate, mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(transactions, responseEntity.getBody().getData());
    }

    @Test
    void getRecentTransactions_success() {
        List<TransactionDTO> transactions = Collections.singletonList(transactionDTO);
        int months = 3;
        when(transactionService.getRecentTransactions(any(User.class), anyInt())).thenReturn(transactions);

        ResponseEntity<ApiResponse<List<TransactionDTO>>> responseEntity = transactionController.getRecentTransactions(months, mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(transactions, responseEntity.getBody().getData());
    }

    @Test
    void deleteTransaction_success() {
        doNothing().when(transactionService).deleteTransaction(anyLong(), any(User.class));

        ResponseEntity<ApiResponse<Void>> responseEntity = transactionController.deleteTransaction(1L, mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Transaction deleted successfully", responseEntity.getBody().getMessage());
    }

    @Test
    void updateCategory_success() {
        TransactionDTO updatedTransactionDTO = new TransactionDTO(
                1L,
                "Grocery shopping",
                BigDecimal.valueOf(50.00),
                "Updated Category",
                LocalDate.now(),
                mockUser.getId()
        );
        when(transactionService.updateTransactionCategory(anyLong(), anyString(), any(User.class)))
                .thenReturn(updatedTransactionDTO);

        ResponseEntity<ApiResponse<TransactionDTO>> responseEntity = transactionController.updateCategory(1L, "Updated Category", mockUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Category updated", responseEntity.getBody().getMessage());
        assertEquals(updatedTransactionDTO, responseEntity.getBody().getData());
    }
}

