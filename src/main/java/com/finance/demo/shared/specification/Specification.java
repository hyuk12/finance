package com.finance.demo.shared.specification;

/**
 * Day 2: Specification 패턴의 기본 인터페이스
 * 
 * 비즈니스 규칙을 재사용 가능한 객체로 캡슐화하여
 * 복잡한 조건을 조합할 수 있게 하는 패턴
 */
public interface Specification<T> {
    
    /**
     * 주어진 객체가 이 명세를 만족하는지 확인합니다
     */
    boolean isSatisfiedBy(T candidate);
    
    /**
     * 이 명세와 다른 명세를 AND 조건으로 결합합니다
     */
    default Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }
    
    /**
     * 이 명세와 다른 명세를 OR 조건으로 결합합니다
     */
    default Specification<T> or(Specification<T> other) {
        return new OrSpecification<>(this, other);
    }
    
    /**
     * 이 명세의 반대 조건을 반환합니다
     */
    default Specification<T> not() {
        return new NotSpecification<>(this);
    }
}
