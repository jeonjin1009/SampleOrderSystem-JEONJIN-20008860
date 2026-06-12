# Phase 5 설계 문서 — 모니터링 및 출고 처리

## 목표

현재 시스템 상태를 한눈에 파악하고 출고를 처리할 수 있다.
`MonitoringController`와 `ReleaseController`는 Phase 1에서 이미 구현 완료되어 있으므로,
Phase 5의 핵심은 **`MonitoringView` · `ReleaseView` UI 구현 + `MainView` 메뉴 4·6번 활성화**다.

---

## 1. Phase 1에서 이미 구현된 항목 (재사용 / 수정)

| 항목 | 상태 | 주요 메서드 |
|---|---|---|
| `MonitoringController` | ✅ 완료 | `getOrderCountByStatus()` → `Map<OrderStatus, Long>` (REJECTED 제외) |
| `MonitoringController` | ⚠️ 수정 필요 | `getStockStatus(sampleId)` — 재고 상태 판단의 "주문수량" 기준 변경 필요 (아래 버그 항목 참조) |
| `ReleaseController` | ✅ 완료 | `getReleasableOrders()` → CONFIRMED 주문 목록 |
| `ReleaseController` | ⚠️ 수정 필요 | `release(orderId)` — 재고 차감 로직 추가 필요 (아래 버그 항목 참조) |
| `StockStatus` enum | ✅ 완료 | SUFFICIENT(여유) / SHORT(부족) / EMPTY(고갈) |

### Phase 1 구현 버그 — Phase 5에서 수정

**버그 1: ReleaseController.release()에 재고 차감 없음**

현재 재고 흐름:

| 케이스 | 승인(approveOrder) 시 | 생산 완료(ProductionLine) 시 | 출고(release) 시 |
|---|---|---|---|
| 재고 충분 | 재고 -= quantity | — | 차감 없음 (승인 시 이미 차감) ✅ |
| 재고 부족 | 차감 없음 | 재고 += requiredQty | 차감 없음 ❌ **버그** |

재고 부족 케이스에서 생산 완료 후 재고가 입고되지만, 출고 시 주문 수량만큼 차감되지 않는다.
→ **`release()` 수정**: 출고 시 `sample.setStock(stock - order.getQuantity())` 차감 추가.

**버그 2: MonitoringController.getStockStatus()의 "주문수량" 기준 오류**

현재 구현은 REJECTED 제외 전체 주문(RESERVED + PRODUCING + CONFIRMED + **RELEASE**) 수량을 합산.
RELEASE(출고 완료) 주문은 이미 처리된 건이므로 재고 압박 지표로 사용하면 부정확함.
→ **`getStockStatus()` 수정**: RESERVED + PRODUCING 진행 중 주문만 합산.

---

## 2. Phase 5 신규 구현 범위

| 구현 항목 | 설명 |
|---|---|
| `MonitoringView` | 상태별 주문 수 테이블, 시료별 재고 현황 테이블 출력 |
| `ReleaseView` | CONFIRMED 주문 목록 출력, 번호 선택 후 출고 처리 |
| `MainView` 수정 | 메뉴 4번(모니터링), 6번(출고 처리) 활성화 |
| `App.java` 수정 | `MonitoringController`, `ReleaseController`, View 인스턴스 생성 및 연결 |
| `ReleaseController.release()` 수정 | 출고 시 재고 차감 로직 추가 (버그 수정) |
| `MonitoringController.getStockStatus()` 수정 | 재고 상태 판단 기준을 RESERVED+PRODUCING으로 좁힘 (버그 수정) |

---

## 3. 패키지 구조 변경

```
src/main/java/com/ssemi/sampleorder/
├── view/
│   ├── MonitoringView.java     ← 신규
│   └── ReleaseView.java        ← 신규
└── App.java                    ← 수정
```

---

## 4. 비즈니스 로직 결정사항

