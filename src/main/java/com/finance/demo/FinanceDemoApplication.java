package com.finance.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 금융 도메인 - 소비패턴 분석 시스템
 * 
 * Day 1 학습 내용:
 * - 바운디드 컨텍스트 식별
 * - 컨텍스트 매핑
 * - 유비쿼터스 언어 정의
 * - 이벤트 스토밍
 */
@SpringBootApplication
public class FinanceDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceDemoApplication.class, args);
    }
}
