package com.finance.demo.transaction.domain.event;

import com.finance.demo.shared.domain.DomainEvent;
import com.finance.demo.transaction.domain.Category;
import com.finance.demo.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 이벤트 스토밍에서 도출된 "거래가 생성되었다" 이벤트
 * 
 * 이 이벤트는 다른 바운디드 컨텍스트에서 구독할 수 있음:
 * - 패턴 분석 컨텍스트: 새로운 거래를 분석에 반영
 * - 예산 관리 컨텍스트: 예산 사용량 업데이트
 * - 알림 컨텍스트: 예산 초과 시 알림 발송
 */
public record TransactionCreatedEvent(
        String eventId,
        LocalDateTime occurredOn,
        Long transactionId,
        Long userId,
        BigDecimal amount,
        TransactionType type,
        Category category,
        LocalDateTime transactionDate,
        String description
) implements DomainEvent {
    
    public TransactionCreatedEvent(Long transactionId, Long userId, BigDecimal amount,
                                 TransactionType type, Category category,
                                 LocalDateTime transactionDate, String description) {
        this(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            transactionId,
            userId,
            amount,
            type,
            category,
            transactionDate,
            description
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
