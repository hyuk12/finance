package com.finance.demo.transaction.application;

import com.finance.demo.transaction.domain.Transaction;
import com.finance.demo.transaction.domain.Category;
import com.finance.demo.shared.domain.UserId;
import com.finance.demo.shared.domain.Money;
import com.finance.demo.shared.domain.DateRange;
import com.finance.demo.shared.specification.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Day 2: 도메인 중심 리포지토리 인터페이스
 * 
 * 기술적 관점이 아닌 도메인/비즈니스 관점에서 설계된 리포지토리
 * - 유비쿼터스 언어 사용
 * - 컬렉션과 같은 인터페이스 제공
 * - 도메인 전문가가 이해할 수 있는 메서드명
 */
public interface TransactionRepository {
    
    // ========== 기본 컬렉션 연산 ==========
    
    /**
     * 거래를 저장합니다 (생성 또는 수정)
     */
    void save(Transaction transaction);
    
    /**
     * 거래를 제거합니다
     */
    void remove(Transaction transaction);
    
    /**
     * ID로 거래를 조회합니다
     */
    Optional<Transaction> findById(Long id);
    
    // ========== 도메인 중심 조회 메서드 ==========
    
    /**
     * 특정 사용자의 모든 거래를 조회합니다
     * 유비쿼터스 언어: "~의 거래들"
     */
    List<Transaction> transactionsOf(UserId userId);
    
    /**
     * 특정 사용자의 최근 거래를 제한된 개수만큼 조회합니다
     */
    List<Transaction> recentTransactionsOf(UserId userId, int limit);
    
    /**
     * 특정 카테고리의 거래들을 조회합니다
     */
    List<Transaction> transactionsInCategory(UserId userId, Category category);
    
    /**
     * 임계값 이상의 고액 거래들을 조회합니다
     * 유비쿼터스 언어: "고액 거래들"
     */
    List<Transaction> expensiveTransactionsOf(UserId userId, Money threshold);
    
    /**
     * 특정 기간 동안의 거래들을 조회합니다
     */
    List<Transaction> transactionsDuring(UserId userId, DateRange period);
    
    /**
     * 사용자의 거래를 카테고리별로 그룹화하여 조회합니다
     */
    Map<Category, List<Transaction>> transactionsByCategory(UserId userId);
    
    /**
     * 특정 기간 동안 카테고리별 거래를 그룹화하여 조회합니다
     */
    Map<Category, List<Transaction>> transactionsByCategory(UserId userId, DateRange period);
    
    // ========== 통계 및 집계 메서드 ==========
    
    /**
     * 특정 카테고리에서 특정 기간 동안 총 지출한 금액을 조회합니다
     */
    Money totalSpentInCategory(UserId userId, Category category, DateRange period);
    
    /**
     * 특정 기간 동안의 총 거래 횟수를 조회합니다
     */
    int countTransactionsInPeriod(UserId userId, DateRange period);
    
    /**
     * 특정 기간 동안의 평균 거래 금액을 조회합니다
     */
    Money averageTransactionAmount(UserId userId, DateRange period);
    
    /**
     * 가장 큰 지출 거래를 조회합니다
     */
    Optional<Transaction> largestExpenseOf(UserId userId);
    
    /**
     * 가장 빈번한 지출 카테고리를 조회합니다
     */
    Optional<Category> mostFrequentCategoryOf(UserId userId);
    
    // ========== 패턴 분석을 위한 특화 메서드 ==========
    
    /**
     * 정기적인 패턴을 가질 수 있는 거래들을 조회합니다
     * (같은 금액, 같은 카테고리, 일정한 간격)
     */
    List<List<Transaction>> potentialRecurringTransactions(UserId userId);
    
    /**
     * 비정상적으로 큰 금액의 거래들을 조회합니다
     * (사용자의 평균 지출 대비 현저히 큰 거래)
     */
    List<Transaction> unusuallyLargeTransactions(UserId userId);
    
    /**
     * 특정 요일에 발생한 거래들을 조회합니다
     */
    List<Transaction> transactionsOnWeekday(UserId userId, int dayOfWeek);
    
    /**
     * 특정 시간대에 발생한 거래들을 조회합니다
     */
    List<Transaction> transactionsInTimeRange(UserId userId, int startHour, int endHour);
    
    // ========== Specification 패턴 지원 ==========
    
    /**
     * Specification을 만족하는 모든 거래를 조회합니다
     */
    List<Transaction> findAll(Specification<Transaction> spec);
    
    /**
     * Specification을 만족하는 첫 번째 거래를 조회합니다
     */
    Optional<Transaction> findOne(Specification<Transaction> spec);
    
    /**
     * Specification을 만족하는 거래의 개수를 조회합니다
     */
    int count(Specification<Transaction> spec);
    
    /**
     * Specification을 만족하는 거래가 존재하는지 확인합니다
     */
    boolean exists(Specification<Transaction> spec);
    
    /**
     * Specification을 만족하는 거래를 페이징하여 조회합니다
     */
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);
    
    /**
     * 사용자별로 Specification을 적용하여 거래를 조회합니다
     */
    List<Transaction> findAllForUser(UserId userId, Specification<Transaction> spec);
}
