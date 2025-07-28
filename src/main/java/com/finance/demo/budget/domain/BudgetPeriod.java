package com.finance.demo.budget.domain;

import java.time.LocalDate;

/**
 * 예산 기간을 나타내는 값 객체 (Value Object)
 * 
 * 유비쿼터스 언어:
 * - WEEKLY: 주별 예산
 * - MONTHLY: 월별 예산
 */
public enum BudgetPeriod {
    WEEKLY("주별", 7),
    MONTHLY("월별", 30);
    
    private final String description;
    private final int defaultDays;
    
    BudgetPeriod(String description, int defaultDays) {
        this.description = description;
        this.defaultDays = defaultDays;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 시작일로부터 종료일을 계산
     */
    public LocalDate calculateEndDate(LocalDate startDate) {
        return switch (this) {
            case WEEKLY -> startDate.plusDays(6); // 7일간 (시작일 포함)
            case MONTHLY -> startDate.plusMonths(1).minusDays(1); // 한 달간
        };
    }
    
    public int getDefaultDays() {
        return defaultDays;
    }
}
