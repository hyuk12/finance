package com.finance.demo;

import com.finance.demo.eventsourcing.*;
import com.finance.demo.shared.domain.Money;
import com.finance.demo.shared.domain.UserId;
import com.finance.demo.shared.specification.Specification;
import com.finance.demo.transaction.application.TransactionRepository;
import com.finance.demo.transaction.application.TransactionSpecificationDemo;
import com.finance.demo.transaction.domain.Category;
import com.finance.demo.transaction.domain.Transaction;
import com.finance.demo.transaction.domain.TransactionType;
import com.finance.demo.transaction.specification.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Day 2: 통합 테스트
 * 
 * 구현된 모든 Day 2 기능들을 테스트:
 * - 리포지토리 패턴 심화
 * - Specification 패턴
 * - 도메인 이벤트 발행
 * - 이벤트 소싱 기초
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class Day2IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(Day2IntegrationTest.class);

    @Autowired
    private EventStore eventStore;

    @Autowired
    private SnapshotStore snapshotStore;

    @Autowired
    private EventSourcedAccountRepository accountRepository;

    @Autowired
    private TransactionSpecificationDemo specificationDemo;

    @Test
    public void testEventSourcingBasics() {
        log.info("=== 이벤트 소싱 기본 테스트 ===");

        // 새 계좌 생성
        UserId userId = new UserId(1L);
        Money initialBalance = Money.of(new BigDecimal("1000000"));
        Account account = new Account(userId, initialBalance);

        // 계좌 저장
        accountRepository.save(account);

        // 계좌 조회 및 검증
        Account retrievedAccount = accountRepository.findById(account.getId()).orElse(null);
        assertThat(retrievedAccount).isNotNull();
        assertThat(retrievedAccount.getBalance()).isEqualTo(initialBalance);
        assertThat(retrievedAccount.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);

        log.info("계좌 생성 및 조회 성공: ID {}, 잔액 {}", 
                retrievedAccount.getId(), retrievedAccount.getBalance());
    }

    @Test
    public void testEventSourcingTransactions() {
        log.info("=== 이벤트 소싱 거래 테스트 ===");

        // 계좌 생성
        UserId userId = new UserId(2L);
        Account account = new Account(userId, Money.of(new BigDecimal("500000")));
        accountRepository.save(account);

        // 입금
        Money depositAmount = Money.of(new BigDecimal("200000"));
        account.deposit(depositAmount, "급여 입금");
        accountRepository.save(account);

        // 출금
        Money withdrawAmount = Money.of(new BigDecimal("150000"));
        account.withdraw(withdrawAmount, "생활비 출금");
        accountRepository.save(account);

        // 최종 잔액 확인
        Account finalAccount = accountRepository.findById(account.getId()).orElse(null);
        assertThat(finalAccount).isNotNull();
        
        Money expectedBalance = Money.of(new BigDecimal("500000"))
                .add(depositAmount)
                .subtract(withdrawAmount);
        
        assertThat(finalAccount.getBalance()).isEqualTo(expectedBalance);

        log.info("거래 후 최종 잔액: {}", finalAccount.getBalance());

        // 이벤트 히스토리 확인
        List<com.finance.demo.shared.domain.DomainEvent> history = 
                accountRepository.getAccountHistory(account.getId());
        
        assertThat(history).hasSize(3); // 계좌개설, 입금, 출금
        log.info("이벤트 히스토리: {}개 이벤트", history.size());
    }

    @Test
    public void testSpecificationPattern() {
        log.info("=== Specification 패턴 테스트 ===");

        // 기본 Specification 테스트
        testBasicSpecifications();
        
        // 복합 Specification 테스트
        testCompositeSpecifications();
        
        // 정기 지출 패턴 Specification 테스트
        testRegularSpendingPattern();
    }

    private void testBasicSpecifications() {
        log.info("--- 기본 Specification 테스트 ---");

        // 테스트 거래 생성
        Transaction transaction1 = createTestTransaction(
                1L, new BigDecimal("50000"), Category.FOOD, LocalDateTime.now().minusDays(1));
        Transaction transaction2 = createTestTransaction(
                1L, new BigDecimal("150000"), Category.SHOPPING, LocalDateTime.now().minusDays(2));

        // 고액 거래 Specification
        HighAmountTransactionSpec highAmountSpec = 
                new HighAmountTransactionSpec(Money.of(new BigDecimal("100000")));
        
        assertThat(highAmountSpec.isSatisfiedBy(transaction1)).isFalse();
        assertThat(highAmountSpec.isSatisfiedBy(transaction2)).isTrue();

        // 카테고리 Specification
        CategoryTransactionSpec foodSpec = new CategoryTransactionSpec(Category.FOOD);
        assertThat(foodSpec.isSatisfiedBy(transaction1)).isTrue();
        assertThat(foodSpec.isSatisfiedBy(transaction2)).isFalse();

        log.info("기본 Specification 테스트 통과");
    }

    private void testCompositeSpecifications() {
        log.info("--- 복합 Specification 테스트 ---");

        Transaction transaction = createTestTransaction(
                1L, new BigDecimal("120000"), Category.FOOD, LocalDateTime.now().minusDays(1));

        // AND 조합
        Specification<Transaction> andSpec = 
                new HighAmountTransactionSpec(Money.of(new BigDecimal("100000")))
                    .and(new CategoryTransactionSpec(Category.FOOD));
        
        assertThat(andSpec.isSatisfiedBy(transaction)).isTrue();

        // OR 조합
        Specification<Transaction> orSpec = 
                new CategoryTransactionSpec(Category.SHOPPING)
                    .or(new CategoryTransactionSpec(Category.FOOD));
        
        assertThat(orSpec.isSatisfiedBy(transaction)).isTrue();

        // NOT 조합
        Specification<Transaction> notSpec = 
                new CategoryTransactionSpec(Category.SHOPPING).not();
        
        assertThat(notSpec.isSatisfiedBy(transaction)).isTrue();

        log.info("복합 Specification 테스트 통과");
    }

    private void testRegularSpendingPattern() {
        log.info("--- 정기 지출 패턴 Specification 테스트 ---");

        // 정기적인 거래 패턴 생성 (매주 월요일 비슷한 금액)
        LocalDateTime baseDate = LocalDateTime.now().minusWeeks(4);
        List<Transaction> regularTransactions = List.of(
                createTestTransaction(1L, new BigDecimal("45000"), Category.FOOD, baseDate),
                createTestTransaction(1L, new BigDecimal("47000"), Category.FOOD, baseDate.plusWeeks(1)),
                createTestTransaction(1L, new BigDecimal("46000"), Category.FOOD, baseDate.plusWeeks(2)),
                createTestTransaction(1L, new BigDecimal("48000"), Category.FOOD, baseDate.plusWeeks(3))
        );

        RegularSpendingPatternSpec regularSpec = 
                new RegularSpendingPatternSpec(3, Duration.ofDays(2)); // 3회 이상, 2일 허용 오차

        assertThat(regularSpec.isSatisfiedBy(regularTransactions)).isTrue();

        double patternStrength = regularSpec.calculatePatternStrength(regularTransactions);
        assertThat(patternStrength).isGreaterThan(0.5);

        log.info("정기 지출 패턴 강도: {:.2f}", patternStrength);
        log.info("정기 지출 패턴 Specification 테스트 통과");
    }

    @Test
    public void testEventStoreOperations() {
        log.info("=== 이벤트 스토어 운영 테스트 ===");

        String aggregateId = UUID.randomUUID().toString();
        
        // 테스트 이벤트 생성
        Account.AccountOpenedEvent event1 = new Account.AccountOpenedEvent(
                aggregateId, new UserId(1L), Money.of(new BigDecimal("100000")), LocalDateTime.now());
        
        Account.MoneyDepositedEvent event2 = new Account.MoneyDepositedEvent(
                aggregateId, new UserId(1L), Money.of(new BigDecimal("50000")), "테스트 입금", LocalDateTime.now());

        // 이벤트 저장
        eventStore.saveEvents(aggregateId, List.of(event1, event2), 0);

        // 이벤트 조회
        List<com.finance.demo.shared.domain.DomainEvent> retrievedEvents = 
                eventStore.getEventsForAggregate(aggregateId);
        
        assertThat(retrievedEvents).hasSize(2);
        assertThat(eventStore.getCurrentVersion(aggregateId)).isEqualTo(2);

        log.info("이벤트 스토어 운영 테스트 통과: {}개 이벤트 저장/조회", retrievedEvents.size());
    }

    @Test
    public void testSnapshotStore() {
        log.info("=== 스냅샷 스토어 테스트 ===");

        String aggregateId = UUID.randomUUID().toString();
        
        // 스냅샷 데이터 생성
        Account.AccountSnapshot snapshotData = new Account.AccountSnapshot(
                aggregateId,
                new UserId(1L),
                Money.of(new BigDecimal("300000")),
                Account.AccountStatus.ACTIVE,
                LocalDateTime.now(),
                10
        );

        // 스냅샷 저장
        snapshotStore.saveSnapshot(aggregateId, snapshotData, 10);

        // 스냅샷 조회
        var retrievedSnapshot = snapshotStore.getSnapshot(aggregateId);
        
        assertThat(retrievedSnapshot).isPresent();
        assertThat(retrievedSnapshot.get().version()).isEqualTo(10);

        log.info("스냅샷 스토어 테스트 통과: 버전 {}", retrievedSnapshot.get().version());
    }

    @Test
    public void testSpecificationDemo() {
        log.info("=== Specification 데모 실행 ===");
        
        // Specification 데모 실행 (실제 데이터가 있다면 결과 출력)
        specificationDemo.demonstrateSpecificationUsage();
        
        log.info("Specification 데모 완료");
    }

    private Transaction createTestTransaction(Long userId, BigDecimal amount, Category category, LocalDateTime date) {
        return new Transaction(
                userId,
                amount,
                date,
                "테스트 거래",
                amount.compareTo(BigDecimal.ZERO) > 0 ? TransactionType.EXPENSE : TransactionType.INCOME,
                category
        );
    }
}
