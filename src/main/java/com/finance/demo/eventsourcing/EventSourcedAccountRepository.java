package com.finance.demo.eventsourcing;

import com.finance.demo.shared.domain.DomainEvent;
import com.finance.demo.shared.domain.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Day 2: 이벤트 소싱 계좌 리포지토리
 * 
 * 전통적인 ORM 기반 리포지토리와 달리:
 * - 이벤트 스토어를 통해 이벤트 저장/조회
 * - 이벤트 재생을 통한 애그리게이트 복원
 * - 낙관적 동시성 제어
 * - 스냅샷 지원으로 성능 최적화
 */
@Repository
public class EventSourcedAccountRepository {

    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    
    // 스냅샷 생성 임계값 (이벤트 개수)
    private static final int SNAPSHOT_THRESHOLD = 50;

    public EventSourcedAccountRepository(EventStore eventStore, SnapshotStore snapshotStore) {
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
    }

    /**
     * 계좌 ID로 계좌를 조회합니다
     * 스냅샷이 있으면 스냅샷부터 복원하고, 없으면 처음부터 이벤트 재생
     */
    public Optional<Account> findById(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return Optional.empty();
        }

        // 1. 스냅샷 확인
        Optional<Snapshot> snapshot = snapshotStore.getSnapshot(accountId);
        
        Account account;
        int fromVersion;

        if (snapshot.isPresent()) {
            // 스냅샷에서 복원
            account = new Account(accountId);
            account.loadFromSnapshot(snapshot.get().data(), snapshot.get().version());
            fromVersion = snapshot.get().version();
        } else {
            // 처음부터 생성
            account = new Account(accountId);
            fromVersion = 0;
        }

        // 2. 스냅샷 이후의 이벤트 재생
        List<DomainEvent> events = eventStore.getEventsForAggregate(accountId, fromVersion);
        
        if (events.isEmpty() && snapshot.isEmpty()) {
            return Optional.empty(); // 계좌가 존재하지 않음
        }

        account.loadFromHistory(events);
        return Optional.of(account);
    }

    /**
     * 사용자 ID로 계좌들을 조회합니다
     */
    public List<Account> findByUserId(UserId userId) {
        // 실제 구현에서는 사용자별 인덱스가 필요
        // 여기서는 간단한 구현만 제공
        return eventStore.getAllEvents().stream()
                .filter(event -> event instanceof Account.AccountOpenedEvent)
                .map(event -> (Account.AccountOpenedEvent) event)
                .filter(event -> event.userId().equals(userId))
                .map(event -> findById(event.accountId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * 계좌를 저장합니다 (이벤트 저장)
     */
    public void save(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("계좌는 null일 수 없습니다");
        }

        if (!account.hasUncommittedEvents()) {
            return; // 저장할 이벤트가 없음
        }

        try {
            // 1. 이벤트 저장
            List<DomainEvent> uncommittedEvents = account.getUncommittedEvents();
            eventStore.saveEvents(
                account.getId(),
                uncommittedEvents,
                account.getExpectedVersion()
            );

            // 2. 이벤트 커밋 표시
            account.markEventsAsCommitted();

            // 3. 스냅샷 생성 검토
            considerCreatingSnapshot(account);

        } catch (ConcurrencyException e) {
            throw new IllegalStateException("동시성 충돌이 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 계좌를 제거합니다 (실제로는 삭제 이벤트 발행)
     */
    public void remove(Account account) {
        if (account == null) {
            return;
        }

        // 계좌 폐쇄 처리
        if (account.getStatus() == Account.AccountStatus.ACTIVE) {
            account.close("계좌 삭제 요청");
            save(account);
        }
    }

    /**
     * 모든 활성 계좌를 조회합니다
     */
    public List<Account> findAllActive() {
        return eventStore.getAllEvents().stream()
                .filter(event -> event instanceof Account.AccountOpenedEvent)
                .map(event -> (Account.AccountOpenedEvent) event)
                .map(event -> findById(event.accountId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(account -> account.getStatus() == Account.AccountStatus.ACTIVE)
                .toList();
    }

    /**
     * 계좌의 이벤트 히스토리를 조회합니다
     */
    public List<DomainEvent> getAccountHistory(String accountId) {
        return eventStore.getEventsForAggregate(accountId);
    }

    /**
     * 특정 시점의 계좌 상태를 조회합니다
     */
    public Optional<Account> findByIdAtTime(String accountId, java.time.LocalDateTime pointInTime) {
        List<DomainEvent> allEvents = eventStore.getEventsForAggregate(accountId);
        
        // 특정 시점 이전의 이벤트만 필터링
        List<DomainEvent> eventsUntilTime = allEvents.stream()
                .filter(event -> !event.getOccurredOn().isAfter(pointInTime))
                .toList();

        if (eventsUntilTime.isEmpty()) {
            return Optional.empty();
        }

        Account account = new Account(accountId);
        account.loadFromHistory(eventsUntilTime);
        return Optional.of(account);
    }

    /**
     * 스냅샷 생성을 고려합니다
     */
    private void considerCreatingSnapshot(Account account) {
        int currentVersion = account.getVersion();
        
        // 스냅샷이 없거나 임계값을 넘으면 새 스냅샷 생성
        Optional<Snapshot> existingSnapshot = snapshotStore.getSnapshot(account.getId());
        
        boolean shouldCreateSnapshot = existingSnapshot.isEmpty() ||
                (currentVersion - existingSnapshot.get().version()) >= SNAPSHOT_THRESHOLD;

        if (shouldCreateSnapshot) {
            Object snapshotData = account.createSnapshot();
            snapshotStore.saveSnapshot(account.getId(), snapshotData, currentVersion);
        }
    }

    /**
     * 개발/테스트용: 특정 계좌의 모든 이벤트와 스냅샷을 제거
     */
    public void purgeAccount(String accountId) {
        if (eventStore instanceof InMemoryEventStore inMemoryStore) {
            inMemoryStore.clearAggregate(accountId);
        }
        // 스냅샷도 제거 (실제 구현 필요)
    }

    /**
     * 개발/테스트용: 계좌별 이벤트 수 조회
     */
    public int getEventCount(String accountId) {
        return eventStore.getEventsForAggregate(accountId).size();
    }
}
