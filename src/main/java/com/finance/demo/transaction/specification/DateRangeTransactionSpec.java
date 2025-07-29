package com.finance.demo.transaction.specification;

import com.finance.demo.shared.specification.Specification;
import com.finance.demo.shared.domain.DateRange;
import com.finance.demo.transaction.domain.Transaction;

/**
 * Day 2: 특정 기간 내의 거래를 식별하는 명세
 * 
 * 유비쿼터스 언어: "최근 7일간 거래", "이번 달 거래" 등
 * 비즈니스 규칙: 거래 발생일이 지정된 기간 내에 있는지 확인
 */
public class DateRangeTransactionSpec implements Specification<Transaction> {
    
    private final DateRange dateRange;
    
    public DateRangeTransactionSpec(DateRange dateRange) {
        if (dateRange == null) {
            throw new IllegalArgumentException("날짜 범위는 필수입니다");
        }
        this.dateRange = dateRange;
    }
    
    @Override
    public boolean isSatisfiedBy(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        
        return dateRange.contains(transaction.getTransactionDate());
    }
    
    @Override
    public String toString() {
        return String.format("기간내거래(%s)", dateRange);
    }
    
    /**
     * 팩토리 메서드: 자주 사용되는 기간별 명세 생성
     */
    public static DateRangeTransactionSpec lastWeek() {
        return new DateRangeTransactionSpec(DateRange.lastDays(7));
    }
    
    public static DateRangeTransactionSpec lastMonth() {
        return new DateRangeTransactionSpec(DateRange.lastDays(30));
    }
    
    public static DateRangeTransactionSpec currentMonth() {
        return new DateRangeTransactionSpec(DateRange.currentMonth());
    }
    
    public static DateRangeTransactionSpec previousMonth() {
        return new DateRangeTransactionSpec(DateRange.lastMonth());
    }
    
    public static DateRangeTransactionSpec lastDays(int days) {
        return new DateRangeTransactionSpec(DateRange.lastDays(days));
    }
}
