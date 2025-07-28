package com.finance.demo.shared.domain;

import java.time.LocalDateTime;

/**
 * 도메인 이벤트의 기본 인터페이스
 * 
 * 이벤트 스토밍에서 도출된 "~가 발생했다" 형태의 이벤트들을 위한 기본 구조
 */
public interface DomainEvent {
    /**
     * 이벤트가 발생한 시간
     */
    LocalDateTime getOccurredOn();
    
    /**
     * 이벤트 식별자
     */
    String getEventId();
    
    /**
     * 이벤트 버전 (향후 이벤트 진화를 위해)
     */
    default int getVersion() {
        return 1;
    }
}
