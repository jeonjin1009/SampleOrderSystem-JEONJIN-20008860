# PLAN.md — 반도체 시료 생산주문관리 시스템 구현 계획

## 구현 전략

PoC에서 검증한 MVC 패턴 + CSV 파일 영속성을 그대로 적용한다.
`src/main/java/com/ssemi/sampleorder/` 하위에 아래 패키지 구조로 개발한다.

```
com.ssemi.sampleorder
├── model/          도메인 객체 (Sample, Order, OrderStatus, StockStatus)
├── repository/     CSV 파일 CRUD (SampleRepository, OrderRepository)
├── controller/     비즈니스 로직
├── production/     생산 라인 (ProductionJob, ProductionLine)
├── view/           콘솔 입출력
└── App.java        진입점
```

---

## PoC 활용 계획

| PoC | 활용 단계 | 활용 내용 |
|-----|-----------|-----------|
| **ConsoleMVC** | Phase 1 ~ 전 단계 | MVC 패키지 구조와 역할 분리 방식을 그대로 참고하여 적용 |
| **DataPersistence** | Phase 1, 2, 3 | `Repository` 인터페이스 및 `CsvSampleRepository` 구현 코드를 `SampleRepository` / `OrderRepository`로 이식 |
| **DummyDataGenerator** | Phase 3, 4, 5 | 주문·시료 더미 데이터를 생성하여 승인/생산/모니터링 흐름 수동 테스트에 활용 |
| **DataMonitor** | Phase 2 ~ 전 단계 | 개발 중 CSV 파일 상태를 실시간으로 확인하는 디버깅 도구로 활용 |

---

## Phase 1. 테스트 코드 작성 (TDD Red 단계)

**목표**: 구현 전에 전체 시스템의 테스트 코드를 먼저 작성한다. 이 단계에서는 모든 테스트가 실패(Red) 상태여야 한다.

### 1-1. 프로젝트 기반 설정
- [ ] 루트 `build.gradle.kts` — 메인 클래스 지정, Java 17, standardInput 설정
- [ ] 도메인 모델 클래스 시그니처만 작성 (테스트 컴파일을 위한 빈 껍데기)
  - `Sample` — id, name, avgProductionTimeMs, yield, stock
  - `Order` — id, sampleId, customerName, quantity, status(OrderStatus)
  - `OrderStatus` — RESERVED / REJECTED / PRODUCING / CONFIRMED / RELEASE (enum)
- [ ] Repository 인터페이스 시그니처만 작성
  - `SampleRepository` — Sample CRUD
  - `OrderRepository` — Order CRUD + 상태별 조회

### 1-2. 테스트 코드 작성
- [ ] `SampleRepositoryTest` — CSV CRUD 전체 시나리오 (임시 디렉터리 사용)
  - 시료 저장 후 전체 조회 시 저장한 시료가 포함되는가
  - ID로 조회 시 정확한 시료가 반환되는가
  - 수정 후 조회 시 변경된 값이 반영되는가
  - 삭제 후 조회 시 해당 시료가 없는가
- [ ] `OrderRepositoryTest` — 주문 CRUD + 상태별 조회
  - 주문 저장/조회/수정/삭제
  - 상태(RESERVED, CONFIRMED 등)별 주문 목록 조회
- [ ] `OrderControllerTest` — 핵심 비즈니스 로직
  - 주문 생성 시 OrderStatus가 RESERVED인가
  - 승인 시 재고 충분 → CONFIRMED로 전환되는가
  - 승인 시 재고 부족 → PRODUCING으로 전환되는가
  - 거절 시 REJECTED로 전환되는가
- [ ] `ProductionLineTest` — 생산 큐 및 자동 처리
  - 생산 큐에 작업 추가 시 자동으로 생산이 시작되는가
  - 생산 완료 시 주문 상태가 CONFIRMED로 전환되는가
  - FIFO 순서로 처리되는가
- [ ] `MonitoringControllerTest` — 집계 로직
  - 상태별 주문 수가 정확히 집계되는가
  - 재고 상태(여유/부족/고갈)가 기준(30%)에 맞게 판단되는가
