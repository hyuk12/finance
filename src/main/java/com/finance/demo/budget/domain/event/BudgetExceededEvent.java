package com.finance.demo.budget.domain.event;

import com.finance.demo.shared.domain.DomainEvent;
import com.finance.demo.transaction.domain.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 이벤트 스토밍에서 도출된 "예산이 초과되었다" 이벤트
 * 
 * 이 이벤트는 알림 컨텍스트에서 구독하여 사용자에게 알림을 발송함
 */
public record BudgetExceededEvent(
        String eventId,
        LocalDateTime occurredOn,
        Long budgetId,
        Long userId,
        Category category,
        BigDecimal plannedAmount,
        BigDecimal spentAmount,
        BigDecimal excessAmount
) implements DomainEvent {
    
    public BudgetExceededEvent(Long budgetId, Long userId, Category category,
                             BigDecimal plannedAmount, BigDecimal spentAmount) {
        this(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            budgetId,
            userId,
            category,
            plannedAmount,
            spentAmount,
            spentAmount.subtract(plannedAmount)
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
}
