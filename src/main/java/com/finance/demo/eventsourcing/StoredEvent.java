package com.finance.demo.eventsourcing;

import com.finance.demo.shared.domain.DomainEvent;
import java.time.LocalDateTime;

/**
 * Day 2: 이벤트 스토어에 저장되는 이벤트의 메타데이터를 포함한 래퍼
 * 
 * 저장되는 정보:
 * - 글로벌 시퀀스 번호 (전체 이벤트 순서)
 * - 애그리게이트 ID
 * - 애그리게이트 내 버전 번호
 * - 이벤트 타입
 * - 실제 도메인 이벤트 객체
 * - 저장 시각
 */
public record StoredEvent(
    long globalSequence,
    String aggregateId,
    int version,
    String eventType,
    DomainEvent domainEvent,
    LocalDateTime timestamp
) {
    
    public StoredEvent {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            throw new IllegalArgumentException("애그리게이트 ID는 필수입니다");
        }
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("이벤트 타입은 필수입니다");
        }
        if (domainEvent == null) {
            throw new IllegalArgumentException("도메인 이벤트는 필수입니다");
        }
        if (version < 0) {
            throw new IllegalArgumentException("버전은 0 이상이어야 합니다");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    /**
     * 이벤트가 특정 타입인지 확인
     */
    public boolean isEventType(Class<?> eventClass) {
        return eventType.equals(eventClass.getSimpleName());
    }
    
    /**
     * 이벤트가 특정 애그리게이트에 속하는지 확인
     */
    public boolean belongsToAggregate(String aggregateId) {
        return this.aggregateId.equals(aggregateId);
    }
    
    /**
     * 이벤트가 특정 버전 이후인지 확인
     */
    public boolean isAfterVersion(int version) {
        return this.version > version;
    }
}
