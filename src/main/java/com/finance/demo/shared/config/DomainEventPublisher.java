package com.finance.demo.shared.config;

import com.finance.demo.shared.domain.AggregateRoot;
import com.finance.demo.shared.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.List;

/**
 * Day 2: 도메인 이벤트 발행자
 * 
 * 도메인 이벤트 발행 패턴:
 * 1. 애그리게이트에서 이벤트 수집
 * 2. 트랜잭션 커밋 직전에 일괄 발행
 * 3. 발행 후 이벤트 목록 정리
 * 
 * 특징:
 * - 트랜잭션 커밋 직전 발행으로 일관성 보장
 * - 실패 시 롤백과 함께 이벤트도 발행되지 않음
 * - 비동기 이벤트 핸들러 지원
 * - 구조화된 로깅으로 추적 가능
 */
@Component
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);
    
    private final ApplicationEventPublisher eventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 애그리게이트가 가진 모든 도메인 이벤트를 발행합니다
     * 트랜잭션 커밋 직전에 실행됩니다
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishEvents(AggregateRoot aggregate) {
        if (aggregate == null || !aggregate.hasUnpublishedEvents()) {
            return;
        }

        List<DomainEvent> events = aggregate.getDomainEvents();
        
        log.info("애그리게이트 {}에서 {}개의 도메인 이벤트 발행 시작", 
                aggregate.getClass().getSimpleName(), events.size());

        for (DomainEvent event : events) {
            try {
                publishSingleEvent(event);
            } catch (Exception e) {
                log.error("도메인 이벤트 발행 실패: {}", event.getClass().getSimpleName(), e);
                // 이벤트 발행 실패 시 트랜잭션 롤백을 위해 예외 재발생
                throw new DomainEventPublishingException("도메인 이벤트 발행에 실패했습니다", e);
            }
        }

        // 이벤트 발행 완료 후 정리
        aggregate.clearDomainEvents();
        
        log.info("모든 도메인 이벤트 발행 완료");
    }

    /**
     * 단일 도메인 이벤트를 발행합니다
     */
    public void publishSingleEvent(DomainEvent event) {
        if (event == null) {
            log.warn("null 이벤트 발행 시도됨");
            return;
        }

        log.debug("도메인 이벤트 발행: {} (ID: {}, 발생시간: {})", 
                event.getClass().getSimpleName(), 
                event.getEventId(), 
                event.getOccurredOn());

        try {
            eventPublisher.publishEvent(event);
            
            log.info("도메인 이벤트 발행 성공: {}", event.getClass().getSimpleName());
            
        } catch (Exception e) {
            log.error("도메인 이벤트 발행 중 오류 발생: {}", event.getClass().getSimpleName(), e);
            throw e;
        }
    }

    /**
     * 여러 애그리게이트의 이벤트를 일괄 발행합니다
     */
    public void publishEventsFromAggregates(List<AggregateRoot> aggregates) {
        if (aggregates == null || aggregates.isEmpty()) {
            return;
        }

        log.info("{}개 애그리게이트에서 이벤트 일괄 발행 시작", aggregates.size());

        int totalEventCount = 0;
        
        for (AggregateRoot aggregate : aggregates) {
            if (aggregate.hasUnpublishedEvents()) {
                publishEvents(aggregate);
                totalEventCount += aggregate.getUnpublishedEventCount();
            }
        }

        log.info("총 {}개의 도메인 이벤트 일괄 발행 완료", totalEventCount);
    }

    /**
     * 도메인 이벤트 발행 예외
     */
    public static class DomainEventPublishingException extends RuntimeException {
        public DomainEventPublishingException(String message) {
            super(message);
        }

        public DomainEventPublishingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
