package com.finance.demo.transaction.specification;

import com.finance.demo.shared.specification.Specification;
import com.finance.demo.transaction.domain.Category;
import com.finance.demo.transaction.domain.Transaction;

/**
 * Day 2: 특정 카테고리의 거래를 식별하는 명세
 * 
 * 유비쿼터스 언어: "식비 거래", "교통비 거래" 등
 * 비즈니스 규칙: 거래가 특정 카테고리에 속하는지 확인
 */
public class CategoryTransactionSpec implements Specification<Transaction> {
    
    private final Category category;
    
    public CategoryTransactionSpec(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다");
        }
        this.category = category;
    }
    
    @Override
    public boolean isSatisfiedBy(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        
        return category.equals(transaction.getCategory());
    }
    
    @Override
    public String toString() {
        return String.format("%s거래", category.getDescription());
    }
    
    /**
     * 팩토리 메서드: 주요 카테고리별 명세 생성
     */
    public static CategoryTransactionSpec food() {
        return new CategoryTransactionSpec(Category.FOOD);
    }
    
    public static CategoryTransactionSpec transportation() {
        return new CategoryTransactionSpec(Category.TRANSPORTATION);
    }
    
    public static CategoryTransactionSpec shopping() {
        return new CategoryTransactionSpec(Category.SHOPPING);
    }
    
    public static CategoryTransactionSpec entertainment() {
        return new CategoryTransactionSpec(Category.ENTERTAINMENT);
    }
    
    public static CategoryTransactionSpec utilities() {
        return new CategoryTransactionSpec(Category.UTILITIES);
    }
}
