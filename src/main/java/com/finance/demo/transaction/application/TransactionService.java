package com.finance.demo.transaction.application;

import com.finance.demo.transaction.domain.Transaction;
import com.finance.demo.transaction.domain.TransactionType;
import com.finance.demo.transaction.domain.Category;
import com.finance.demo.transaction.domain.event.TransactionCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 거래 데이터 컨텍스트의 Application Service
 * 
 * 컨텍스트 매핑에서 정의한 바와 같이:
 * - 다른 컨텍스트에 이벤트를 발행하여 데이터를 제공 (Customer-Supplier 관계)
 * - 계정 관리 컨텍스트와는 Partnership 관계로 사용자 정보를 함께 활용
 */
@Service
@Transactional
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public TransactionService(TransactionRepository transactionRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 새로운 거래를 생성하고 관련 이벤트를 발행
     * 이벤트 스토밍에서 도출된 "거래를 생성하다" 커맨드의 구현
     */
    public Long createTransaction(Long userId, BigDecimal amount, LocalDateTime transactionDate,
                                String description, TransactionType type, Category category) {
        
        // 도메인 객체 생성
        Transaction transaction = new Transaction(
            userId, amount, transactionDate, description, type, category
        );
        
        // 영속화
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // 도메인 이벤트 발행 - 다른 컨텍스트들이 구독할 수 있음
        TransactionCreatedEvent event = new TransactionCreatedEvent(
            savedTransaction.getId(),
            savedTransaction.getUserId(),
            savedTransaction.getAmount(),
            savedTransaction.getType(),
            savedTransaction.getCategory(),
            savedTransaction.getTransactionDate(),
            savedTransaction.getDescription()
        );
        
        eventPublisher.publishEvent(event);
        
        return savedTransaction.getId();
    }
    
    /**
     * 사용자별 거래 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
    }
    
    /**
     * 카테고리별 거래 목록 조회 (패턴 분석 컨텍스트에서 사용)
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByUserAndCategory(Long userId, Category category) {
        return transactionRepository.findByUserIdAndCategoryOrderByTransactionDateDesc(userId, category);
    }
    
    /**
     * 거래 재분류 (유비쿼터스 언어: "거래를 분류하다")
     */
    public void categorizeTransaction(Long transactionId, Category newCategory) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다: " + transactionId));
        
        transaction.categorize(newCategory);
        transactionRepository.save(transaction);
        
        // 재분류 이벤트 발행 가능 (필요시)
    }
}
