package com.finance.demo.pattern.domain;

/**
 * 패턴 유형을 나타내는 값 객체 (Value Object)
 * 
 * 유비쿼터스 언어:
 * - DAILY: 일일 패턴 (매일 발생하는 소비)
 * - WEEKLY: 주간 패턴 (주 단위로 반복되는 소비)
 * - MONTHLY: 월간 패턴 (월 단위로 반복되는 소비)
 * - SEASONAL: 계절 패턴 (특정 시기에 반복되는 소비)
 */
public enum PatternType {
    DAILY("일일 패턴", 1),
    WEEKLY("주간 패턴", 7),
    MONTHLY("월간 패턴", 30),
    SEASONAL("계절 패턴", 90);
    
    private final String description;
    private final int cycleDays; // 패턴 주기 (일 단위)
    
    PatternType(String description, int cycleDays) {
        this.description = description;
        this.cycleDays = cycleDays;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getCycleDays() {
        return cycleDays;
    }
    
    /**
     * 주어진 일수가 이 패턴 타입에 해당하는지 확인
     */
    public boolean matchesCycle(int daysBetween) {
        int tolerance = cycleDays / 4; // 25% 허용 오차
        return Math.abs(daysBetween - cycleDays) <= tolerance;
    }
}
