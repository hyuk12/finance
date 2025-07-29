package com.finance.demo.transaction.domain;

import com.finance.demo.shared.domain.AggregateRoot;
import com.finance.demo.transaction.domain.event.TransactionCreatedEvent;
import com.finance.demo.transaction.domain.event.TransactionRecategorizedEvent;
import com.finance.demo.transaction.domain.event.HighAmountTransactionDetectedEvent;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Day 2: 도메인 이벤트를 발행하는 거래 애그리게이트 루트
 * 
 * AggregateRoot를 상속하여 도메인 이벤트 관리 기능 추가:
 * - 거래 생성 시 TransactionCreatedEvent 발행
 * - 고액 거래 감지 시 HighAmountTransactionDetectedEvent 발행  
 * - 재분류 시 TransactionRecategorizedEvent 발행
 */
@Entity
@Table(name = "transactions")
public class Transaction extends AggregateRoot {
    
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
        
        // Day 2: 거래 생성 이벤트 발행
        this.raise(new TransactionCreatedEvent(
            this.id,
            this.userId,
            this.amount,
            this.type,
            this.category,
            this.transactionDate,
            this.description
        ));
        
        // Day 2: 고액 거래 감지
        if (isHighAmountTransaction(amount)) {
            this.raise(new HighAmountTransactionDetectedEvent(
                this.id,
                this.userId,
                this.amount,
                this.category,
                calculateRiskLevel(amount, category),
                "금액 기준 고액 거래 감지",
                BigDecimal.ZERO // 실제로는 사용자 평균 금액을 계산해야 함
            ));
        }
    }
    
    // 도메인 비즈니스 메서드
    public void categorize(Category newCategory) {
        if (newCategory == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다");
        }
        
        Category previousCategory = this.category;
        this.category = newCategory;
        
        // Day 2: 재분류 이벤트 발행
        if (!previousCategory.equals(newCategory)) {
            this.raise(new TransactionRecategorizedEvent(
                this.id,
                this.userId,
                previousCategory,
                newCategory,
                this.amount,
                "사용자 수동 재분류"
            ));
        }
    }
    
    public boolean isExpense() {
        return this.type == TransactionType.EXPENSE;
    }
    
    public boolean isIncome() {
        return this.type == TransactionType.INCOME;
    }
    
    // Day 2: 고액 거래 판단 로직
    private boolean isHighAmountTransaction(BigDecimal amount) {
        // 50만원 이상을 고액으로 분류
        return amount.compareTo(new BigDecimal("500000")) >= 0;
    }
    
    // Day 2: 위험도 계산 로직
    private HighAmountTransactionDetectedEvent.RiskLevel calculateRiskLevel(BigDecimal amount, Category category) {
        // 금액별 위험도 분류
        if (amount.compareTo(new BigDecimal("2000000")) >= 0) {
            return HighAmountTransactionDetectedEvent.RiskLevel.CRITICAL;
        } else if (amount.compareTo(new BigDecimal("1000000")) >= 0) {
            return HighAmountTransactionDetectedEvent.RiskLevel.HIGH;
        } else if (amount.compareTo(new BigDecimal("500000")) >= 0) {
            return HighAmountTransactionDetectedEvent.RiskLevel.MEDIUM;
        } else {
            return HighAmountTransactionDetectedEvent.RiskLevel.LOW;
        }
    }
    
    // Day 2: 도메인 쿼리 메서드들
    public boolean isWeekendTransaction() {
        int dayOfWeek = transactionDate.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7; // 토요일(6) 또는 일요일(7)
    }
    
    public boolean isLateNightTransaction() {
        int hour = transactionDate.getHour();
        return hour >= 22 || hour <= 5; // 오후 10시 ~ 오전 5시
    }
    
    public boolean isLargerThan(BigDecimal threshold) {
        return this.amount.compareTo(threshold) > 0;
    }
    
    public boolean isSameCategoryAs(Transaction other) {
        return this.category.equals(other.category);
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
