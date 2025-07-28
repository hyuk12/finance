package com.finance.demo.pattern.domain;

import com.finance.demo.transaction.domain.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 패턴 분석 컨텍스트의 소비 패턴 애그리게이트 루트
 * 
 * 유비쿼터스 언어 정의:
 * - Pattern: 반복적으로 나타나는 소비 행동
 * - 속성: 주기, 금액범위, 카테고리, 신뢰도
 * - 규칙: 최소 3회 이상 반복되어야 패턴으로 인정
 */
@Entity
@Table(name = "spending_patterns")
public class SpendingPattern {
    
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
    private PatternType patternType;
    
    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal averageAmount;
    
    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal minAmount;
    
    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal maxAmount;
    
    /**
     * 패턴의 신뢰도 (0.0 ~ 1.0)
     */
    @NotNull
    @Column(precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    /**
     * 패턴이 발견된 거래 횟수
     */
    @NotNull
    private Integer occurrenceCount;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    protected SpendingPattern() {} // JPA를 위한 기본 생성자
    
    public SpendingPattern(Long userId, Category category, PatternType patternType,
                          BigDecimal averageAmount, BigDecimal minAmount, BigDecimal maxAmount,
                          BigDecimal confidenceScore, Integer occurrenceCount) {
        if (occurrenceCount < 3) {
            throw new IllegalArgumentException("패턴으로 인정받으려면 최소 3회 이상 반복되어야 합니다");
        }
        
        if (confidenceScore.compareTo(BigDecimal.ZERO) < 0 || 
            confidenceScore.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("신뢰도는 0.0과 1.0 사이여야 합니다");
        }
        
        this.userId = userId;
        this.category = category;
        this.patternType = patternType;
        this.averageAmount = averageAmount;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.confidenceScore = confidenceScore;
        this.occurrenceCount = occurrenceCount;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    
    // 도메인 비즈니스 메서드
    public void updatePattern(BigDecimal newAverageAmount, BigDecimal newMinAmount, 
                             BigDecimal newMaxAmount, BigDecimal newConfidenceScore) {
        this.averageAmount = newAverageAmount;
        this.minAmount = newMinAmount;
        this.maxAmount = newMaxAmount;
        this.confidenceScore = newConfidenceScore;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void incrementOccurrence() {
        this.occurrenceCount++;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public boolean isHighConfidence() {
        return this.confidenceScore.compareTo(new BigDecimal("0.8")) >= 0;
    }
    
    public boolean isStablePattern() {
        return this.occurrenceCount >= 5 && isHighConfidence();
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Category getCategory() { return category; }
    public PatternType getPatternType() { return patternType; }
    public BigDecimal getAverageAmount() { return averageAmount; }
    public BigDecimal getMinAmount() { return minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public Integer getOccurrenceCount() { return occurrenceCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
