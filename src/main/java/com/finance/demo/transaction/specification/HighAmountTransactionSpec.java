package com.finance.demo.transaction.specification;

import com.finance.demo.shared.specification.Specification;
import com.finance.demo.shared.domain.Money;
import com.finance.demo.transaction.domain.Transaction;

/**
 * Day 2: 고액 거래를 식별하는 명세
 * 
 * 유비쿼터스 언어: "고액 거래"
 * 비즈니스 규칙: 특정 임계값 이상의 거래를 고액으로 분류
 */
public class HighAmountTransactionSpec implements Specification<Transaction> {
    
    private final Money threshold;
    
    public HighAmountTransactionSpec(Money threshold) {
        if (threshold == null) {
            throw new IllegalArgumentException("임계값은 필수입니다");
        }
        this.threshold = threshold;
    }
    
    @Override
    public boolean isSatisfiedBy(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        
        Money transactionAmount = Money.of(transaction.getAmount());
        return transactionAmount.isGreaterThanOrEqual(threshold);
    }
    
    @Override
    public String toString() {
        return String.format("고액거래(≥%s)", threshold);
    }
    
    /**
     * 팩토리 메서드: 일반적인 고액 기준으로 명세 생성
     */
    public static HighAmountTransactionSpec over100K() {
        return new HighAmountTransactionSpec(Money.of(100000));
    }
    
    public static HighAmountTransactionSpec over500K() {
        return new HighAmountTransactionSpec(Money.of(500000));
    }
    
    public static HighAmountTransactionSpec over1M() {
        return new HighAmountTransactionSpec(Money.of(1000000));
    }
}