| 항목 | 결정 |
|---|---|
| 주문량 현황 집계 범위 | REJECTED 제외 (RESERVED + PRODUCING + CONFIRMED + RELEASE) — MonitoringController 기존 로직 유지 |
| 재고 상태 판단 수식 | SUFFICIENT: 재고 > 주문수량 × 0.3 / SHORT: 0 < 재고 ≤ 주문수량 × 0.3 / EMPTY: 재고 = 0 |
| 재고 상태의 "주문수량" 기준 | **RESERVED + PRODUCING 진행 중 주문만 합산** (버그 수정 — CONFIRMED/RELEASE 제외) |
| 출고 시 재고 차감 | **재고 부족 케이스에서 출고 시 재고 -= quantity** (버그 수정) |
| 출고 선택 방식 | CONFIRMED 목록 출력 후 **번호(인덱스)** 입력으로 선택 (OrderView와 동일 패턴) |
| CONFIRMED 주문 없을 시 | "출고 가능한 주문이 없습니다." 출력 후 반환 |
| 주문/시료 데이터 없을 시 | 각 테이블에 해당 내용 없으면 "데이터가 없습니다." 메시지 출력 |
| 출고 결과 안내 | "출고 완료 (RELEASE) — 주문번호: {앞 8자리}..." 출력 |

---

## 5. 클래스 시그니처

### MonitoringView

```java
public class MonitoringView {

    public MonitoringView(MonitoringController monitoringController,
                          SampleController sampleController) { ... }

    // 상태별 주문 수 테이블 출력 (REJECTED 제외)
    public void showOrderStatus();

    // 시료별 재고 현황 테이블 출력 (재고 수, 재고 상태)
    public void showStockStatus();

    // showOrderStatus + showStockStatus 순서로 통합 출력
    public void show();
}
```

### ReleaseView

```java
public class ReleaseView {

    public ReleaseView(ReleaseController releaseController, Scanner scanner) { ... }

    // CONFIRMED 주문 목록 출력 후 번호 선택 → 출고 처리
    public void showReleaseMenu();
}
```

---

## 6. 화면 설계

### 6-1. 모니터링 화면

```
[모니터링]

[주문 현황]
상태          주문 수
------------------
RESERVED      3
PRODUCING     1
CONFIRMED     2
RELEASE       5

[재고 현황]
시료ID   시료명     현재 재고   재고 상태
------------------------------------------
S001     시료A      50          여유
S002     시료B      0           고갈
S003     시료C      3           부족
```

- 주문이 하나도 없는 상태별 항목은 0으로 표기 (해당 상태 행을 생략하지 않음).
- 등록된 시료가 없으면 재고 현황 테이블 대신 "등록된 시료가 없습니다." 출력.
- 주문이 전혀 없으면 주문 현황 테이블 대신 "접수된 주문이 없습니다." 출력.

### 6-2. 출고 처리 화면

```
[출고 처리]
번호  주문ID(앞 8자리)  시료ID  고객명    수량
--------------------------------------------------
1     a1b2c3d4         S001    홍길동    10
2     e5f6g7h8         S002    김철수    5

선택 (번호, 0: 취소) > 1

출고 완료 (RELEASE) — 주문번호: a1b2c3d4...
```

CONFIRMED 주문 없을 시:
```
[출고 처리]
출고 가능한 주문이 없습니다.
```

---

## 7. 테스트 케이스

### 7-1. MonitoringViewTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `showOrderStatus_주문있음_상태별집계출력` | RESERVED·PRODUCING·CONFIRMED·RELEASE 각 주문을 생성 후 출력에 각 상태와 수량이 포함됨 확인 |
| `showOrderStatus_REJECTED_제외` | REJECTED 주문이 있어도 출력에 REJECTED 행 미포함 확인 |
| `showStockStatus_SUFFICIENT_표기` | 재고 충분 시료의 출력에 "여유" 포함 확인 |
| `showStockStatus_SHORT_표기` | 재고 부족 시료의 출력에 "부족" 포함 확인 |
| `showStockStatus_EMPTY_표기` | 재고 0 시료의 출력에 "고갈" 포함 확인 |

### 7-2. ReleaseViewTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `showReleaseMenu_정상출고_RELEASE전환` | CONFIRMED 주문 선택 후 출고 시 "RELEASE" 출력 확인 |
| `showReleaseMenu_빈목록_안내메시지` | CONFIRMED 주문 없을 시 "출고 가능한 주문이 없습니다." 출력 확인 |
| `showReleaseMenu_취소_0입력` | 0 입력 시 출고 없이 반환 확인 |

