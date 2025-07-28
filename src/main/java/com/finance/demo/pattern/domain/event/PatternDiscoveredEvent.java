package com.finance.demo.pattern.domain.event;

import com.finance.demo.shared.domain.DomainEvent;
import com.finance.demo.transaction.domain.Category;
import com.finance.demo.pattern.domain.PatternType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 이벤트 스토밍에서 도출된 "패턴이 발견되었다" 이벤트
 * 
 * 이 이벤트는 알림 컨텍스트에서 구독하여 사용자에게 인사이트를 제공함
 */
public record PatternDiscoveredEvent(
        String eventId,
        LocalDateTime occurredOn,
        Long patternId,
        Long userId,
        Category category,
        PatternType patternType,
        BigDecimal averageAmount,
        BigDecimal confidenceScore,
        Integer occurrenceCount
) implements DomainEvent {
    
    public PatternDiscoveredEvent(Long patternId, Long userId, Category category,
                                PatternType patternType, BigDecimal averageAmount,
                                BigDecimal confidenceScore, Integer occurrenceCount) {
        this(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            patternId,
            userId,
            category,
            patternType,
            averageAmount,
            confidenceScore,
            occurrenceCount
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
