# 🎯 DDD Day 1 실습 가이드

## 📚 학습 목표 복습
오늘 구현한 코드를 통해 다음 DDD 개념들을 확인해보세요:

### 1️⃣ 바운디드 컨텍스트 식별
- **계정 관리 컨텍스트**: `com.finance.demo.account`
- **거래 데이터 컨텍스트**: `com.finance.demo.transaction`  
- **패턴 분석 컨텍스트**: `com.finance.demo.pattern`
- **예산 관리 컨텍스트**: `com.finance.demo.budget`

### 2️⃣ 유비쿼터스 언어 적용
- **Transaction**: 거래 (금융 계좌의 입출금 기록)
- **Category**: 카테고리 (거래 분류 기준)
- **Pattern**: 패턴 (반복적 소비 행동)
- **Budget**: 예산 (카테고리별 지출 계획)

### 3️⃣ 이벤트 스토밍 결과 반영
- **TransactionCreatedEvent**: "거래가 생성되었다"
- **PatternDiscoveredEvent**: "패턴이 발견되었다"
- **BudgetExceededEvent**: "예산이 초과되었다"

### 4️⃣ 컨텍스트 매핑
- **Customer-Supplier**: 거래 데이터 → 패턴 분석
- **Partnership**: 계정 관리 ↔ 거래 데이터

---

## 🚀 실습 실행 방법

### 1. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 2. 데모 데이터 확인
애플리케이션이 시작되면 자동으로 다음과 같은 데모 데이터가 생성됩니다:

**사용자:**
- 김철수 (ID: 1) - 규칙적인 소비 패턴
- 이영희 (ID: 2) - 불규칙한 소비 패턴  
- 박민수 (ID: 3) - 절약형 소비 패턴

### 3. H2 데이터베이스 확인
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa`
- Password: (비어있음)

### 4. API 테스트

#### 거래 목록 조회
```bash
curl "http://localhost:8080/api/transactions?userId=1"
```

#### 패턴 분석 결과 조회  
```bash
curl "http://localhost:8080/api/patterns?userId=1"
```

#### 새로운 거래 생성
```bash
curl -X POST "http://localhost:8080/api/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "amount": 15000,
    "transactionDate": "2025-01-28T12:30:00",
    "description": "점심식사",
    "type": "EXPENSE",
    "category": "FOOD"
  }'
```

---

## 🔍 코드 구조 분석

### 도메인 모델 (Domain Model)
각 바운디드 컨텍스트의 핵심 비즈니스 로직이 담긴 엔티티들:

**Transaction.java**
```java
// 거래 도메인의 핵심 비즈니스 규칙
public void categorize(Category newCategory) {
    if (newCategory == null) {
        throw new IllegalArgumentException("카테고리는 필수입니다");
    }
    this.category = newCategory;
}
```

**SpendingPattern.java**
```java
// 패턴의 신뢰도와 안정성을 판단하는 비즈니스 로직
public boolean isStablePattern() {
    return this.occurrenceCount >= 5 && isHighConfidence();
}
```

### 도메인 이벤트 (Domain Events)
이벤트 스토밍에서 도출된 도메인 이벤트들:

**TransactionCreatedEvent.java**
- 거래 생성 시 발행
- 패턴 분석 컨텍스트에서 구독하여 분석 수행

**PatternDiscoveredEvent.java**  
- 새로운 패턴 발견 시 발행
- 알림 컨텍스트에서 사용자에게 인사이트 제공

### 애플리케이션 서비스 (Application Services)
바운디드 컨텍스트 간의 협력을 조율:

**TransactionService.java**
- 거래 생성 시 도메인 이벤트 발행
- 다른 컨텍스트에 데이터 제공 (Customer-Supplier)

**PatternAnalysisService.java**
- TransactionCreatedEvent 구독
- 패턴 분석 후 PatternDiscoveredEvent 발행

---

## 🧪 실습 과제

### 과제 1: 새로운 카테고리 추가
`Category.java`에 새로운 카테고리를 추가하고 테스트해보세요.

### 과제 2: 예산 관리 기능 완성
현재 도메인 모델만 있는 `Budget` 클래스를 위한:
- Repository 인터페이스 작성
- Application Service 작성  
- REST Controller 작성

### 과제 3: 알림 컨텍스트 구현
BudgetExceededEvent와 PatternDiscoveredEvent를 구독하는:
- NotificationService 작성
- 이메일/SMS 발송 로직 구현 (모의)

### 과제 4: 이벤트 로그 확인
애플리케이션 로그에서 도메인 이벤트 발행을 확인하고, 컨텍스트 간 데이터 흐름을 추적해보세요.

---

## 🎓 학습 점검 사항

### ✅ 바운디드 컨텍스트 이해도 체크
- [ ] 각 컨텍스트의 책임이 명확히 분리되어 있는가?
- [ ] 컨텍스트 간 의존성이 이벤트를 통해 느슨하게 결합되어 있는가?
- [ ] 같은 용어(예: User)가 컨텍스트마다 다른 의미를 가지는가?

### ✅ 유비쿼터스 언어 적용도 체크  
- [ ] 코드의 클래스명, 메서드명이 비즈니스 용어와 일치하는가?
- [ ] 주석과 문서에서 일관된 용어를 사용하고 있는가?
- [ ] 도메인 전문가와 대화할 때 코드와 같은 용어를 사용할 수 있는가?

### ✅ 이벤트 스토밍 반영도 체크
- [ ] "~가 발생했다" 형태의 도메인 이벤트가 구현되어 있는가?
- [ ] 이벤트 기반으로 컨텍스트 간 통신이 이루어지는가? 
- [ ] 비즈니스 프로세스의 흐름이 이벤트 체인으로 표현되는가?

### ✅ 컨텍스트 매핑 구현도 체크
- [ ] Customer-Supplier 관계가 이벤트 발행/구독으로 구현되어 있는가?
- [ ] Partnership 관계가 적절히 모델링되어 있는가?
- [ ] 각 컨텍스트가 독립적으로 배포 가능한 구조인가?

---

## 🔥 고급 실습 (선택사항)

### 1. CQRS 패턴 적용
읽기 전용 모델을 별도로 분리해보세요:
- TransactionReadModel 생성
- 조회 성능 최적화
- Command와 Query 분리

### 2. 이벤트 소싱 적용  
도메인 이벤트를 저장하고 재생하는 구조로 확장:
- EventStore 구현
- 이벤트 재생을 통한 상태 복원
- 스냅샷 생성

### 3. 마이크로서비스 분할
각 바운디드 컨텍스트를 별도 서비스로 분리:
- 독립적인 데이터베이스
- REST API 통신
- 메시지 큐를 통한 이벤트 전달

---

## 🤝 Day 2 준비사항

내일은 오늘 구현한 바운디드 컨텍스트를 바탕으로 다음을 학습합니다:

1. **애그리게이트 설계**: 트랜잭션 경계와 일관성 보장
2. **도메인 서비스**: 복잡한 비즈니스 로직 처리  
3. **값 객체**: 불변성과 도메인 개념 표현
4. **리포지토리 패턴**: 도메인과 인프라 분리

오늘 구현한 코드를 바탕으로 더 정교한 도메인 모델을 설계해보겠습니다!

---

**Happy Coding! 🚀**
