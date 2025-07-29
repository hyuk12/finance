package com.finance.demo.eventsourcing;

import com.finance.demo.shared.domain.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Day 2: 이벤트 소싱 애그리게이트 베이스 클래스
 * 
 * 이벤트 소싱 패턴의 핵심 구현:
 * 1. 상태 변경을 이벤트로 기록
 * 2. 이벤트 히스토리로부터 상태 복원
 * 3. 낙관적 동시성 제어를 위한 버전 관리
 * 
 * 사용법:
 * - 상태 변경 시 applyChange() 호출
 * - 이벤트 핸들링은 applyEvent() 구현
 * - 이벤트 히스토리 로딩은 loadFromHistory() 사용
 */
public abstract class EventSourcedAggregate {

    private String id;
    private int version = 0;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    /**
     * 새로운 애그리게이트 생성
     */
    protected EventSourcedAggregate(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("애그리게이트 ID는 필수입니다");
        }
        this.id = id;
    }

    /**
     * 이벤트 히스토리로부터 애그리게이트 상태를 복원합니다
     * 
     * @param history 순서대로 정렬된 이벤트 목록
     */
    public void loadFromHistory(List<DomainEvent> history) {
        if (history == null) {
            return;
        }

        for (DomainEvent event : history) {
            applyEvent(event);
            this.version++;
        }
    }

    /**
     * 새로운 이벤트를 적용하고 커밋되지 않은 이벤트 목록에 추가합니다
     * 
     * @param event 적용할 도메인 이벤트
     */
    protected void applyChange(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("이벤트는 null일 수 없습니다");
        }

        applyEvent(event);
        uncommittedEvents.add(event);
    }

    /**
     * 이벤트를 애그리게이트 상태에 반영하는 추상 메서드
     * 각 구체적인 애그리게이트에서 구현해야 합니다
     * 
     * @param event 상태에 반영할 이벤트
     */
    protected abstract void applyEvent(DomainEvent event);

    /**
     * 커밋되지 않은 이벤트 목록을 반환합니다
     * 이벤트 스토어에 저장할 때 사용됩니다
     */
    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }

    /**
     * 이벤트가 성공적으로 저장되었음을 표시합니다
     * 커밋되지 않은 이벤트 목록을 정리하고 버전을 업데이트합니다
     */
    public void markEventsAsCommitted() {
        version += uncommittedEvents.size();
        uncommittedEvents.clear();
    }

    /**
     * 커밋되지 않은 이벤트가 있는지 확인합니다
     */
    public boolean hasUncommittedEvents() {
        return !uncommittedEvents.isEmpty();
    }

    /**
     * 커밋되지 않은 이벤트의 개수를 반환합니다
     */
    public int getUncommittedEventCount() {
        return uncommittedEvents.size();
    }

    /**
     * 애그리게이트의 현재 상태를 스냅샷으로 생성합니다
     * 성능 최적화를 위해 사용할 수 있습니다
     */
    public abstract Object createSnapshot();

    /**
     * 스냅샷으로부터 애그리게이트 상태를 복원합니다
     */
    public abstract void loadFromSnapshot(Object snapshot, int snapshotVersion);

    // Getters
    public String getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    /**
     * 다음에 저장될 때의 예상 버전을 반환합니다
     * 동시성 제어에 사용됩니다
     */
    public int getExpectedVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EventSourcedAggregate that = (EventSourcedAggregate) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s{id='%s', version=%d, uncommittedEvents=%d}",
                getClass().getSimpleName(), id, version, uncommittedEvents.size());
    }
}
