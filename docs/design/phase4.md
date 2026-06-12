# Phase 4 설계 문서 — 생산 라인

## 목표

백그라운드 생산 스레드가 자동으로 처리되는 `ProductionLine`은 Phase 1에서 이미 구현 완료되어 있다.
Phase 4의 핵심은 **생산 현황 조회 UI 구현 + 메인 메뉴 대기 건수 반영 + thread-safe 강화**다.

---

## 1. Phase 1에서 이미 구현된 항목 (재사용)

| 항목 | 상태 | 비고 |
|---|---|---|
| `ProductionJob` | ✅ 완료 | orderId, sampleId, requiredQty, productionTimeMs |
| `ProductionLine` | ✅ 완료 | LinkedBlockingQueue + 단일 백그라운드 스레드, FIFO, 완료 시 CONFIRMED 전환 |
| 실 생산량 계산 | ✅ 완료 | `Math.ceil(부족분 / yield / 0.9)` — OrderController에 구현 |
| 총 생산시간 계산 | ✅ 완료 | `avgProductionTimeMs × 실 생산량` — OrderController에 구현 |

---

## 2. Phase 4 신규 구현 범위

| 구현 항목 | 설명 |
|---|---|
| `ProductionJob` 수정 | `startTime` 필드 추가 (예상 잔여 시간 계산용) |
| `ProductionLine` 수정 | `startTime` 설정 + thread-safe 동기화 강화 |
| `ProductionController` | 현재 생산 현황 조회, 대기 큐 목록·건수 조회 |
| `ProductionView` | 생산 현황 및 대기 큐 테이블 출력 |
| `MainView` 수정 | 메뉴 5번(생산 라인 조회) 활성화, 요약 정보 대기 건수 반영 |
| `App.java` 수정 | `ProductionController`, `ProductionView` 인스턴스 생성 및 연결 |

---

## 3. 패키지 구조 변경

```
src/main/java/com/ssemi/sampleorder/
├── production/
│   ├── ProductionJob.java      ← 수정 (startTime 추가)
│   └── ProductionLine.java     ← 수정 (startTime 설정, 동기화)
├── controller/
│   └── ProductionController.java  ← 신규
└── view/
    └── ProductionView.java     ← 신규
```

---

## 4. 비즈니스 로직 결정사항

| 항목 | 결정 | 근거 |
|---|---|---|
| `startTime` 타입 | `long` (System.currentTimeMillis() 기준 ms) | |
| `startTime` 위치 | `ProductionJob`에 `setStartTime` setter 추가 | 시작 시각은 작업 자체의 속성 — 별도 Map보다 단순 |
| 예상 잔여 시간 | `startTime + productionTimeMs - System.currentTimeMillis()` (ms 단위 출력) | |
| 잔여 시간이 음수인 경우 | "완료 처리 중" 메시지 출력 | 생산 완료 후 상태 전환 전 찰나를 사용자가 인지할 수 있도록 |
| 대기 건수 집계 | `ProductionLine.getWaitingJobs().size()` | |
| thread-safe 범위 | `CsvSampleRepository`, `CsvOrderRepository`의 `update()` 메서드를 `synchronized` 선언 | 백그라운드 스레드(ProductionLine)와 메인 스레드(승인 로직)가 모두 `update()`를 호출하므로 Repository 레벨에서 직렬화 |
| 생산 중 없을 때 | "현재 생산 중인 작업이 없습니다." 출력 | |
| 대기 큐 비었을 때 | "대기 중인 작업이 없습니다." 출력 | |

---

## 5. 클래스 시그니처

### 5-1. ProductionJob (수정)

```java
public class ProductionJob {
    private String orderId;
    private String sampleId;
    private int requiredQty;
    private long productionTimeMs;
    private long startTime;           // ← 추가: 생산 시작 시각 (ms)

    public ProductionJob(String orderId, String sampleId, int requiredQty, long productionTimeMs) { ... }

    public String getOrderId() { return orderId; }
    public String getSampleId() { return sampleId; }
    public int getRequiredQty() { return requiredQty; }
    public long getProductionTimeMs() { return productionTimeMs; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
}
```

### 5-2. ProductionLine (수정 부분만)

`startTime` 설정만 추가. thread-safe 처리는 Repository 레벨에서 담당하므로 별도 `synchronized` 블록 불필요.

```java
private void processLoop() {
    while (!Thread.currentThread().isInterrupted()) {
        try {
            ProductionJob job = queue.take();
            job.setStartTime(System.currentTimeMillis());  // ← 추가
            currentJob = job;
            try {
                Thread.sleep(job.getProductionTimeMs());

                Order order = orderRepository.findById(job.getOrderId()).orElse(null);
                if (order != null) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.update(order);         // synchronized 메서드
                }
                Sample sample = sampleRepository.findById(job.getSampleId()).orElse(null);
                if (sample != null) {
                    sample.setStock(sample.getStock() + job.getRequiredQty());
                    sampleRepository.update(sample);       // synchronized 메서드
                }
            } finally {
                currentJob = null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
        }
    }
}
```

### 5-3. CsvSampleRepository / CsvOrderRepository (수정 부분만)

