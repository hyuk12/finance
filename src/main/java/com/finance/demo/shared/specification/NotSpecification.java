package com.finance.demo.shared.specification;

/**
 * NOT 조건을 나타내는 복합 명세
 */
public class NotSpecification<T> implements Specification<T> {
    
    private final Specification<T> spec;
    
    public NotSpecification(Specification<T> spec) {
        this.spec = spec;
    }
    
    @Override
    public boolean isSatisfiedBy(T candidate) {
        return !spec.isSatisfiedBy(candidate);
    }
    
    @Override
    public String toString() {
        return String.format("(NOT %s)", spec.toString());
    }
}
