package com.finance.demo.transaction.specification;

import com.finance.demo.shared.specification.Specification;
import com.finance.demo.transaction.domain.Transaction;
import com.finance.demo.transaction.domain.TransactionType;

/**
 * Day 2: 지출 거래를 식별하는 명세
 * 
 * 유비쿼터스 언어: "지출 거래"
 * 비즈니스 규칙: 거래 유형이 EXPENSE인 거래만 포함
 */
public class ExpenseTransactionSpec implements Specification<Transaction> {
    
    @Override
    public boolean isSatisfiedBy(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        
        return transaction.isExpense();
    }
    
    @Override
    public String toString() {
        return "지출거래";
    }
    
    /**
     * 싱글톤 인스턴스 (상태가 없으므로 재사용 가능)
     */
    private static final ExpenseTransactionSpec INSTANCE = new ExpenseTransactionSpec();
    
    public static ExpenseTransactionSpec instance() {
        return INSTANCE;
    }
}
