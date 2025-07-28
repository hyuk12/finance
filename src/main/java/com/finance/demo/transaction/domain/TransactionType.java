package com.finance.demo.transaction.domain;

/**
 * 거래 유형을 나타내는 값 객체 (Value Object)
 * 
 * 유비쿼터스 언어:
 * - INCOME: 입금/수입
 * - EXPENSE: 출금/지출
 */
public enum TransactionType {
    INCOME("수입"),
    EXPENSE("지출");
    
    private final String description;
    
    TransactionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
