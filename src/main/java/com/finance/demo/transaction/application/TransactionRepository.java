package com.finance.demo.transaction.application;

import com.finance.demo.transaction.domain.Transaction;
import com.finance.demo.transaction.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 거래 데이터 컨텍스트의 Repository 인터페이스
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    
    List<Transaction> findByUserIdAndCategoryOrderByTransactionDateDesc(Long userId, Category category);
    
    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, 
                                                          LocalDateTime startDate, 
                                                          LocalDateTime endDate);
    
    List<Transaction> findByUserIdAndCategoryAndTransactionDateBetween(Long userId, 
                                                                     Category category,
                                                                     LocalDateTime startDate, 
                                                                     LocalDateTime endDate);
}
