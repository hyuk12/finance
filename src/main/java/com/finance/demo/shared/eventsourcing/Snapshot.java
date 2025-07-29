package com.finance.demo.shared.eventsourcing;

import java.time.LocalDateTime;

/**
 * Day 2: 스냅샷 데이터를 담는 레코드
 * 
 * 이벤트 소싱에서 성능 최적화를 위한 스냅샷:
 * - 특정 시점의 애그리게이트 상태 저장
 * - 이벤트 재생 시간 단축
 * - 메모리 사용량 최적화
 */
public record Snapshot(
    String aggregateId,
    Object data,
    int version,
    LocalDateTime createdAt
) {
    public Snapshot {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            throw new IllegalArgumentException("애그리게이트 ID는 필수입니다");
        }
        if (data == null) {
            throw new IllegalArgumentException("스냅샷 데이터는 필수입니다");
        }
        if (version < 0) {
            throw new IllegalArgumentException("버전은 0 이상이어야 합니다");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시간은 필수입니다");
        }
    }
    
    /**
     * 생성 시간을 현재 시간으로 하는 스냅샷 생성
     */
    public static Snapshot of(String aggregateId, Object data, int version) {
        return new Snapshot(aggregateId, data, version, LocalDateTime.now());
    }
}
