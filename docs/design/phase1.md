# Phase 1 설계 문서 — 테스트 코드 작성 (TDD Red 단계)

## 목표

구현 코드 없이 테스트 코드를 먼저 작성한다.
컴파일이 가능한 최소한의 클래스 시그니처(빈 껍데기)만 만들고,
모든 테스트는 실패(Red) 상태로 완료한다.

---

## 1. 프로젝트 설정

### 1-1. build.gradle.kts
- 루트 `src/main/java` 를 메인 소스로 사용
- 메인 클래스: `com.ssemi.sampleorder.App`
- Java 17 툴체인
- JUnit 5 의존성
- `standardInput = System.in` (콘솔 입력)

### 1-2. 패키지 구조

```
src/
├── main/java/com/ssemi/sampleorder/
│   ├── model/
│   │   ├── Sample.java          ← 껍데기
│   │   ├── Order.java           ← 껍데기
│   │   └── OrderStatus.java     ← enum (값만 정의)
│   ├── repository/
│   │   ├── SampleRepository.java   ← 인터페이스
│   │   ├── OrderRepository.java    ← 인터페이스
│   │   ├── CsvSampleRepository.java ← 껍데기 (메서드 미구현)
│   │   └── CsvOrderRepository.java  ← 껍데기 (메서드 미구현)
│   ├── controller/
│   │   ├── OrderController.java      ← 껍데기
│   │   ├── MonitoringController.java ← 껍데기
│   │   └── ReleaseController.java    ← 껍데기
│   │   (SampleController.java는 Phase 2에서 추가)
│   ├── production/
│   │   ├── ProductionJob.java   ← 껍데기
│   │   └── ProductionLine.java  ← 껍데기
│   └── App.java                 ← main() 만 존재
│
└── test/java/com/ssemi/sampleorder/
    ├── repository/
    │   ├── SampleRepositoryTest.java
    │   └── OrderRepositoryTest.java
    ├── controller/
    │   ├── OrderControllerTest.java
    │   ├── MonitoringControllerTest.java
    │   └── ReleaseControllerTest.java
    └── production/
        └── ProductionLineTest.java
```

---

## 2. 도메인 모델 시그니처

### Sample
```java
public class Sample {
    private String id;
    private String name;
    private long avgProductionTimeMs;
    private double yield;          // 수율 (0.0 ~ 1.0)
    private int stock;

    // 생성자, getter, setter
}
```

### Order
```java
public class Order {
    private String id;
    private String sampleId;
    private String customerName;
    private int quantity;
    private OrderStatus status;

    // 생성자, getter, setter
}
```

### OrderStatus
```java
public enum OrderStatus {
    RESERVED, REJECTED, PRODUCING, CONFIRMED, RELEASE
}
```

### ProductionJob
```java
public class ProductionJob {
    private String orderId;
    private String sampleId;
    private int requiredQty;     // 실 생산량
    private long productionTimeMs; // 총 생산 시간

    // 생성자, getter
}
```

---

## 3. Repository 인터페이스

### SampleRepository
```java
public interface SampleRepository {
    Sample save(Sample sample);
    List<Sample> findAll();
    Optional<Sample> findById(String id);
    boolean update(Sample sample);
    boolean deleteById(String id);
}
```

### OrderRepository
```java
public interface OrderRepository {
    Order save(Order order);
    List<Order> findAll();
    Optional<Order> findById(String id);
    boolean update(Order order);
    boolean deleteById(String id);
    List<Order> findByStatus(OrderStatus status);   // 상태별 조회
}
```

---

## 4. 테스트 코드 상세

### 4-1. SampleRepositoryTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `save_저장후_전체조회시_포함된다` | save 후 findAll 결과에 해당 시료 존재 |
| `findById_존재하는ID_반환된다` | 저장한 ID로 조회 시 동일 객체 반환 |
| `findById_없는ID_빈Optional반환` | 미존재 ID 조회 시 Optional.empty() |
| `update_수정후_변경값이_반영된다` | update 후 findById로 변경된 값 확인 |
| `deleteById_삭제후_조회되지않는다` | deleteById 후 findAll에 미포함 확인 |
| `deleteById_없는ID_false반환` | 미존재 ID 삭제 시 false 반환 |

