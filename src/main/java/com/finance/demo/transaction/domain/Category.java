package com.finance.demo.transaction.domain;

/**
 * 카테고리를 나타내는 값 객체 (Value Object)
 * 
 * 유비쿼터스 언어 정의:
 * - Category: 거래를 분류하는 기준 (식비, 교통비, 쇼핑 등)
 * - 규칙: 카테고리는 계층 구조를 가질 수 있음
 */
public enum Category {
    // 지출 카테고리
    FOOD("식비"),
    TRANSPORTATION("교통비"),
    SHOPPING("쇼핑"),
    UTILITIES("공과금"),
    ENTERTAINMENT("유흥/오락"),
    HEALTHCARE("의료비"),
    EDUCATION("교육비"),
    HOUSING("주거비"),
    
    // 수입 카테고리
    SALARY("급여"),
    BONUS("보너스"),
    INVESTMENT("투자수익"),
    OTHER_INCOME("기타수입"),
    
    // 기타
    OTHER("기타");
    
    private final String description;
    
    Category(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 지출 카테고리인지 확인
     */
    public boolean isExpenseCategory() {
        return switch (this) {
            case FOOD, TRANSPORTATION, SHOPPING, UTILITIES, 
                 ENTERTAINMENT, HEALTHCARE, EDUCATION, HOUSING, OTHER -> true;
            default -> false;
        };
    }
    
    /**
     * 수입 카테고리인지 확인
     */
    public boolean isIncomeCategory() {
        return switch (this) {
            case SALARY, BONUS, INVESTMENT, OTHER_INCOME -> true;
            default -> false;
        };
    }
}
