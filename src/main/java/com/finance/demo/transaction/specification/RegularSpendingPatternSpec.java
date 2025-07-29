package com.finance.demo.transaction.specification;

import com.finance.demo.shared.specification.Specification;
import com.finance.demo.transaction.domain.Transaction;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Day 2: 정기적인 지출 패턴을 검증하는 Specification
 * 
 * 비즈니스 규칙:
 * - 최소 발생 횟수 이상의 거래
 * - 거래 간 간격의 일관성
 * - 금액의 유사성 (허용 오차 범위 내)
 * 
 * 사용 예:
 * - 월세, 통신비 등 정기 지출 감지
 * - 구독 서비스 패턴 분석
 * - 자동 예산 할당 후보 식별
 */
public class RegularSpendingPatternSpec implements Specification<List<Transaction>> {
    
    private final int minimumOccurrences;
    private final Duration intervalTolerance;
    private final double amountTolerancePercentage;

    public RegularSpendingPatternSpec(int minimumOccurrences, Duration intervalTolerance) {
        this(minimumOccurrences, intervalTolerance, 0.1); // 기본 10% 허용 오차
    }

    public RegularSpendingPatternSpec(int minimumOccurrences, Duration intervalTolerance, double amountTolerancePercentage) {
        if (minimumOccurrences < 2) {
            throw new IllegalArgumentException("최소 발생 횟수는 2 이상이어야 합니다");
        }
        if (intervalTolerance == null || intervalTolerance.isNegative()) {
            throw new IllegalArgumentException("간격 허용 오차는 양수여야 합니다");
        }
        if (amountTolerancePercentage < 0 || amountTolerancePercentage > 1) {
            throw new IllegalArgumentException("금액 허용 오차는 0~1 사이여야 합니다");
        }

        this.minimumOccurrences = minimumOccurrences;
        this.intervalTolerance = intervalTolerance;
        this.amountTolerancePercentage = amountTolerancePercentage;
    }

    @Override
    public boolean isSatisfiedBy(List<Transaction> transactions) {
        if (transactions == null || transactions.size() < minimumOccurrences) {
            return false;
        }

        // 시간순 정렬 (오름차순)
        List<Transaction> sortedTransactions = transactions.stream()
                .sorted((t1, t2) -> t1.getTransactionDate().compareTo(t2.getTransactionDate()))
                .toList();

        // 1. 간격의 일관성 확인
        if (!hasConsistentIntervals(sortedTransactions)) {
            return false;
        }

        // 2. 금액의 유사성 확인
        if (!hasSimilarAmounts(sortedTransactions)) {
            return false;
        }

        return true;
    }

    /**
     * 거래 간 간격이 일관적인지 확인
     */
    private boolean hasConsistentIntervals(List<Transaction> transactions) {
        if (transactions.size() < 2) {
            return false;
        }

        // 첫 번째 간격을 기준으로 설정
        LocalDateTime first = transactions.get(0).getTransactionDate();
        LocalDateTime second = transactions.get(1).getTransactionDate();
        Duration expectedInterval = Duration.between(first, second);

        // 모든 연속된 거래 간 간격을 확인
        for (int i = 1; i < transactions.size() - 1; i++) {
            LocalDateTime current = transactions.get(i).getTransactionDate();
            LocalDateTime next = transactions.get(i + 1).getTransactionDate();
            Duration actualInterval = Duration.between(current, next);

            // 예상 간격과 실제 간격의 차이가 허용 오차를 벗어나면 false
            Duration intervalDifference = actualInterval.minus(expectedInterval).abs();
            if (intervalDifference.compareTo(intervalTolerance) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 거래 금액이 유사한지 확인
     */
    private boolean hasSimilarAmounts(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return false;
        }

        // 평균 금액 계산
        BigDecimal averageAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(transactions.size()), BigDecimal.ROUND_HALF_UP);

        // 허용 오차 범위 계산
        BigDecimal tolerance = averageAmount.multiply(BigDecimal.valueOf(amountTolerancePercentage));

        // 모든 거래가 허용 오차 범위 내에 있는지 확인
        for (Transaction transaction : transactions) {
            BigDecimal difference = transaction.getAmount().subtract(averageAmount).abs();
            if (difference.compareTo(tolerance) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 패턴의 강도를 0~1 사이 값으로 반환
     * 1에 가까울수록 더 규칙적인 패턴
     */
    public double calculatePatternStrength(List<Transaction> transactions) {
        if (!isSatisfiedBy(transactions)) {
            return 0.0;
        }

        double intervalConsistency = calculateIntervalConsistency(transactions);
        double amountConsistency = calculateAmountConsistency(transactions);
        
        // 가중평균 (간격 일관성 60%, 금액 일관성 40%)
        return intervalConsistency * 0.6 + amountConsistency * 0.4;
    }

    private double calculateIntervalConsistency(List<Transaction> transactions) {
        if (transactions.size() < 2) {
            return 0.0;
        }

        List<Transaction> sortedTransactions = transactions.stream()
                .sorted((t1, t2) -> t1.getTransactionDate().compareTo(t2.getTransactionDate()))
                .toList();

        // 간격 변동성 계산 (표준편차 기반)
        List<Duration> intervals = calculateIntervals(sortedTransactions);
        double avgInterval = intervals.stream()
                .mapToLong(Duration::toHours)
                .average()
                .orElse(0.0);

        double variance = intervals.stream()
                .mapToDouble(interval -> Math.pow(interval.toHours() - avgInterval, 2))
                .average()
                .orElse(0.0);

        double standardDeviation = Math.sqrt(variance);
        
        // 변동 계수를 이용한 일관성 점수 (낮을수록 일관적)
        double coefficientOfVariation = avgInterval == 0 ? 1.0 : standardDeviation / avgInterval;
        
        return Math.max(0.0, 1.0 - coefficientOfVariation);
    }

    private double calculateAmountConsistency(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return 0.0;
        }

        double avgAmount = transactions.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .average()
                .orElse(0.0);

        double variance = transactions.stream()
                .mapToDouble(t -> Math.pow(t.getAmount().doubleValue() - avgAmount, 2))
                .average()
                .orElse(0.0);

        double standardDeviation = Math.sqrt(variance);
        
        // 변동 계수를 이용한 일관성 점수
        double coefficientOfVariation = avgAmount == 0 ? 1.0 : standardDeviation / avgAmount;
        
        return Math.max(0.0, 1.0 - coefficientOfVariation);
    }

    private List<Duration> calculateIntervals(List<Transaction> sortedTransactions) {
        List<Duration> intervals = new ArrayList<>();
        for (int i = 1; i < sortedTransactions.size(); i++) {
            LocalDateTime previous = sortedTransactions.get(i - 1).getTransactionDate();
            LocalDateTime current = sortedTransactions.get(i).getTransactionDate();
            intervals.add(Duration.between(previous, current));
        }
        return intervals;
    }

    // Getters
    public int getMinimumOccurrences() {
        return minimumOccurrences;
    }

    public Duration getIntervalTolerance() {
        return intervalTolerance;
    }

    public double getAmountTolerancePercentage() {
        return amountTolerancePercentage;
    }
}
