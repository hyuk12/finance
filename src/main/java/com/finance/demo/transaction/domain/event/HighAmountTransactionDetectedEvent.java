package com.finance.demo.transaction.domain.event;

import com.finance.demo.shared.domain.DomainEvent;
import com.finance.demo.transaction.domain.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Day 2: "고액 거래가 감지되었다" 이벤트
 * 
 * 비즈니스 규칙:
 * - 50만원 이상의 거래는 고액으로 분류
 * - 사용자 평균 지출의 3배 이상인 경우 고액으로 분류
 * - 알림 시스템에서 즉시 알림 발송
 * - 보안 시스템에서 추가 검증 수행
 */
public record HighAmountTransactionDetectedEvent(
        String eventId,
        LocalDateTime occurredOn,
        Long transactionId,
        Long userId,
        BigDecimal amount,
        Category category,
        RiskLevel riskLevel,
        String detectionReason,
        BigDecimal userAverageAmount
) implements DomainEvent {
    
    public HighAmountTransactionDetectedEvent(Long transactionId, Long userId,
                                            BigDecimal amount, Category category,
                                            RiskLevel riskLevel, String detectionReason,
                                            BigDecimal userAverageAmount) {
        this(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            transactionId,
            userId,
            amount,
            category,
            riskLevel,
            detectionReason,
            userAverageAmount
        );
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
    
    /**
     * 위험도 수준 열거형
     */
    public enum RiskLevel {
        LOW("낮음", 1),
        MEDIUM("보통", 2), 
        HIGH("높음", 3),
        CRITICAL("위험", 4);
        
        private final String description;
        private final int level;
        
        RiskLevel(String description, int level) {
            this.description = description;
            this.level = level;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getLevel() {
            return level;
        }
        
        public boolean isHigherThan(RiskLevel other) {
            return this.level > other.level;
        }
    }
    
    /**
     * 즉시 알림이 필요한 위험도인지 확인
     */
    public boolean requiresImmediateAlert() {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL;
    }
    
    /**
     * 추가 보안 검증이 필요한지 확인
     */
    public boolean requiresSecurityVerification() {
        return riskLevel == RiskLevel.CRITICAL || 
               amount.compareTo(new BigDecimal("1000000")) >= 0;
    }
}
