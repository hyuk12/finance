package com.finance.demo.shared.specification;

/**
 * AND 조건을 나타내는 복합 명세
 */
public class AndSpecification<T> implements Specification<T> {
    
    private final Specification<T> left;
    private final Specification<T> right;
    
    public AndSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) && right.isSatisfiedBy(candidate);
    }
    
    @Override
    public String toString() {
        return String.format("(%s AND %s)", left.toString(), right.toString());
    }
}
