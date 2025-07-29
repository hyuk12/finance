package com.finance.demo.transaction.domain.event;

import com.finance.demo.shared.domain.DomainEvent;
import com.finance.demo.transaction.domain.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Day 2: "거래가 재분류되었다" 이벤트
 * 
 * 이벤트 스토밍에서 도출된 이벤트:
 * - 사용자가 거래의 카테고리를 변경했을 때 발생
 * - 패턴 분석 시스템에서 기존 패턴을 재계산해야 함
 * - 예산 관리 시스템에서 카테고리별 사용량을 업데이트해야 함
 */
public record TransactionRecategorizedEvent(
        String eventId,
        LocalDateTime occurredOn,
        Long transactionId,
        Long userId,
        Category previousCategory,
        Category newCategory,
        BigDecimal amount,
        String reason
) implements DomainEvent {
    
    public TransactionRecategorizedEvent(Long transactionId, Long userId, 
                                       Category previousCategory, Category newCategory,
                                       BigDecimal amount, String reason) {
        this(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            transactionId,
            userId,
            previousCategory,
            newCategory,
            amount,
            reason != null ? reason : "사용자 수동 재분류"
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
     * 카테고리가 실제로 변경되었는지 확인
     */
    public boolean isCategoryActuallyChanged() {
        return !previousCategory.equals(newCategory);
    }
    
    /**
     * 지출 패턴에 영향을 주는 변경인지 확인
     */
    public boolean affectsSpendingPattern() {
        return isCategoryActuallyChanged() && 
               (previousCategory.isExpenseCategory() || newCategory.isExpenseCategory());
    }
}
