package com.finance.demo.eventsourcing;

import com.finance.demo.shared.domain.DomainEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Day 2: 인메모리 이벤트 스토어 구현체
 * <p>
 * 실제 운영 환경에서는 데이터베이스나 전용 이벤트 스토어를 사용하지만,
 * 학습 목적으로 간단한 인메모리 구현체를 제공합니다.
 * <p>
 * 특징:
 * - 스레드 세이프한 동시성 처리
 * - 낙관적 동시성 제어 (버전 기반)
 * - 글로벌 이벤트 순서 보장
 * - 타입별, 시간별 이벤트 조회 지원
 */
@Component
public class InMemoryEventStore implements EventStore {

    // 애그리게이트별 이벤트 저장소
    private final Map<String, List<StoredEvent>> eventStore = new ConcurrentHashMap<>();

    // 글로벌 이벤트 순서를 위한 시퀀스
    private final AtomicLong globalSequence = new AtomicLong(0);

    // 전체 이벤트 로그 (시간순 정렬을 위해)
    private final List<StoredEvent> globalEventLog = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void saveEvents(String aggregateId, List<DomainEvent> events, int expectedVersion) {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            throw new IllegalArgumentException("Aggregate ID는 필수입니다");
        }

        if (events == null || events.isEmpty()) {
            return; // 저장할 이벤트가 없으면 아무것도 하지 않음
        }

        synchronized (this) {
            List<StoredEvent> aggregateEvents = eventStore.computeIfAbsent(
                    aggregateId,
                    k -> Collections.synchronizedList(new ArrayList<>())
            );

            // 동시성 체크 - 예상 버전과 현재 버전이 일치하는지 확인
            int currentVersion = aggregateEvents.size();
            if (currentVersion != expectedVersion) {
                throw new ConcurrencyException(aggregateId, expectedVersion, currentVersion);
            }

            // 이벤트들을 순차적으로 저장
            for (DomainEvent event : events) {
                int newVersion = aggregateEvents.size() + 1;

                StoredEvent storedEvent = new StoredEvent(
                        globalSequence.incrementAndGet(),
                        aggregateId,
                        newVersion,
                        event.getClass().getSimpleName(),
                        event,
                        event.getOccurredOn()
                );

                aggregateEvents.add(storedEvent);
                globalEventLog.add(storedEvent);
            }
        }
    }

    @Override
    public List<DomainEvent> getEventsForAggregate(String aggregateId) {
        return getEventsForAggregate(aggregateId, 0);
    }

    @Override
    public List<DomainEvent> getEventsForAggregate(String aggregateId, int fromVersion) {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<StoredEvent> aggregateEvents = eventStore.getOrDefault(aggregateId, Collections.emptyList());

        return aggregateEvents.stream()
                .filter(storedEvent -> storedEvent.version() > fromVersion)
                .map(StoredEvent::domainEvent)
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainEvent> getAllEvents() {
        return globalEventLog.stream()
                .sorted(Comparator.comparing(StoredEvent::globalSequence))
                .map(StoredEvent::domainEvent)
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainEvent> getEventsByType(Class<? extends DomainEvent> eventType) {
        if (eventType == null) {
            return Collections.emptyList();
        }

        return globalEventLog.stream()
                .filter(storedEvent -> eventType.isInstance(storedEvent.domainEvent()))
                .sorted(Comparator.comparing(StoredEvent::globalSequence))
                .map(StoredEvent::domainEvent)
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainEvent> getEventsByTimeRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("시작 시간과 종료 시간은 필수입니다");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다");
        }

        return globalEventLog.stream()
                .filter(storedEvent -> {
                    LocalDateTime eventTime = storedEvent.timestamp();
                    return !eventTime.isBefore(from) && !eventTime.isAfter(to);
                })
                .sorted(Comparator.comparing(StoredEvent::timestamp))
                .map(StoredEvent::domainEvent)
                .collect(Collectors.toList());
    }

    @Override
    public int getCurrentVersion(String aggregateId) {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            return 0;
        }

        List<StoredEvent> aggregateEvents = eventStore.getOrDefault(aggregateId, Collections.emptyList());
        return aggregateEvents.size();
    }

    @Override
    public long getTotalEventCount() {
        return globalEventLog.size();
    }

    /**
     * 개발/테스트 목적으로 모든 이벤트를 제거합니다
     */
    public void clear() {
        eventStore.clear();
        globalEventLog.clear();
        globalSequence.set(0);
    }

    /**
     * 특정 애그리게이트의 이벤트만 제거합니다
     */
    public void clearAggregate(String aggregateId) {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            return;
        }

        List<StoredEvent> removedEvents = eventStore.remove(aggregateId);
        if (removedEvents != null) {
            globalEventLog.removeAll(removedEvents);
        }
    }

    /**
     * 디버깅을 위한 이벤트 스토어 상태 출력
     */
    public void printEventStore() {
        System.out.println("=== Event Store Status ===");
        System.out.println("Total events: " + getTotalEventCount());
        System.out.println("Aggregates: " + eventStore.size());

        eventStore.forEach((aggregateId, events) -> {
            System.out.println(String.format("Aggregate %s: %d events", aggregateId, events.size()));
        });

        System.out.println("========================");
    }

    /**
     * 스냅샷 스토어의 총 메모리 사용량을 추정합니다 (대략적)
     */
    public long estimateMemoryUsage() {
        return eventStore.values().stream()
                .mapToLong(List::size)
                .sum() * 1024; // 대략적인 추정값
    }
}
