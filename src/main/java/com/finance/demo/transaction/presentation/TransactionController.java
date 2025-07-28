package com.finance.demo.transaction.presentation;

import com.finance.demo.transaction.application.TransactionService;
import com.finance.demo.transaction.domain.Transaction;
import com.finance.demo.transaction.domain.TransactionType;
import com.finance.demo.transaction.domain.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 거래 관리 REST API
 * 1일차 실습을 위한 컨트롤러
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    /**
     * 새로운 거래 생성
     * POST /api/transactions
     */
    @PostMapping
    public ResponseEntity<CreateTransactionResponse> createTransaction(
            @RequestBody CreateTransactionRequest request) {
        
        Long transactionId = transactionService.createTransaction(
            request.userId(),
            request.amount(),
            request.transactionDate(),
            request.description(),
            request.type(),
            request.category()
        );
        
        return ResponseEntity.ok(new CreateTransactionResponse(transactionId, "거래가 성공적으로 생성되었습니다"));
    }
    
    /**
     * 사용자별 거래 목록 조회
     * GET /api/transactions?userId={userId}
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam Long userId) {
        
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        List<TransactionResponse> response = transactions.stream()
            .map(this::toTransactionResponse)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 거래 재분류
     * PUT /api/transactions/{transactionId}/category
     */
    @PutMapping("/{transactionId}/category")
    public ResponseEntity<String> categorizeTransaction(
            @PathVariable Long transactionId,
            @RequestBody CategorizeRequest request) {
        
        transactionService.categorizeTransaction(transactionId, request.category());
        return ResponseEntity.ok("거래가 재분류되었습니다");
    }
    
    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getUserId(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getCategory(),
            transaction.getTransactionDate(),
            transaction.getDescription(),
            transaction.getCreatedAt()
        );
    }
    
    // Request/Response DTOs
    public record CreateTransactionRequest(
        Long userId,
        BigDecimal amount,
        LocalDateTime transactionDate,
        String description,
        TransactionType type,
        Category category
    ) {}
    
    public record CreateTransactionResponse(
        Long transactionId,
        String message
    ) {}
    
    public record TransactionResponse(
        Long id,
        Long userId,
        BigDecimal amount,
        TransactionType type,
        Category category,
        LocalDateTime transactionDate,
        String description,
        LocalDateTime createdAt
    ) {}
    
    public record CategorizeRequest(
        Category category
    ) {}
}
