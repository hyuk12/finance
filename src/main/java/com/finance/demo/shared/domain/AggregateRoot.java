package com.finance.demo.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Day 2: 도메인 이벤트를 관리하는 애그리게이트 루트 베이스 클래스
 * 
 * 도메인 이벤트 발행 패턴:
 * 1. 애그리게이트에서 이벤트 수집
 * 2. 트랜잭션 커밋 직전에 일괄 발행
 * 3. 발행 후 이벤트 목록 정리
 */
public abstract class AggregateRoot {
    
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * 도메인 이벤트를 발생시킵니다
     * 즉시 발행하지 않고 수집만 합니다
     */
    protected void raise(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("도메인 이벤트는 null일 수 없습니다");
        }
        
        domainEvents.add(event);
    }
    
    /**
     * 발생한 모든 도메인 이벤트를 반환합니다
     * 외부에서 수정할 수 없도록 불변 리스트로 반환
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * 도메인 이벤트 목록을 정리합니다
     * 보통 이벤트 발행 후에 호출됩니다
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    /**
     * 발행되지 않은 이벤트가 있는지 확인합니다
     */
    public boolean hasUnpublishedEvents() {
        return !domainEvents.isEmpty();
    }
    
    /**
     * 발행되지 않은 이벤트의 개수를 반환합니다
     */
    public int getUnpublishedEventCount() {
        return domainEvents.size();
    }
}
