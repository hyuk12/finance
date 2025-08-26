package com.finance.demo.shared.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * 사용자 식별자 값 객체
 * Day 2: 도메인 중심 리포지토리에서 사용할 타입 안전한 식별자
 */
@Embeddable
public class UserId {
    
    private Long value;
    
    protected UserId() {} // JPA용 기본 생성자
    
    public UserId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("사용자 ID는 양수여야 합니다");
        }
        this.value = value;
    }
    
    public static UserId of(Long value) {
        return new UserId(value);
    }
    
    public Long value() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "UserId{" + value + '}';
    }
}