- [ ] `ReleaseControllerTest` — 출고 처리
  - CONFIRMED 주문만 출고 처리 가능한가
  - 출고 후 상태가 RELEASE로 전환되는가

---

## Phase 2. 시료 관리

**목표**: 시료를 등록·조회·검색할 수 있다

- [ ] `SampleController` — 등록 / 전체 조회 / 이름 검색 로직
- [ ] `SampleView` — 시료 입력 폼, 테이블 출력, 중복 ID 재입력 루프
- [ ] `MainView` — 메인 메뉴 루프, 요약 정보 출력
- [ ] `App.java` — MainView 연결, 메뉴 루프 통합
- [ ] 시료 등록 시 중복 ID 검증 (예외 발생 후 재입력 유도)
- [ ] 메인 메뉴 요약 정보에서 등록 시료 수 / 전체 재고 수 반영

---

## Phase 3. 주문 접수 및 승인/거절

**목표**: 주문을 생성하고 승인·거절 처리할 수 있다

- [ ] `OrderController` — 주문 생성(RESERVED), 승인, 거절 로직
- [ ] `OrderView` — 주문 입력 폼, RESERVED 목록 출력
- [ ] 주문 생성 시 유효한 시료 ID인지 검증
- [ ] 주문 승인 처리 분기
  - 재고 충분 → 즉시 `CONFIRMED`로 전환, 재고 차감
  - 재고 부족 → `PRODUCING`으로 전환 후 생산 큐에 자동 등록
- [ ] 주문 거절 → 즉시 `REJECTED`로 전환
- [ ] 메인 메뉴 요약 정보에서 전체 주문 수 반영

---

## Phase 4. 생산 라인

**목표**: 백그라운드에서 생산이 자동으로 처리된다

- [ ] `ProductionJob` — 생산 작업 단위 (orderId, sampleId, requiredQty, startTime 등)
- [ ] `ProductionLine` — `LinkedBlockingQueue` + 단일 백그라운드 스레드
  - 큐에 등록되면 즉시 생산 시작
  - `Thread.sleep(총 생산시간)`으로 실제 시간 경과 처리
  - 생산 완료 시 재고 추가 + 해당 Order 상태 `PRODUCING → CONFIRMED` 자동 전환
  - 앞 작업 완료 후 다음 큐 작업 자동 처리 (FIFO)
- [ ] 실 생산량 계산: `Math.ceil(부족분 / yield / 0.9)`
- [ ] 총 생산 시간 계산: `avgProductionTimeMs × 실 생산량`
- [ ] `ProductionController` — 현재 생산 현황 조회, 대기 큐 목록 조회
- [ ] `ProductionView` — 생산 현황 및 대기 큐 테이블 출력
- [ ] 메인 메뉴 요약 정보에서 생산 라인 대기 건수 반영
- [ ] thread-safe 처리: 재고·주문 상태 변경 시 동기화

---

## Phase 5. 모니터링 및 출고 처리

**목표**: 현재 시스템 상태를 한눈에 파악하고 출고를 처리할 수 있다

- [ ] `MonitoringController` — 상태별 주문 수, 시료별 재고 현황 집계
- [ ] `MonitoringView` — 주문량 현황 테이블, 재고 상태 테이블 출력
  - 재고 상태 표기: 여유(재고 > 주문수량 × 0.3) / 부족(0 < 재고 ≤ 주문수량 × 0.3) / 고갈(재고 = 0)
  - REJECTED 주문은 집계에서 제외
- [ ] `ReleaseController` — CONFIRMED 주문 목록 조회, 출고 처리 (→ RELEASE)
- [ ] `ReleaseView` — CONFIRMED 주문 목록 출력, 출고 확인 입력

---

## Phase별 의존 관계

```
Phase 1 (기반)
    ↓
Phase 2 (시료 관리)
    ↓
Phase 3 (주문 접수/승인)
    ↓
Phase 4 (생산 라인)   ←── Phase 3 승인 로직과 연동
    ↓
Phase 5 (모니터링/출고)
```

각 Phase 완료 후 동작 확인하고 다음 Phase로 진행한다.