- `@TempDir` 사용으로 임시 디렉터리에 CSV 파일 생성 → 테스트 간 독립성 보장

### 4-2. OrderRepositoryTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `save_저장후_전체조회시_포함된다` | save 후 findAll 결과에 해당 주문 존재 |
| `findById_존재하는ID_반환된다` | 저장한 ID로 조회 시 동일 객체 반환 |
| `update_상태변경후_반영된다` | 상태 변경 후 findById로 변경 확인 |
| `deleteById_삭제후_조회되지않는다` | deleteById 후 findAll에 미포함 확인 |
| `findByStatus_RESERVED_목록반환` | RESERVED 상태 주문만 필터링 반환 |
| `findByStatus_CONFIRMED_목록반환` | CONFIRMED 상태 주문만 필터링 반환 |

### 4-3. OrderControllerTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `createOrder_상태가_RESERVED이다` | 주문 생성 직후 status == RESERVED |
| `approveOrder_재고충분_CONFIRMED로전환` | 재고 >= 주문수량일 때 승인 → CONFIRMED |
| `approveOrder_재고부족_PRODUCING으로전환` | 재고 < 주문수량일 때 승인 → PRODUCING |
| `approveOrder_재고충분_재고가차감된다` | 승인 후 Sample.stock이 수량만큼 감소 |
| `rejectOrder_REJECTED로전환` | 거절 처리 후 status == REJECTED |
| `approveOrder_없는주문ID_예외발생` | 미존재 주문 ID 승인 시 `IllegalArgumentException` |
| `createOrder_없는샘플ID_예외발생` | 등록되지 않은 sampleId로 주문 생성 시 `IllegalArgumentException` |

### 4-4. ProductionLineTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `submit_작업등록시_자동으로생산시작` | 큐에 작업 등록 후 생산 스레드가 처리 시작 |
| `production_완료후_주문상태_CONFIRMED` | 생산 완료 시 해당 Order status == CONFIRMED |
| `production_완료후_재고증가` | 생산 완료 시 Sample.stock이 실 생산량만큼 증가 |
| `submit_복수작업_FIFO순서로처리` | 2개 작업 등록 시 먼저 등록한 작업이 먼저 완료 |
| `실생산량_계산_수율과오차반영` | `ceil(부족분 / yield / 0.9)` 공식 검증 |

- 생산 시간을 매우 짧게(예: 100ms) 설정하여 테스트 속도 확보
- `CountDownLatch` 또는 `Thread.sleep`으로 비동기 완료 대기

### 4-5. MonitoringControllerTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `getOrderCountByStatus_상태별_집계정확` | 각 상태별 주문 수 집계 값 검증 |
| `getOrderCountByStatus_REJECTED_제외` | REJECTED 주문은 집계에 포함되지 않음 |
| `getStockStatus_여유_재고충분` | 재고 > 주문수량 × 0.3 → 여유 |
| `getStockStatus_부족_30퍼센트이하` | 0 < 재고 ≤ 주문수량 × 0.3 → 부족 |
| `getStockStatus_고갈_재고없음` | 재고 == 0 → 고갈 |

### 4-6. ReleaseControllerTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `release_CONFIRMED주문_RELEASE로전환` | 출고 처리 후 status == RELEASE |
| `release_CONFIRMED아닌주문_예외발생` | RESERVED 등 다른 상태 출고 시 예외 |
| `getReleasableOrders_CONFIRMED만반환` | CONFIRMED 상태 주문만 목록에 포함 |

---

## 5. 작업 순서

```
1. build.gradle.kts 설정
2. OrderStatus enum 작성
3. 모델 클래스 시그니처 작성 (Sample, Order, ProductionJob)
4. Repository 인터페이스 작성
5. Repository CSV 구현체 껍데기 작성 (throw new UnsupportedOperationException())
6. Controller / ProductionLine 껍데기 작성
7. 테스트 코드 전체 작성
8. 전체 테스트 실행 → 모두 실패(Red) 확인
```

> 8번까지 완료 후 사용자 검토를 받고 Phase 2 (Green 단계) 로 진행한다.
