package com.finance.demo.pattern.presentation;

import com.finance.demo.pattern.application.PatternAnalysisService;
import com.finance.demo.pattern.domain.SpendingPattern;
import com.finance.demo.pattern.domain.PatternType;
import com.finance.demo.transaction.domain.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 패턴 분석 REST API
 * 1일차 실습을 위한 컨트롤러
 */
@RestController
@RequestMapping("/api/patterns")
public class PatternController {
    
    private final PatternAnalysisService patternAnalysisService;
    
    public PatternController(PatternAnalysisService patternAnalysisService) {
        this.patternAnalysisService = patternAnalysisService;
    }
    
    /**
     * 사용자의 소비 패턴 조회
     * GET /api/patterns?userId={userId}
     */
    @GetMapping
    public ResponseEntity<List<PatternResponse>> getUserPatterns(@RequestParam Long userId) {
        List<SpendingPattern> patterns = patternAnalysisService.getUserPatterns(userId);
        List<PatternResponse> response = patterns.stream()
            .map(this::toPatternResponse)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    private PatternResponse toPatternResponse(SpendingPattern pattern) {
        return new PatternResponse(
            pattern.getId(),
            pattern.getUserId(),
            pattern.getCategory(),
            pattern.getPatternType(),
            pattern.getAverageAmount(),
            pattern.getMinAmount(),
            pattern.getMaxAmount(),
            pattern.getConfidenceScore(),
            pattern.getOccurrenceCount(),
            pattern.isHighConfidence(),
            pattern.isStablePattern(),
            pattern.getCreatedAt(),
            pattern.getLastUpdated()
        );
    }
    
    // Response DTO
    public record PatternResponse(
        Long id,
        Long userId,
        Category category,
        PatternType patternType,
        BigDecimal averageAmount,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal confidenceScore,
        Integer occurrenceCount,
        boolean isHighConfidence,
        boolean isStablePattern,
        LocalDateTime createdAt,
        LocalDateTime lastUpdated
    ) {}
}
