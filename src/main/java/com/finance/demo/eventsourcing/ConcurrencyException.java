package com.finance.demo.eventsourcing;

/**
 * Day 2: 이벤트 소싱에서 동시성 충돌을 나타내는 예외
 * 
 * 낙관적 동시성 제어에서 발생:
 * - 예상한 애그리게이트 버전과 실제 버전이 다를 때
 * - 여러 사용자가 동시에 같은 애그리게이트를 수정할 때
 */
public class ConcurrencyException extends RuntimeException {
    
    private final String aggregateId;
    private final int expectedVersion;
    private final int actualVersion;
    
    public ConcurrencyException(String aggregateId, int expectedVersion, int actualVersion) {
        super(String.format(
            "동시성 충돌이 발생했습니다. 애그리게이트 ID: %s, 예상 버전: %d, 실제 버전: %d",
            aggregateId, expectedVersion, actualVersion
        ));
        this.aggregateId = aggregateId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    public String getAggregateId() {
        return aggregateId;
    }
    
    public int getExpectedVersion() {
        return expectedVersion;
    }
    
    public int getActualVersion() {
        return actualVersion;
    }
}