### 7-3. ReleaseControllerTest 추가 (버그 수정 검증)

| 테스트 메서드 | 검증 내용 |
|---|---|
| `release_재고부족케이스_재고차감확인` | 생산 완료(재고 += requiredQty) 후 release 시 재고 -= quantity 확인 |

### 7-4. MonitoringControllerTest 수정 (버그 수정 검증)

| 테스트 메서드 | 검증 내용 |
|---|---|
| `getStockStatus_CONFIRMED_RELEASE_주문_제외` | CONFIRMED/RELEASE 주문은 재고 상태 판단 시 주문수량 합산에서 제외됨 확인 |

### 7-5. AppIntegrationTest 추가

| 테스트 메서드 | 검증 내용 |
|---|---|
| `release_후_상태RELEASE확인` | createOrder → approveOrder(재고 충분) → release → 상태 RELEASE 검증 |
| `release_재고부족흐름_재고차감확인` | createOrder → approveOrder(재고 부족) → (생산 완료 대기) → release → 재고 차감 확인 |

---

## 8. 구현 순서

```
[버그 수정 — RED/GREEN]
1. ReleaseControllerTest 추가 (release 재고 차감 RED)
2. MonitoringControllerTest 수정 (CONFIRMED/RELEASE 제외 기준 RED)
3. ReleaseController.release() 재고 차감 로직 추가 (GREEN)
4. MonitoringController.getStockStatus() 주문수량 기준 수정 (GREEN)

[View 신규 구현 — RED/GREEN]
5. MonitoringView 시그니처 + MonitoringViewTest 작성 (RED)
6. ReleaseView 시그니처 + ReleaseViewTest 작성 (RED)
7. AppIntegrationTest 추가 (RED)
8. MonitoringView 구현 (GREEN)
9. ReleaseView 구현 (GREEN)
10. MainView 메뉴 4번(모니터링), 6번(출고 처리) 활성화
11. App.java: MonitoringController, ReleaseController, View 인스턴스 생성 및 연결
12. 수동 실행으로 전체 흐름 확인
```

---

## 9. 검토 포인트

### P1 — 비즈니스 로직 (버그 수정 방향 확인)

| # | 포인트 | 결정 방향 |
|---|---|---|
| 1 | **출고 시 재고 차감** | 재고 차감 시점을 **출고(release) 시점으로 통일**. `approveOrder` 충분 케이스에서 재고 차감 제거 → `release()` 에서 항상 `재고 -= quantity` 차감. SampleRepository를 ReleaseController에 추가 주입. `OrderControllerTest.approveOrder_재고충분_재고가차감된다` 테스트도 "승인 시 재고 불변"으로 수정. |
| 2 | **재고 상태 판단 기준 변경** | `getStockStatus()`에서 RESERVED + PRODUCING 주문만 합산 (CONFIRMED, RELEASE 제외) |

### P2 — UI/UX

| # | 포인트 | 설계 방향 (검토 후 수정 가능) |
|---|---|---|
| 3 | **모니터링 진입 방식** | 메뉴 4번 선택 시 주문 현황 + 재고 현황 통합 출력 후 메인 메뉴로 복귀 (서브메뉴 없이 단순하게) |
| 4 | **모니터링 자동 갱신** | 1회 출력 후 메인 메뉴로 복귀 (갱신 원하면 메뉴 4번 재선택) |
| 5 | **ReleaseView의 출고 후 동작** | 1건 출고 후 남은 CONFIRMED 목록 다시 표시 (연속 출고 지원, 0 입력으로 복귀) |

### P3 — 기술 결정 (자명한 사항, 별도 확인 불필요)

| # | 포인트 | 결정 |
|---|---|---|
| 6 | **MonitoringView의 Scanner** | 출력 전용 — Scanner 불필요. MonitoringController + SampleController만 주입 |
| 7 | **재고 현황 테이블 정렬** | ID 오름차순 (SampleController.listSamples()와 동일) |
