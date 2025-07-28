package com.finance.demo.transaction.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래 데이터 컨텍스트의 거래 애그리게이트 루트
 * 
 * 유비쿼터스 언어 정의:
 * - Transaction: 사용자의 금융 계좌에서 발생하는 입금 또는 출금 기록
 * - 속성: 금액, 일시, 카테고리, 설명, 계좌정보
 * - 규칙: 모든 거래는 반드시 하나의 카테고리에 속해야 함
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    private Long userId; // 계정 관리 컨텍스트와의 연결점
    
    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    
    @NotNull
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    
    @NotNull
    private String description;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private Category category;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    protected Transaction() {} // JPA를 위한 기본 생성자
    
    public Transaction(Long userId, BigDecimal amount, LocalDateTime transactionDate, 
                      String description, TransactionType type, Category category) {
        if (amount == null || amount.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("거래 금액은 0이 될 수 없습니다");
        }
        
        this.userId = userId;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.type = type;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }
    
    // 도메인 비즈니스 메서드
    public void categorize(Category newCategory) {
        if (newCategory == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다");
        }
        this.category = newCategory;
    }
    
    public boolean isExpense() {
        return this.type == TransactionType.EXPENSE;
    }
    
    public boolean isIncome() {
        return this.type == TransactionType.INCOME;
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getDescription() { return description; }
    public TransactionType getType() { return type; }
    public Category getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
