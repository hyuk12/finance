package com.finance.demo.shared.config;

import com.finance.demo.account.domain.User;
import com.finance.demo.transaction.application.TransactionService;
import com.finance.demo.transaction.domain.TransactionType;
import com.finance.demo.transaction.domain.Category;
import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 1일차 실습을 위한 데모 데이터 초기화
 * 
 * 바운디드 컨텍스트별로 샘플 데이터를 생성하여
 * 이벤트 스토밍에서 도출된 시나리오를 테스트할 수 있도록 함
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {
    
    private final EntityManager entityManager;
    private final TransactionService transactionService;
    
    public DemoDataInitializer(EntityManager entityManager, 
                             TransactionService transactionService) {
        this.entityManager = entityManager;
        this.transactionService = transactionService;
    }
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeUsers();
        initializeTransactions();
        
        System.out.println("=".repeat(60));
        System.out.println("🎯 DDD Day 1 실습 데모 데이터가 초기화되었습니다!");
        System.out.println("=".repeat(60));
        System.out.println("📊 H2 Console: http://localhost:8080/h2-console");
        System.out.println("🔗 JDBC URL: jdbc:h2:mem:financedb");
        System.out.println("👤 Username: sa");
        System.out.println("🔑 Password: (비어있음)");
        System.out.println("=".repeat(60));
        System.out.println("🧪 API 테스트 예시:");
        System.out.println("GET /api/transactions?userId=1");
        System.out.println("GET /api/patterns?userId=1");
        System.out.println("=".repeat(60));
    }
    
    private void initializeUsers() {
        // 계정 관리 컨텍스트: 사용자 생성
        User user1 = new User("kim@example.com", "김철수");
        User user2 = new User("lee@example.com", "이영희");
        User user3 = new User("park@example.com", "박민수");
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();
        
        System.out.println("✅ 사용자 데이터 초기화 완료 (계정 관리 컨텍스트)");
    }
    
    private void initializeTransactions() {
        // 거래 데이터 컨텍스트: 다양한 거래 패턴 생성
        LocalDateTime baseDate = LocalDateTime.now().minusDays(30);
        
        // 김철수(ID: 1)의 거래 패턴 - 규칙적인 소비 패턴
        createRegularSpendingPattern(1L, baseDate);
        
        // 이영희(ID: 2)의 거래 패턴 - 불규칙한 소비 패턴  
        createIrregularSpendingPattern(2L, baseDate);
        
        // 박민수(ID: 3)의 거래 패턴 - 절약형 소비 패턴
        createSavingSpendingPattern(3L, baseDate);
        
        System.out.println("✅ 거래 데이터 초기화 완료 (거래 데이터 컨텍스트)");
        System.out.println("🔄 패턴 분석이 자동으로 실행됩니다 (패턴 분석 컨텍스트)");
    }
    
    private void createRegularSpendingPattern(Long userId, LocalDateTime baseDate) {
        // 매일 아침 커피 (DAILY 패턴)
        for (int i = 0; i < 25; i++) {
            transactionService.createTransaction(
                userId,
                new BigDecimal("4500"),
                baseDate.plusDays(i).withHour(8).withMinute(30),
                "스타벅스 아메리카노",
                TransactionType.EXPENSE,
                Category.FOOD
            );
        }
        
        // 주말마다 쇼핑 (WEEKLY 패턴)
        for (int i = 0; i < 4; i++) {
            transactionService.createTransaction(
                userId,
                new BigDecimal("85000"),
                baseDate.plusDays(i * 7 + 6).withHour(14).withMinute(0),
                "백화점 쇼핑",
                TransactionType.EXPENSE,
                Category.SHOPPING
            );
        }
        
        // 월급 (MONTHLY 패턴)
        transactionService.createTransaction(
            userId,
            new BigDecimal("3500000"),
            baseDate.plusDays(25).withHour(9).withMinute(0),
            "급여",
            TransactionType.INCOME,
            Category.SALARY
        );
        
        // 교통비 (DAILY 패턴)
        for (int i = 0; i < 20; i++) {
            if (i % 7 < 5) { // 평일만
                transactionService.createTransaction(
                    userId,
                    new BigDecimal("2800"),
                    baseDate.plusDays(i).withHour(18).withMinute(0),
                    "지하철 교통비",
                    TransactionType.EXPENSE,
                    Category.TRANSPORTATION
                );
            }
        }
    }
    
    private void createIrregularSpendingPattern(Long userId, LocalDateTime baseDate) {
        // 불규칙한 식비
        int[] foodAmounts = {12000, 8500, 15000, 22000, 6500, 18000, 9000};
        for (int i = 0; i < 15; i++) {
            transactionService.createTransaction(
                userId,
                new BigDecimal(foodAmounts[i % foodAmounts.length]),
                baseDate.plusDays(i * 2).withHour(12 + (i % 3)).withMinute(0),
                "점심식사",
                TransactionType.EXPENSE,
                Category.FOOD
            );
        }
        
        // 가끔씩하는 큰 쇼핑
        transactionService.createTransaction(
            userId,
            new BigDecimal("450000"),
            baseDate.plusDays(10).withHour(15).withMinute(30),
            "명품가방 구매",
            TransactionType.EXPENSE,
            Category.SHOPPING
        );
        
        transactionService.createTransaction(
            userId,
            new BigDecimal("230000"),
            baseDate.plusDays(22).withHour(16).withMinute(15),
            "온라인 쇼핑",
            TransactionType.EXPENSE,
            Category.SHOPPING
        );
        
        // 월급
        transactionService.createTransaction(
            userId,
            new BigDecimal("2800000"),
            baseDate.plusDays(25).withHour(9).withMinute(0),
            "급여",
            TransactionType.INCOME,
            Category.SALARY
        );
    }
    
    private void createSavingSpendingPattern(Long userId, LocalDateTime baseDate) {
        // 집에서 도시락 (절약 패턴)
        for (int i = 0; i < 20; i++) {
            if (i % 7 < 5) { // 평일만
                transactionService.createTransaction(
                    userId,
                    new BigDecimal("3000"),
                    baseDate.plusDays(i).withHour(12).withMinute(0),
                    "편의점 도시락",
                    TransactionType.EXPENSE,
                    Category.FOOD
                );
            }
        }
        
        // 대중교통 이용
        for (int i = 0; i < 18; i++) {
            if (i % 7 < 5) { // 평일만
                transactionService.createTransaction(
                    userId,
                    new BigDecimal("1400"),
                    baseDate.plusDays(i).withHour(8).withMinute(30),
                    "버스비",
                    TransactionType.EXPENSE,
                    Category.TRANSPORTATION
                );
            }
        }
        
        // 필수 지출만
        transactionService.createTransaction(
            userId,
            new BigDecimal("45000"),
            baseDate.plusDays(15).withHour(10).withMinute(0),
            "생필품 구매",
            TransactionType.EXPENSE,
            Category.SHOPPING
        );
        
        // 월급
        transactionService.createTransaction(
            userId,
            new BigDecimal("3200000"),
            baseDate.plusDays(25).withHour(9).withMinute(0),
            "급여",
            TransactionType.INCOME,
            Category.SALARY
        );
        
        // 투자 (저축 패턴)
        transactionService.createTransaction(
            userId,
            new BigDecimal("500000"),
            baseDate.plusDays(26).withHour(14).withMinute(0),
            "적금 입금",
            TransactionType.EXPENSE,
            Category.OTHER
        );
    }
}