```java
// CsvSampleRepository
public synchronized boolean update(Sample sample) { ... }

// CsvOrderRepository
public synchronized boolean update(Order order) { ... }
```

### 5-3. ProductionController (신규)

```java
public class ProductionController {

    public ProductionController(ProductionLine productionLine) { ... }

    // 현재 생산 중인 작업 (없으면 null)
    public ProductionJob getCurrentJob();

    // 대기 중인 작업 목록
    public List<ProductionJob> getWaitingJobs();

    // 대기 건수 (메인 메뉴 요약용)
    public int getWaitingCount();
}
```

### 5-4. ProductionView (신규)

```java
public class ProductionView {

    public ProductionView(ProductionController productionController) { ... }

    // 생산 현황 + 대기 큐 전체 출력 (메뉴 5번 진입 시 호출)
    public void showProductionStatus();
}
```

---

## 6. 화면 설계

### 6-1. 생산 중 + 대기 있는 경우

```
[생산 라인 조회]
--- 현재 생산 중 ---
주문ID   : a1b2c3d4
시료ID   : S001
생산량   : 13개
잔여시간 : 1200ms

--- 대기 중인 작업 ---
번호  주문ID(앞 8자리)  시료ID  생산량
------------------------------------------
1     e5f6g7h8         S002    8개
2     i9j0k1l2         S001    5개
```

### 6-2. 생산 중 없는 경우

```
[생산 라인 조회]
현재 생산 중인 작업이 없습니다.

--- 대기 중인 작업 ---
대기 중인 작업이 없습니다.
```

### 6-3. 메인 메뉴 요약 (수정 후)

```
=== 현황 요약 ===
등록 시료 수: 3
전체 재고 수: 120
전체 주문 수: 5
생산 대기 건수: 2
```

---

## 7. 테스트 케이스

### 7-1. ProductionControllerTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `getCurrentJob_생산중_현재작업반환` | 작업 제출 후 짧은 대기 → getCurrentJob() != null |
| `getCurrentJob_생산없음_null반환` | 작업 없을 때 getCurrentJob() == null |
| `getWaitingJobs_대기작업_목록반환` | 제출한 작업이 waiting 목록에 포함됨 |
| `getWaitingCount_대기건수_정확` | 제출 건수와 getWaitingCount() 일치 |

### 7-2. ProductionViewTest (System.setIn / System.setOut 활용)

| 테스트 메서드 | 검증 내용 |
|---|---|
| `showProductionStatus_생산중_정보출력` | 현재 생산 중 정보(주문ID, 시료ID, 생산량, 잔여시간)가 출력에 포함됨 |
| `showProductionStatus_생산없음_안내메시지` | "현재 생산 중인 작업이 없습니다." 출력 |
| `showProductionStatus_대기작업있음_목록출력` | 대기 작업 목록이 출력에 포함됨 |
| `showProductionStatus_대기없음_안내메시지` | "대기 중인 작업이 없습니다." 출력 |

---

## 8. 구현 순서

```
1. ProductionJob에 startTime 필드 추가
2. ProductionLine processLoop()에 startTime 설정 + synchronized 블록 추가
3. ProductionControllerTest 작성 (RED)
4. ProductionViewTest 작성 (RED)
5. ProductionController 구현 (GREEN)
6. ProductionView 구현 (GREEN)
7. MainView 수정: 메뉴 5번 활성화, printSummary()에 대기 건수 반영
8. App.java 수정: ProductionController, ProductionView 인스턴스 생성 및 연결
9. 전체 테스트 실행 → PASS 확인
10. 수동 실행으로 전체 흐름 확인
```

---

## 9. 검토 포인트 (Review Points) — 결정 완료

| # | 분류 | 포인트 | 결정 |
|---|---|---|---|
| P1-1 | 비즈니스 로직 | `startTime` 필드 위치 | ✅ `ProductionJob`에 `setStartTime` setter 추가 |
| P1-2 | 비즈니스 로직 | 잔여 시간 음수 처리 | ✅ "완료 처리 중" 메시지 출력 |
| P1-3 | 비즈니스 로직 | thread-safe 범위 | ✅ `CsvSampleRepository` / `CsvOrderRepository`의 `update()` 메서드를 `synchronized` 선언 |
| P2-4 | UI/UX | 생산 현황 표시 정보 | ✅ 주문ID(앞 8자리) + 시료ID + 생산량 + 잔여시간 |
| P2-5 | UI/UX | 대기 큐 표시 정보 | ✅ 번호 + 주문ID(앞 8자리) + 시료ID + 생산량 |
| P2-6 | UI/UX | 메뉴 5번 진입 후 동작 | ✅ 현황 출력 후 메인 메뉴로 복귀 (서브메뉴 없음) |
| P3-7 | 기술 | `ProductionController` 역할 | ✅ `ProductionLine` 단순 위임 |
| P3-8 | 기술 | `getWaitingCount()` 호출 경로 | ✅ `MainView` → `ProductionController.getWaitingCount()` |
| P3-9 | 기술 | `ProductionViewTest` 방식 | ✅ `ProductionController` stub 주입 (단위 테스트) |
