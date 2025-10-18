package com.finance.aiexpense.controller;

import com.finance.aiexpense.dto.ApiResponse;
import com.finance.aiexpense.dto.TransactionDTO;
import com.finance.aiexpense.dto.TransactionRequest;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/add")
    @Operation(summary = "Add new transaction")
    public ResponseEntity<ApiResponse<TransactionDTO>> addTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal User user) {
        TransactionDTO transaction = transactionService.addTransaction(request, user);
        return ResponseEntity.ok(ApiResponse.success("Transaction added successfully", transaction));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all user transactions")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getAllTransactions(
            @AuthenticationPrincipal User user) {
        List<TransactionDTO> transactions = transactionService.getAllTransactions(user);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get transactions by date range")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal User user) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(
                user, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent transactions")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getRecentTransactions(
            @RequestParam(defaultValue = "3") int months,
            @AuthenticationPrincipal User user) {
        List<TransactionDTO> transactions = transactionService.getRecentTransactions(user, months);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        transactionService.deleteTransaction(id, user);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully", null));
    }

    @PutMapping("/{id}/category")
    @Operation(summary = "Update transaction category")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateCategory(
            @PathVariable Long id,
            @RequestParam String category,
            @AuthenticationPrincipal User user) {
        TransactionDTO transaction = transactionService.updateTransactionCategory(id, category, user);
        return ResponseEntity.ok(ApiResponse.success("Category updated", transaction));
    }
}