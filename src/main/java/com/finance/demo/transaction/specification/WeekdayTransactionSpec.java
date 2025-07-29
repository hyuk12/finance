package com.finance.demo.transaction.specification;

import com.finance.demo.shared.specification.Specification;
import com.finance.demo.transaction.domain.Transaction;

import java.time.DayOfWeek;
import java.util.Set;

/**
 * Day 2: 특정 요일의 거래를 식별하는 명세
 * 
 * 유비쿼터스 언어: "평일 거래", "주말 거래"
 * 비즈니스 규칙: 거래가 지정된 요일에 발생했는지 확인
 */
public class WeekdayTransactionSpec implements Specification<Transaction> {
    
    private final Set<DayOfWeek> targetDays;
    
    public WeekdayTransactionSpec(Set<DayOfWeek> targetDays) {
        if (targetDays == null || targetDays.isEmpty()) {
            throw new IllegalArgumentException("대상 요일은 최소 하나 이상 필요합니다");
        }
        this.targetDays = Set.copyOf(targetDays);
    }
    
    public WeekdayTransactionSpec(DayOfWeek... days) {
        this(Set.of(days));
    }
    
    @Override
    public boolean isSatisfiedBy(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        
        DayOfWeek transactionDay = transaction.getTransactionDate().getDayOfWeek();
        return targetDays.contains(transactionDay);
    }
    
    @Override
    public String toString() {
        return String.format("요일거래(%s)", targetDays);
    }
    
    /**
     * 팩토리 메서드: 자주 사용되는 요일 패턴
     */
    public static WeekdayTransactionSpec weekdays() {
        return new WeekdayTransactionSpec(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        );
    }
    
    public static WeekdayTransactionSpec weekends() {
        return new WeekdayTransactionSpec(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
    
    public static WeekdayTransactionSpec monday() {
        return new WeekdayTransactionSpec(DayOfWeek.MONDAY);
    }
    
    public static WeekdayTransactionSpec friday() {
        return new WeekdayTransactionSpec(DayOfWeek.FRIDAY);
    }
}
