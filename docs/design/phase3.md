# Phase 3 설계 문서 — 주문 접수 및 승인/거절

## 목표

주문을 생성하고 승인·거절 처리할 수 있는 콘솔 UI를 구현한다.
`OrderController`는 Phase 1에서 이미 구현 완료되어 있으므로, Phase 3의 핵심은
**`OrderView` UI 구현 + `MainView` 메뉴 2·3번 활성화**다.

---

## 1. Phase 1에서 이미 구현된 항목 (재사용)

| 항목 | 상태 | 비고 |
|---|---|---|
| `OrderController.createOrder()` | ✅ 완료 | 없는 sampleId → IllegalArgumentException |
| `OrderController.approveOrder()` | ✅ 완료 | 재고 충분 → CONFIRMED / 부족 → PRODUCING + 생산큐 등록 |
| `OrderController.rejectOrder()` | ✅ 완료 | REJECTED 전환 |
| `CsvOrderRepository` | ✅ 완료 | CSV CRUD + findByStatus |
| `ProductionLine` | ✅ 완료 | LinkedBlockingQueue + 백그라운드 스레드 |

---

## 2. Phase 3 신규 구현 범위

| 구현 항목 | 설명 |
|---|---|
| `OrderView` | 주문 입력 폼, RESERVED 목록 출력, 승인/거절 처리 UI |
| `MainView` 수정 | 메뉴 2번(시료 주문), 3번(주문 승인/거절) 활성화 |
| `App.java` 수정 | `OrderController`, `ProductionLine` 인스턴스 생성 및 연결 |
| 요약 정보 | 메인 메뉴 "전체 주문 수" 반영 |

---

## 3. 패키지 구조 변경

```
src/main/java/com/ssemi/sampleorder/
├── view/
│   └── OrderView.java     ← 신규
└── App.java               ← 수정 (ProductionLine, OrderController 연결)
```

---

## 4. 비즈니스 로직 결정사항

| 항목 | 결정 |
|---|---|
| 주문 ID 선택 방식 | RESERVED 목록 출력 후 **번호(인덱스)** 입력으로 선택 |
| 승인 결과 안내 | 재고 충분 → "승인 완료 (CONFIRMED)", 재고 부족 → "승인 완료 (생산 중 - PRODUCING)" 출력 |
| 거절 결과 안내 | "거절 완료 (REJECTED)" 출력 |
| 전체 주문 수 집계 | REJECTED 제외한 상태(RESERVED + PRODUCING + CONFIRMED + RELEASE) 합산 |
| 없는 주문 선택 | 범위 초과 번호 → "잘못된 선택입니다." 출력 |
| **주문 목록 표시 범위** | **RESERVED 상태만 표시** (PRD.md 3.4절 기준) |

---

## 5. 클래스 시그니처

### OrderView

```java
public class OrderView {

    public OrderView(OrderController orderController,
                     SampleController sampleController,
                     Scanner scanner) { ... }

    // 시료 주문 접수 폼 — sampleId, 고객명, 수량 입력 (없는 sampleId 재입력 루프)
    public void showCreateForm();

    // RESERVED 주문 목록 출력 — 번호 + 주문ID(앞 8자리) + sampleId + 고객명 + 수량
    public void showReservedList();

    // 승인/거절 처리 — RESERVED 목록 출력 후 번호 선택, 승인/거절 선택
    public void showApproveRejectMenu();
}
```

---

## 6. 화면 설계

### 6-1. 시료 주문 접수

```
[시료 주문]
시료 ID  > S001
고객명   > 홍길동
주문 수량 > 10

주문 접수 완료 (주문번호: a1b2c3d4...) — RESERVED
```

존재하지 않는 시료 ID 입력 시:
```
시료 ID  > NONE
오류: 존재하지 않는 시료 ID입니다. 다시 입력해 주세요.
시료 ID  >
```

### 6-2. 승인/거절 — RESERVED 목록

```
[주문 승인/거절]
번호  주문ID(앞 8자리)  시료ID  고객명    수량
--------------------------------------------------
1     a1b2c3d4         S001    홍길동    10
2     e5f6g7h8         S002    김철수    5

선택 (번호, 0: 취소) > 1
처리 선택 > 1. 승인 / 2. 거절
선택 > 1

승인 완료 (CONFIRMED) — 재고 차감: 10개
```

