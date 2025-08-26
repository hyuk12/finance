package com.finance.demo.eventsourcing;

import com.finance.demo.shared.eventsourcing.Snapshot;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Day 2: 스냅샷 스토어 인터페이스
 * 
 * 이벤트 소싱에서 성능 최적화를 위한 스냅샷 관리:
 * - 특정 시점의 애그리게이트 상태를 저장
 * - 이벤트 재생 시 스냅샷부터 시작하여 성능 향상
 * - 이벤트 수가 많을 때 필수적인 최적화
 */
public interface SnapshotStore {
    
    /**
     * 애그리게이트의 스냅샷을 저장합니다
     * 
     * @param aggregateId 애그리게이트 식별자
     * @param snapshot 스냅샷 데이터
     * @param version 스냅샷 생성 시점의 버전
     */
    void saveSnapshot(String aggregateId, Object snapshot, int version);
    
    /**
     * 애그리게이트의 최신 스냅샷을 조회합니다
     */
    Optional<Snapshot> getSnapshot(String aggregateId);
    
    /**
     * 특정 버전 이하의 스냅샷을 조회합니다
     */
    Optional<Snapshot> getSnapshotAtOrBefore(String aggregateId, int version);
    
    /**
     * 오래된 스냅샷을 정리합니다
     * 
     * @param aggregateId 애그리게이트 식별자
     * @param keepCount 유지할 스냅샷 개수
     */
    void cleanupOldSnapshots(String aggregateId, int keepCount);
    
    /**
     * 모든 스냅샷을 제거합니다 (테스트 목적)
     */
    void clear();
}
