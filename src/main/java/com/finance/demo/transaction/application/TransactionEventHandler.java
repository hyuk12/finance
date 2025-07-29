package com.finance.demo.transaction.application;

import com.finance.demo.transaction.domain.event.TransactionCreatedEvent;
import com.finance.demo.transaction.domain.event.TransactionRecategorizedEvent;
import com.finance.demo.transaction.domain.event.HighAmountTransactionDetectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Day 2: 거래 도메인 이벤트 핸들러
 * 
 * 도메인 이벤트 처리:
 * - 거래 생성 시 패턴 분석 트리거
 * - 고액 거래 감지 시 알림 발송
 * - 재분류 시 예산 업데이트
 * 
 * 특징:
 * - 비동기 처리로 성능 향상
 * - 이벤트별 독립적인 처리 로직
 * - 실패 시에도 다른 핸들러에 영향 없음
 */
@Component
public class TransactionEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventHandler.class);

    /**
     * 거래 생성 이벤트 처리
     * - 패턴 분석 시스템에 새 거래 알림
     * - 예산 사용량 업데이트
     * - 통계 데이터 갱신
     */
    @Async
    @EventListener
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("거래 생성 이벤트 처리 시작: 거래 ID {}, 사용자 ID {}, 금액 {}", 
                event.transactionId(), event.userId(), event.amount());

        try {
            // 1. 패턴 분석 시스템에 통지
            triggerPatternAnalysis(event);
            
            // 2. 예산 사용량 업데이트
            updateBudgetUsage(event);
            
            // 3. 실시간 통계 업데이트
            updateRealTimeStatistics(event);
            
            log.info("거래 생성 이벤트 처리 완료: 거래 ID {}", event.transactionId());
            
        } catch (Exception e) {
            log.error("거래 생성 이벤트 처리 중 오류 발생: 거래 ID {}", event.transactionId(), e);
            // 실제 운영에서는 재시도 로직이나 데드레터 큐 처리 필요
        }
    }

    /**
     * 고액 거래 감지 이벤트 처리
     * - 즉시 알림 발송
     * - 보안 검증 요청
     * - 위험도 분석
     */
    @Async
    @EventListener
    public void handleHighAmountTransactionDetected(HighAmountTransactionDetectedEvent event) {
        log.warn("고액 거래 감지: 거래 ID {}, 금액 {}, 위험도 {}", 
                event.transactionId(), event.amount(), event.riskLevel());

        try {
            // 1. 즉시 알림이 필요한 경우
            if (event.requiresImmediateAlert()) {
                sendImmediateAlert(event);
            }
            
            // 2. 보안 검증이 필요한 경우
            if (event.requiresSecurityVerification()) {
                requestSecurityVerification(event);
            }
            
            // 3. 위험도 분석 및 기록
            analyzeAndRecordRisk(event);
            
            log.info("고액 거래 감지 이벤트 처리 완료: 거래 ID {}", event.transactionId());
            
        } catch (Exception e) {
            log.error("고액 거래 감지 이벤트 처리 중 오류 발생: 거래 ID {}", event.transactionId(), e);
        }
    }

    /**
     * 거래 재분류 이벤트 처리
     * - 예산 카테고리별 사용량 재계산
     * - 패턴 분석 데이터 업데이트
     * - 분류 정확도 학습
     */
    @Async
    @EventListener
    public void handleTransactionRecategorized(TransactionRecategorizedEvent event) {
        log.info("거래 재분류 이벤트 처리 시작: 거래 ID {}, {} -> {}", 
                event.transactionId(), event.previousCategory(), event.newCategory());

        try {
            // 1. 카테고리가 실제로 변경된 경우에만 처리
            if (event.isCategoryActuallyChanged()) {
                
                // 2. 예산 사용량 재계산
                recalculateBudgetUsage(event);
                
                // 3. 패턴 분석 데이터 업데이트
                if (event.affectsSpendingPattern()) {
                    updatePatternAnalysisData(event);
                }
                
                // 4. 자동 분류 정확도 개선을 위한 학습
                improveCategoryPrediction(event);
            }
            
            log.info("거래 재분류 이벤트 처리 완료: 거래 ID {}", event.transactionId());
            
        } catch (Exception e) {
            log.error("거래 재분류 이벤트 처리 중 오류 발생: 거래 ID {}", event.transactionId(), e);
        }
    }

    // ========== 개별 처리 메서드들 ==========

    private void triggerPatternAnalysis(TransactionCreatedEvent event) {
        log.debug("패턴 분석 트리거: 사용자 {}", event.userId());
        // 실제 구현: 패턴 분석 서비스 호출
        // patternAnalysisService.analyzeNewTransaction(event);
    }

    private void updateBudgetUsage(TransactionCreatedEvent event) {
        log.debug("예산 사용량 업데이트: 카테고리 {}, 금액 {}", event.category(), event.amount());
        // 실제 구현: 예산 서비스 호출
        // budgetService.updateUsage(event.userId(), event.category(), event.amount());
    }

    private void updateRealTimeStatistics(TransactionCreatedEvent event) {
        log.debug("실시간 통계 업데이트: 사용자 {}", event.userId());
        // 실제 구현: 통계 서비스 호출
        // statisticsService.updateRealTimeStats(event);
    }

    private void sendImmediateAlert(HighAmountTransactionDetectedEvent event) {
        log.warn("즉시 알림 발송: 사용자 {}, 금액 {}", event.userId(), event.amount());
        // 실제 구현: 알림 서비스 호출
        // alertService.sendHighAmountAlert(event);
        
        // SMS, 이메일, 푸시 알림 등 다중 채널 알림
        sendMultiChannelAlert(event);
    }

    private void requestSecurityVerification(HighAmountTransactionDetectedEvent event) {
        log.warn("보안 검증 요청: 거래 ID {}, 위험도 {}", event.transactionId(), event.riskLevel());
        // 실제 구현: 보안 서비스 호출
        // securityService.requestVerification(event);
    }

    private void analyzeAndRecordRisk(HighAmountTransactionDetectedEvent event) {
        log.debug("위험도 분석 및 기록: 거래 ID {}", event.transactionId());
        // 실제 구현: 위험 분석 서비스 호출
        // riskAnalysisService.analyzeAndRecord(event);
    }

    private void recalculateBudgetUsage(TransactionRecategorizedEvent event) {
        log.debug("예산 사용량 재계산: {} -> {}, 금액 {}", 
                event.previousCategory(), event.newCategory(), event.amount());
        // 실제 구현: 
        // 1. 이전 카테고리에서 금액 차감
        // 2. 새 카테고리에 금액 추가
        // budgetService.recalculateUsage(event);
    }

    private void updatePatternAnalysisData(TransactionRecategorizedEvent event) {
        log.debug("패턴 분석 데이터 업데이트: 거래 ID {}", event.transactionId());
        // 실제 구현: 패턴 분석 서비스 호출
        // patternAnalysisService.updateForRecategorization(event);
    }

    private void improveCategoryPrediction(TransactionRecategorizedEvent event) {
        log.debug("카테고리 예측 정확도 개선: 거래 ID {}", event.transactionId());
        // 실제 구현: 머신러닝 모델 피드백
        // mlService.improveClassification(event);
    }

    private void sendMultiChannelAlert(HighAmountTransactionDetectedEvent event) {
        // 알림 채널별 발송 로직
        log.debug("다중 채널 알림 발송 시작");
        
        // SMS 알림
        sendSmsAlert(event);
        
        // 이메일 알림  
        sendEmailAlert(event);
        
        // 앱 푸시 알림
        sendPushNotification(event);
    }

    private void sendSmsAlert(HighAmountTransactionDetectedEvent event) {
        log.debug("SMS 알림 발송: 사용자 {}", event.userId());
        // SMS 발송 로직
    }

    private void sendEmailAlert(HighAmountTransactionDetectedEvent event) {
        log.debug("이메일 알림 발송: 사용자 {}", event.userId());
        // 이메일 발송 로직
    }

    private void sendPushNotification(HighAmountTransactionDetectedEvent event) {
        log.debug("푸시 알림 발송: 사용자 {}", event.userId());
        // 푸시 알림 발송 로직
    }
}
