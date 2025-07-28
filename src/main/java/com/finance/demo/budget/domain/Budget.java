package com.finance.demo.budget.domain;

import com.finance.demo.transaction.domain.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 예산 관리를 위한 예산 애그리게이트 루트
 * 
 * 유비쿼터스 언어 정의:
 * - Budget: 특정 기간 동안 카테고리별 지출 계획
 * - 속성: 기간, 카테고리, 계획금액, 실제금액
 * - 규칙: 월별 또는 주별 단위로만 설정 가능
 */
@Entity
@Table(name = "budgets")
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    private Long userId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private Category category;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private BudgetPeriod period;
    
    @NotNull
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @NotNull
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @NotNull
    @Column(name = "planned_amount", precision = 19, scale = 2)
    private BigDecimal plannedAmount;
    
    @Column(name = "spent_amount", precision = 19, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    protected Budget() {} // JPA를 위한 기본 생성자
    
    public Budget(Long userId, Category category, BudgetPeriod period,
                  LocalDate startDate, BigDecimal plannedAmount) {
        if (plannedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("예산 금액은 0보다 커야 합니다");
        }
        
        this.userId = userId;
        this.category = category;
        this.period = period;
        this.startDate = startDate;
        this.endDate = period.calculateEndDate(startDate);
        this.plannedAmount = plannedAmount;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    
    // 도메인 비즈니스 메서드
    public void addSpending(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("지출 금액은 음수가 될 수 없습니다");
        }
        
        this.spentAmount = this.spentAmount.add(amount);
        this.lastUpdated = LocalDateTime.now();
    }
    
    public BigDecimal getRemainingAmount() {
        return plannedAmount.subtract(spentAmount);
    }
    
    public BigDecimal getUsagePercentage() {
        if (plannedAmount.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return spentAmount.divide(plannedAmount, 4, BigDecimal.ROUND_HALF_UP)
                         .multiply(new BigDecimal("100"));
    }
    
    public boolean isOverBudget() {
        return spentAmount.compareTo(plannedAmount) > 0;
    }
    
    public boolean isNearBudgetLimit() {
        BigDecimal usagePercentage = getUsagePercentage();
        return usagePercentage.compareTo(new BigDecimal("80")) >= 0 && !isOverBudget();
    }
    
    public boolean isPeriodActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Category getCategory() { return category; }
    public BudgetPeriod getPeriod() { return period; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getPlannedAmount() { return plannedAmount; }
    public BigDecimal getSpentAmount() { return spentAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
