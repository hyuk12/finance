package com.finance.demo.eventsourcing;

import com.finance.demo.shared.eventsourcing.Snapshot;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Day 2: 인메모리 스냅샷 스토어 구현체
 * 
 * 실제 운영 환경에서는 데이터베이스나 전용 스토리지를 사용하지만,
 * 학습 목적으로 간단한 인메모리 구현체를 제공합니다.
 * 
 * 특징:
 * - 스레드 세이프한 동시성 처리
 * - 애그리게이트별 다중 스냅샷 관리
 * - 자동 정리 기능
 * - 버전 기반 조회
 */
@Component
public class InMemorySnapshotStore implements SnapshotStore {

    // 애그리게이트별 스냅샷 목록 (버전 순 정렬)
    private final Map<String, List<Snapshot>> snapshotStore = new ConcurrentHashMap<>();
    
    // 기본 유지할 스냅샷 개수
    private static final int DEFAULT_KEEP_COUNT = 3;

    @Override
    public void saveSnapshot(String aggregateId, Object snapshot, int version) {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            throw new IllegalArgumentException("애그리게이트 ID는 필수입니다");
        }
        if (snapshot == null) {
            throw new IllegalArgumentException("스냅샷 데이터는 필수입니다");
        }
        if (version < 0) {
            throw new IllegalArgumentException("버전은 0 이상이어야 합니다");
        }

        Snapshot newSnapshot = new Snapshot(
            aggregateId,
            snapshot,
            version,
            LocalDateTime.now()
        );

        snapshotStore.compute(aggregateId, (key, existingSnapshots) -> {
            if (existingSnapshots == null) {
                existingSnapshots = Collections.synchronizedList(new ArrayList<>());
            }

            // 같은 버전의 스냅샷이 있으면 교체, 없으면 추가
            existingSnapshots.removeIf(s -> s.version() == version);
            existingSnapshots.add(newSnapshot);
            
            // 버전 순으로 정렬
            existingSnapshots.sort(Comparator.comparing(Snapshot::version));
            
            return existingSnapshots;
        });

        // 자동으로 오래된 스냅샷 정리
        cleanupOldSnapshots(aggregateId, DEFAULT_KEEP_COUNT);
    }

    @Override
    public Optional<Snapshot> getSnapshot(String aggregateId) {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            return Optional.empty();
        }

        List<Snapshot> snapshots = snapshotStore.get(aggregateId);
        if (snapshots == null || snapshots.isEmpty()) {
            return Optional.empty();
        }

        // 가장 최신 스냅샷 반환
        return snapshots.stream()
                .max(Comparator.comparing(Snapshot::version));
    }

    @Override
    public Optional<Snapshot> getSnapshotAtOrBefore(String aggregateId, int version) {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            return Optional.empty();
        }

        List<Snapshot> snapshots = snapshotStore.get(aggregateId);
        if (snapshots == null || snapshots.isEmpty()) {
            return Optional.empty();
        }

        // 지정된 버전 이하의 가장 최신 스냅샷 반환
        return snapshots.stream()
                .filter(snapshot -> snapshot.version() <= version)
                .max(Comparator.comparing(Snapshot::version));
    }

    @Override
    public void cleanupOldSnapshots(String aggregateId, int keepCount) {
        if (aggregateId == null || aggregateId.trim().isEmpty() || keepCount <= 0) {
            return;
        }

        snapshotStore.computeIfPresent(aggregateId, (key, snapshots) -> {
            if (snapshots.size() <= keepCount) {
                return snapshots;
            }

            // 버전 순으로 정렬 (최신 순)
            snapshots.sort(Comparator.comparing(Snapshot::version).reversed());
            
            // 최신 keepCount개만 유지
            List<Snapshot> keptSnapshots = Collections.synchronizedList(
                new ArrayList<>(snapshots.subList(0, keepCount))
            );
            
            return keptSnapshots;
        });
    }

    @Override
    public void clear() {
        snapshotStore.clear();
    }

    /**
     * 특정 애그리게이트의 스냅샷만 제거합니다
     */
    public void clearAggregate(String aggregateId) {
        if (aggregateId != null && !aggregateId.trim().isEmpty()) {
            snapshotStore.remove(aggregateId);
        }
    }

    /**
     * 모든 애그리게이트의 스냅샷 개수를 조회합니다
     */
    public Map<String, Integer> getSnapshotCounts() {
        Map<String, Integer> counts = new HashMap<>();
        snapshotStore.forEach((aggregateId, snapshots) -> 
            counts.put(aggregateId, snapshots.size())
        );
        return counts;
    }

    /**
     * 특정 애그리게이트의 모든 스냅샷을 조회합니다 (디버깅용)
     */
    public List<Snapshot> getAllSnapshots(String aggregateId) {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<Snapshot> snapshots = snapshotStore.get(aggregateId);
        if (snapshots == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(snapshots);
    }

    /**
     * 전체 스냅샷 스토어 상태를 출력합니다 (디버깅용)
     */
    public void printSnapshotStore() {
        System.out.println("=== Snapshot Store Status ===");
        System.out.println("Total aggregates: " + snapshotStore.size());
        
        snapshotStore.forEach((aggregateId, snapshots) -> {
            System.out.println(String.format("Aggregate %s: %d snapshots", 
                aggregateId, snapshots.size()));
            
            snapshots.forEach(snapshot -> 
                System.out.println(String.format("  Version %d at %s", 
                    snapshot.version(), snapshot.createdAt()))
            );
        });
        
        System.out.println("============================");
    }

    /**
     * 스냅샷 스토어의 총 메모리 사용량을 추정합니다 (대략적)
     */
    public long estimateMemoryUsage() {
        return snapshotStore.values().stream()
                .mapToLong(List::size)
                .sum() * 1024; // 대략적인 추정값
    }
}
