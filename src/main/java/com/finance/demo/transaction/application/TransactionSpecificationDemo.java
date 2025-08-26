package com.finance.demo.transaction.application;

import com.finance.demo.shared.domain.DateRange;
import com.finance.demo.shared.domain.Money;
import com.finance.demo.shared.domain.UserId;
import com.finance.demo.shared.specification.Specification;
import com.finance.demo.transaction.domain.Category;
import com.finance.demo.transaction.domain.Transaction;
import com.finance.demo.transaction.specification.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Day 2: Specification 패턴 활용 데모
 */
@Component
public class TransactionSpecificationDemo {

    private static final Logger log = LoggerFactory.getLogger(TransactionSpecificationDemo.class);

    private final TransactionRepository transactionRepository;

    public TransactionSpecificationDemo(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void demonstrateSpecificationUsage() {
        log.info("=== Specification 패턴 활용 데모 시작 ===");

        UserId userId = new UserId(1L);
        
        demonstrateBasicSpecifications(userId);
        demonstrateComplexCombinations(userId);

        log.info("=== Specification 패턴 활용 데모 완료 ===");
    }

    private void demonstrateBasicSpecifications(UserId userId) {
        log.info("--- 기본 Specification 데모 ---");

        Specification<Transaction> highAmountSpec = 
            new HighAmountTransactionSpec(Money.of(new BigDecimal("100000")));
        
        // 실제 구현체가 없으므로 로그만 출력
        log.info("고액 거래 Specification 생성 완료");

        Specification<Transaction> foodSpec = new CategoryTransactionSpec(Category.FOOD);
        log.info("식비 거래 Specification 생성 완료");
    }

    private void demonstrateComplexCombinations(UserId userId) {
        log.info("--- 복합 조건 조합 데모 ---");

        Specification<Transaction> recentHighAmountFoodSpec = 
            new DateRangeTransactionSpec(DateRange.lastDays(30))
                .and(new CategoryTransactionSpec(Category.FOOD))
                .and(new HighAmountTransactionSpec(Money.of(new BigDecimal("50000"))));

        log.info("복합 Specification 조합 완료: 최근 30일간 고액 식비 지출");
    }

    private Money calculateTotal(List<Transaction> transactions) {
        return transactions.stream()
                .map(t -> Money.of(t.getAmount()))
                .reduce(Money.ZERO, Money::add);
    }
}