재고 부족 시:
```
승인 완료 (PRODUCING) — 생산 등록: 13개 / 예상 시간: 13000ms
```

RESERVED 주문 없을 시:
```
[주문 승인/거절]
접수된 주문이 없습니다.
```

---

## 7. 테스트 케이스

### 7-1. OrderViewTest (System.setIn / System.setOut 활용)

| 테스트 메서드 | 검증 내용 |
|---|---|
| `showCreateForm_정상입력_RESERVED등록` | 유효한 sampleId 입력 시 RESERVED 주문 생성 |
| `showCreateForm_없는sampleId_재입력유도` | 없는 sampleId 입력 후 오류 메시지 출력 + 재입력 루프 |
| `showReservedList_목록출력` | RESERVED 주문 목록이 번호와 함께 출력됨 확인 |
| `showReservedList_빈목록_안내메시지` | RESERVED 주문 없을 시 "접수된 주문이 없습니다." 출력 |
| `showApproveRejectMenu_승인_재고충분_CONFIRMED` | 번호 선택 후 승인 시 CONFIRMED 전환 출력 |
| `showApproveRejectMenu_승인_재고부족_PRODUCING` | 번호 선택 후 승인 시 PRODUCING 전환 출력 |
| `showApproveRejectMenu_거절_REJECTED` | 번호 선택 후 거절 시 REJECTED 전환 출력 |

### 7-2. AppIntegrationTest 추가

| 테스트 메서드 | 검증 내용 |
|---|---|
| `createOrder_후_approveOrder_CONFIRMED` | createOrder → approveOrder(재고 충분) 흐름 통합 검증 |
| `createOrder_후_approveOrder_PRODUCING` | createOrder → approveOrder(재고 부족) 흐름 통합 검증 |

---

## 8. 구현 순서

```
1. OrderView 시그니처 + OrderViewTest 작성 (RED)
2. OrderView 구현 (GREEN)
3. AppIntegrationTest 추가 (RED → GREEN)
4. MainView 메뉴 2번(시료 주문), 3번(주문 승인/거절) 활성화
5. App.java: ProductionLine, OrderController, OrderView 인스턴스 생성 및 연결
6. MainView 요약 정보: 전체 주문 수(REJECTED 제외) 반영
7. 수동 실행으로 전체 흐름 확인
```

---

## 9. 검토 포인트

### P1 — 비즈니스 로직 (반드시 결정 필요)

| # | 포인트 | 선택지 |
|---|---|---|
| 1 | **주문 선택 방식** | 번호(인덱스) 선택 vs 주문 ID 직접 입력 — 번호 선택이 편리하나 동시성 상황에서 인덱스가 바뀔 수 있음 |
| 2 | **전체 주문 수 집계 기준** | ✅ **결정 완료: REJECTED 제외 집계** (RESERVED + PRODUCING + CONFIRMED + RELEASE) |
| 3 | **존재하지 않는 sampleId 주문 입력 시** | 오류 후 재입력 루프(현재 설계) vs 오류 출력 후 메뉴로 복귀 |

### P2 — UI/UX (방향 결정)

| # | 포인트 | 선택지 |
|---|---|---|
| 4 | **승인 결과 상세 표시** | "CONFIRMED" 단순 출력 vs "재고 차감: N개" / "생산 등록: N개, 예상 시간: Nms" 상세 출력 |
| 5 | **주문 목록 표시 범위** | RESERVED 상태만 표시 (PRD.md 3.4절 기준) |
| 6 | **주문 ID 표시 방식** | UUID 전체(36자) vs 앞 8자리만 축약 표시 |

### P3 — App.java 연결 구조 (기술 결정)

| # | 포인트 | 선택지 |
|---|---|---|
| 7 | **ProductionLine 초기화 위치** | `App.java`에서 단일 인스턴스로 생성해 OrderController에 주입 vs 별도 팩토리 메서드 |
| 8 | **CSV 파일 경로** | 현재 `"samples.csv"`, `"orders.csv"` (작업 디렉터리 기준) 유지 여부 |
