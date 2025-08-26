package com.finance.demo.eventsourcing;

import com.finance.demo.shared.domain.DomainEvent;
import com.finance.demo.shared.domain.Money;
import com.finance.demo.shared.domain.UserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Day 2: 이벤트 소싱으로 구현된 계좌 애그리게이트
 * 
 * 전통적인 CRUD 방식이 아닌 이벤트 소싱 방식으로 구현:
 * - 상태가 아닌 상태 변경 이벤트를 저장
 * - 현재 상태는 이벤트 재생으로 복원
 * - 완전한 감사 추적 가능
 * - 시점별 상태 복원 가능
 */
public class Account extends EventSourcedAggregate {

    private UserId userId;
    private Money balance;
    private AccountStatus status;
    private LocalDateTime createdAt;

    /**
     * 새 계좌 생성 (Event Sourcing)
     */
    public Account(UserId userId, Money initialBalance) {
        super(UUID.randomUUID().toString());

        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }
        if (initialBalance == null || initialBalance.isNegative()) {
            throw new IllegalArgumentException("초기 잔액은 0 이상이어야 합니다");
        }

        // 계좌 개설 이벤트 발행
        applyChange(new AccountOpenedEvent(
            getId(),
            userId,
            initialBalance,
            LocalDateTime.now()
        ));
    }

    /**
     * 이벤트 히스토리로부터 복원할 때 사용하는 생성자
     */
    public Account(String id) {
        super(id);
    }

    /**
     * 입금 처리
     */
    public void deposit(Money amount, String description) {
        validateActiveAccount();
        
        if (amount == null || amount.isNegativeOrZero()) {
            throw new IllegalArgumentException("입금액은 0보다 커야 합니다");
        }

        applyChange(new MoneyDepositedEvent(
            getId(),
            userId,
            amount,
            description != null ? description : "입금",
            LocalDateTime.now()
        ));
    }

    /**
     * 출금 처리
     */
    public void withdraw(Money amount, String description) {
        validateActiveAccount();
        
        if (amount == null || amount.isNegativeOrZero()) {
            throw new IllegalArgumentException("출금액은 0보다 커야 합니다");
        }

        if (balance.isLessThan(amount)) {
            throw new InsufficientBalanceException(
                String.format("잔액이 부족합니다. 현재 잔액: %s, 출금 요청액: %s", 
                    balance.toString(), amount.toString())
            );
        }

        applyChange(new MoneyWithdrawnEvent(
            getId(),
            userId,
            amount,
            description != null ? description : "출금",
            LocalDateTime.now()
        ));
    }

    /**
     * 계좌 폐쇄
     */
    public void close(String reason) {
        validateActiveAccount();
        
        if (!balance.isZero()) {
            throw new IllegalStateException("잔액이 있는 계좌는 폐쇄할 수 없습니다");
        }

        applyChange(new AccountClosedEvent(
            getId(),
            userId,
            reason != null ? reason : "사용자 요청",
            LocalDateTime.now()
        ));
    }

    @Override
    protected void applyEvent(DomainEvent event) {
        switch (event) {
            case AccountOpenedEvent e -> {
                this.userId = e.userId();
                this.balance = e.initialBalance();
                this.status = AccountStatus.ACTIVE;
                this.createdAt = e.occurredOn();
            }
            case MoneyDepositedEvent e -> {
                this.balance = this.balance.add(e.amount());
            }
            case MoneyWithdrawnEvent e -> {
                this.balance = this.balance.subtract(e.amount());
            }
            case AccountClosedEvent e -> {
                this.status = AccountStatus.CLOSED;
            }
            default -> throw new IllegalArgumentException("알 수 없는 이벤트: " + event.getClass().getSimpleName());
        }
    }

    @Override
    public Object createSnapshot() {
        return new AccountSnapshot(
            getId(),
            userId,
            balance,
            status,
            createdAt,
            getVersion()
        );
    }

    @Override
    public void loadFromSnapshot(Object snapshot, int snapshotVersion) {
        if (!(snapshot instanceof AccountSnapshot accountSnapshot)) {
            throw new IllegalArgumentException("올바른 계좌 스냅샷이 아닙니다");
        }

        this.userId = accountSnapshot.userId();
        this.balance = accountSnapshot.balance();
        this.status = accountSnapshot.status();
        this.createdAt = accountSnapshot.createdAt();
        
        // 스냅샷 버전으로 애그리게이트 버전 설정
        // 실제로는 reflection을 사용하거나 protected setter 필요
    }

    private void validateActiveAccount() {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("활성화된 계좌가 아닙니다. 현재 상태: " + status);
        }
    }

    // 조회 메서드들 (이벤트 소싱에서는 부작용 없는 순수 함수)
    public boolean canWithdraw(Money amount) {
        return status == AccountStatus.ACTIVE && 
               balance.isGreaterThanOrEqual(amount);
    }

    public Money getAvailableBalance() {
        return status == AccountStatus.ACTIVE ? balance : Money.ZERO;
    }

    public boolean isOverdrawn() {
        return balance.isNegative();
    }

    // Getters
    public UserId getUserId() { return userId; }
    public Money getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * 계좌 상태 열거형
     */
    public enum AccountStatus {
        ACTIVE("활성"),
        CLOSED("폐쇄"),
        SUSPENDED("정지");

        private final String description;

        AccountStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 잔액 부족 예외
     */
    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }

    /**
     * 계좌 스냅샷 (성능 최적화용)
     */
    public record AccountSnapshot(
        String accountId,
        UserId userId,
        Money balance,
        AccountStatus status,
        LocalDateTime createdAt,
        int version
    ) {}

    // ========== 이벤트 정의 ==========

    public record AccountOpenedEvent(
        String eventId,
        LocalDateTime occurredOn,
        String accountId,
        UserId userId,
        Money initialBalance,
        LocalDateTime openedAt
    ) implements DomainEvent {
        
        public AccountOpenedEvent(String accountId, UserId userId, Money initialBalance, LocalDateTime openedAt) {
            this(UUID.randomUUID().toString(), LocalDateTime.now(), accountId, userId, initialBalance, openedAt);
        }

        @Override
        public String getEventId() { return eventId; }

        @Override
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public record MoneyDepositedEvent(
        String eventId,
        LocalDateTime occurredOn,
        String accountId,
        UserId userId,
        Money amount,
        String description,
        LocalDateTime depositedAt
    ) implements DomainEvent {
        
        public MoneyDepositedEvent(String accountId, UserId userId, Money amount, String description, LocalDateTime depositedAt) {
            this(UUID.randomUUID().toString(), LocalDateTime.now(), accountId, userId, amount, description, depositedAt);
        }

        @Override
        public String getEventId() { return eventId; }

        @Override
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public record MoneyWithdrawnEvent(
        String eventId,
        LocalDateTime occurredOn,
        String accountId,
        UserId userId,
        Money amount,
        String description,
        LocalDateTime withdrawnAt
    ) implements DomainEvent {
        
        public MoneyWithdrawnEvent(String accountId, UserId userId, Money amount, String description, LocalDateTime withdrawnAt) {
            this(UUID.randomUUID().toString(), LocalDateTime.now(), accountId, userId, amount, description, withdrawnAt);
        }

        @Override
        public String getEventId() { return eventId; }

        @Override
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }

    public record AccountClosedEvent(
        String eventId,
        LocalDateTime occurredOn,
        String accountId,
        UserId userId,
        String reason,
        LocalDateTime closedAt
    ) implements DomainEvent {
        
        public AccountClosedEvent(String accountId, UserId userId, String reason, LocalDateTime closedAt) {
            this(UUID.randomUUID().toString(), LocalDateTime.now(), accountId, userId, reason, closedAt);
        }

        @Override
        public String getEventId() { return eventId; }

        @Override
        public LocalDateTime getOccurredOn() { return occurredOn; }
    }
}
