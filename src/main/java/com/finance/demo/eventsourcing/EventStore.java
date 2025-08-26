package com.finance.demo.eventsourcing;

import com.finance.demo.shared.domain.DomainEvent;

import java.util.List;

/**
 * Day 2: 이벤트 소싱을 위한 이벤트 스토어 인터페이스
 * 
 * 이벤트 스토어의 역할:
 * - 도메인 이벤트를 순서대로 저장
 * - 애그리게이트별 이벤트 히스토리 조회
 * - 동시성 충돌 감지 및 처리
 * - 이벤트 재생을 통한 상태 복원
 */
public interface EventStore {
    
    /**
     * 애그리게이트의 이벤트들을 저장합니다
     * 
     * @param aggregateId 애그리게이트 식별자
     * @param events 저장할 이벤트 목록
     * @param expectedVersion 예상 버전 (낙관적 동시성 제어)
     * @throws ConcurrencyException 버전 충돌 시 발생
     */
    void saveEvents(String aggregateId, List<DomainEvent> events, int expectedVersion);
    
    /**
     * 애그리게이트의 모든 이벤트를 조회합니다
     */
    List<DomainEvent> getEventsForAggregate(String aggregateId);
    
    /**
     * 애그리게이트의 특정 버전 이후 이벤트를 조회합니다
     * 스냅샷 이후의 이벤트만 조회할 때 유용
     */
    List<DomainEvent> getEventsForAggregate(String aggregateId, int fromVersion);
    
    /**
     * 전체 이벤트 스트림을 조회합니다
     * 글로벌 이벤트 처리, 감사, 분석 등에 사용
     */
    List<DomainEvent> getAllEvents();
    
    /**
     * 특정 타입의 이벤트들만 조회합니다
     */
    List<DomainEvent> getEventsByType(Class<? extends DomainEvent> eventType);
    
    /**
     * 특정 기간의 이벤트들을 조회합니다
     */
    List<DomainEvent> getEventsByTimeRange(java.time.LocalDateTime from, java.time.LocalDateTime to);
    
    /**
     * 애그리게이트의 현재 버전을 조회합니다
     */
    int getCurrentVersion(String aggregateId);
    
    /**
     * 이벤트 스토어에 저장된 총 이벤트 수를 조회합니다
     */
    long getTotalEventCount();
}
