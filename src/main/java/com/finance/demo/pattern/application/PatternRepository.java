package com.finance.demo.pattern.application;

import com.finance.demo.pattern.domain.SpendingPattern;
import com.finance.demo.transaction.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 패턴 분석 컨텍스트의 Repository 인터페이스
 */
@Repository
public interface PatternRepository extends JpaRepository<SpendingPattern, Long> {
    
    List<SpendingPattern> findByUserIdAndCategory(Long userId, Category category);
    
    List<SpendingPattern> findByUserIdOrderByConfidenceScoreDesc(Long userId);
    
    List<SpendingPattern> findByUserIdAndConfidenceScoreGreaterThanEqual(Long userId, 
                                                                        java.math.BigDecimal confidenceScore);
}
