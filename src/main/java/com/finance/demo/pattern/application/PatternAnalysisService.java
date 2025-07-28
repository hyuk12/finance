package com.finance.demo.pattern.application;

import com.finance.demo.pattern.domain.SpendingPattern;
import com.finance.demo.pattern.domain.PatternType;
import com.finance.demo.pattern.domain.event.PatternDiscoveredEvent;
import com.finance.demo.transaction.domain.Transaction;
import com.finance.demo.transaction.domain.Category;
import com.finance.demo.transaction.domain.event.TransactionCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;  
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 패턴 분석 컨텍스트의 Application Service
 * 
 * 컨텍스트 매핑에서 정의한 바와 같이:
 * - 거래 데이터 컨텍스트로부터 TransactionCreatedEvent를 구독 (Customer-Supplier 관계)
 * - 패턴 발견 시 다른 컨텍스트에 이벤트를 발행
 */
@Service
@Transactional
public class PatternAnalysisService {
    
    private final PatternRepository patternRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public PatternAnalysisService(PatternRepository patternRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.patternRepository = patternRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 거래 생성 이벤트를 구독하여 패턴 분석 수행
     * 이벤트 스토밍에서 도출된 반응형 프로세스
     */
    @EventListener
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        // 지출 거래만 패턴 분석 대상
        if (event.type().name().equals("EXPENSE")) {
            analyzeSpendingPattern(event.userId(), event.category());
        }
    }
    
    /**
     * 소비 패턴 분석 로직
     * 유비쿼터스 언어: "패턴을 분석하다"
     */
    private void analyzeSpendingPattern(Long userId, Category category) {
        // 기존 패턴 조회
        List<SpendingPattern> existingPatterns = patternRepository
            .findByUserIdAndCategory(userId, category);
        
        // 모의 분석 로직 (실제로는 더 복잡한 ML 알고리즘 사용)
        PatternAnalysisResult result = performPatternAnalysis(userId, category);
        
        if (result.isPatternFound()) {
            SpendingPattern pattern = new SpendingPattern(
                userId,
                category,
                result.patternType(),
                result.averageAmount(),
                result.minAmount(),
                result.maxAmount(),
                result.confidenceScore(),
                result.occurrenceCount()
            );
            
            SpendingPattern savedPattern = patternRepository.save(pattern);
            
            // 패턴 발견 이벤트 발행
            PatternDiscoveredEvent patternEvent = new PatternDiscoveredEvent(
                savedPattern.getId(),
                savedPattern.getUserId(),
                savedPattern.getCategory(),
                savedPattern.getPatternType(),
                savedPattern.getAverageAmount(),
                savedPattern.getConfidenceScore(),
                savedPattern.getOccurrenceCount()
            );
            
            eventPublisher.publishEvent(patternEvent);
        }
    }
    
    /**
     * 패턴 분석 수행 (모의 구현)
     * 실제 환경에서는 머신러닝 알고리즘을 사용
     */
    private PatternAnalysisResult performPatternAnalysis(Long userId, Category category) {
        // 모의 분석 결과
        return new PatternAnalysisResult(
            true,
            PatternType.WEEKLY,
            new BigDecimal("50000"),
            new BigDecimal("30000"),
            new BigDecimal("80000"),
            new BigDecimal("0.85"),
            5
        );
    }
    
    /**
     * 사용자의 모든 패턴 조회
     */
    @Transactional(readOnly = true)
    public List<SpendingPattern> getUserPatterns(Long userId) {
        return patternRepository.findByUserIdOrderByConfidenceScoreDesc(userId);
    }
    
    /**
     * 패턴 분석 결과를 담는 내부 클래스
     */
    private record PatternAnalysisResult(
        boolean isPatternFound,
        PatternType patternType,
        BigDecimal averageAmount,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal confidenceScore,
        Integer occurrenceCount
    ) {}
}
