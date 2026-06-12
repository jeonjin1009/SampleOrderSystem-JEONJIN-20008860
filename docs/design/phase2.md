# Phase 2 설계 문서 — 시료 관리 (TDD Red → Green)

## 목표

시료를 등록·조회·검색할 수 있는 콘솔 UI를 구현한다.
Phase 1에서 만든 `CsvSampleRepository`를 실제로 활용하는 첫 번째 단계다.

---

## 1. 구현 범위

| 구현 항목 | 설명 |
|---|---|
| `SampleController` | 시료 등록 / 전체 조회 / 이름 검색 비즈니스 로직 |
| `SampleView` | 시료 입력 폼, 테이블 출력 콘솔 UI |
| `MainView` | 메인 메뉴 루프, 요약 정보 섹션 |
| `App.java` | 메인 메뉴 루프, 요약 정보 섹션 연동 |
| 중복 ID 검증 | 동일 ID 시료 등록 시 예외 처리 후 재입력 유도 |
| 요약 정보 | 등록 시료 수 / 전체 재고 수 메인 메뉴에 반영 |

---

## 2. 패키지 구조 변경

```
src/main/java/com/ssemi/sampleorder/
├── controller/
│   └── SampleController.java      ← 신규
├── view/
│   ├── SampleView.java            ← 신규
│   └── MainView.java              ← 신규 (메인 메뉴 + 요약 정보)
└── App.java                       ← 수정 (메뉴 루프 통합)
```

---

## 3. 비즈니스 로직 결정사항

| 항목 | 결정 |
|---|---|
| 중복 ID 처리 | `IllegalArgumentException` 발생 → View에서 catch 후 오류 메시지 출력 + 재입력 루프 |
| 초기 재고 | 항상 `0`으로 고정 (사용자 입력 없음) |
| 이름 검색 | 대소문자 무시(`toLowerCase`) + 부분 일치(`contains`) |
| 검색 결과 확인 | 검색 후 결과 출력, 계속 검색할지 여부를 묻는 루프 제공 |
| 목록 정렬 | ID 오름차순 정렬 |
| 미구현 요약 항목 | "추후 구현" 문자열로 표기 |

---

## 4. 클래스 시그니처

### SampleController

```java
public class SampleController {

    public SampleController(SampleRepository sampleRepository) { ... }

    // 시료 등록 — 중복 ID 시 IllegalArgumentException
    public Sample addSample(String id, String name, long avgProductionTimeMs, double yield);

    // 전체 조회 — ID 오름차순 정렬
    public List<Sample> listSamples();

    // 이름 포함 검색 (대소문자 무시)
    public List<Sample> searchByName(String keyword);
}
```

### SampleView

```java
public class SampleView {

    public SampleView(SampleController sampleController, Scanner scanner) { ... }

    // 시료 등록 폼 — 중복 ID 오류 시 오류 메시지 출력 후 재입력 루프
    public void showAddForm();

    // 전체 시료 목록 테이블 출력 (ID 오름차순)
    public void showList();

    // 이름 검색 입력 → 결과 출력 → 계속 검색 여부 확인 루프
    public void showSearchResult();
}
```

### MainView

```java
public class MainView {

    public MainView(SampleController sampleController, Scanner scanner) { ... }

    // 메인 메뉴 루프 실행
    public void run();

    // 요약 정보 출력
    private void printSummary();
}
```

---

## 5. 화면 설계

### 5-1. 메인 메뉴

```
====================================
  S Semi 시료 생산주문관리 시스템
====================================
[요약]
  등록 시료 수 : 3
  전체 재고 수 : 120
  전체 주문 수 : (추후 구현)
  생산 대기 건수: (추후 구현)

[메뉴]
  1. 시료 관리
  2. 시료 주문         (추후 구현)
  3. 주문 승인/거절    (추후 구현)
  4. 모니터링          (추후 구현)
  5. 생산 라인 조회    (추후 구현)
  6. 출고 처리         (추후 구현)
  0. 종료

선택 >
```

### 5-2. 시료 관리 서브메뉴

```
[시료 관리]
  1. 시료 등록
  2. 전체 조회
  3. 이름 검색
  0. 뒤로

선택 >
```

### 5-3. 시료 등록 흐름 (중복 ID 재입력)

```
[시료 등록]
시료 ID     > S001
시료 이름   > 시료A
평균생산(ms)> 1000
수율 (0~1)  > 0.9

등록 완료: S001 / 시료A / 1000ms / 수율 0.9 / 재고 0

-- 중복 ID인 경우 --
시료 ID     > S001
오류: 이미 등록된 시료 ID입니다. 다시 입력해 주세요.
시료 ID     >
```

### 5-4. 이름 검색 흐름 (반복 검색)

```
[이름 검색]
검색어 > 시료

  ID      이름        평균생산시간(ms)  수율   재고
  --------------------------------------------------
  S001    시료A       1000             0.90   50
  S002    시료B       2000             0.85   0

계속 검색하시겠습니까? (y/n) > y
검색어 >
```

### 5-5. 시료 목록 테이블 (ID 오름차순)

```
  ID      이름        평균생산시간(ms)  수율   재고
  --------------------------------------------------
  S001    시료A       1000             0.90   50
  S002    시료B       2000             0.85   0
  S003    시료C       500              0.95   30
```

---

## 6. 테스트 케이스

### 6-1. SampleControllerTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `addSample_정상등록_반환값확인` | 등록된 Sample의 id/name/yield 값이 입력값과 일치, stock은 0 |
| `addSample_중복ID_예외발생` | 동일 ID로 두 번 등록 시 `IllegalArgumentException` |
| `listSamples_ID오름차순정렬` | 등록 순서와 무관하게 ID 오름차순으로 반환 |
| `listSamples_빈목록_빈리스트반환` | 등록된 시료 없으면 빈 List 반환 |
| `searchByName_키워드포함_반환` | 이름에 키워드가 포함된 시료만 반환 |
| `searchByName_없는키워드_빈목록` | 매칭되는 시료 없으면 빈 List 반환 |
| `searchByName_대소문자무시` | "sample"과 "SAMPLE"을 동일하게 매칭 |

### 6-2. AppIntegrationTest

| 테스트 메서드 | 검증 내용 |
|---|---|
| `addSample_후_listSamples_포함확인` | addSample → listSamples 흐름에서 등록한 시료가 목록에 존재 |
| `addSample_후_searchByName_검색됨` | addSample → searchByName 흐름에서 키워드로 검색 가능 |

### 6-3. SampleViewTest (최대한 구현, 불가 시 수동 확인)

| 테스트 메서드 | 검증 내용 | 방식 |
|---|---|---|
| `showAddForm_정상입력_등록성공` | 표준 입력 스텁으로 정상 등록 흐름 시뮬레이션 | `System.setIn` + `ByteArrayInputStream` |
| `showAddForm_중복ID_재입력유도` | 중복 ID 입력 후 재입력 루프 동작 확인 | `System.setIn` 스텁 |
| `showList_테이블출력_ID정렬` | 출력 내용에 ID가 오름차순으로 포함됨 | `System.setOut` 캡처 |
| `showSearchResult_결과포함_출력` | 검색 결과가 출력에 포함됨 | `System.setOut` 캡처 |

---

## 7. 구현 순서

```
1. SampleController 시그니처 + SampleControllerTest 작성 (RED)
2. SampleController 구현 (GREEN)
3. AppIntegrationTest 작성 (RED) → 통과 확인 (GREEN)
4. SampleView 구현 (콘솔 출력, 재입력 루프, 반복 검색)
5. SampleViewTest 구현 (최대한, 불가 시 수동 확인)
6. MainView + App.java 메인 메뉴 루프 연결
7. 요약 정보 (등록 시료 수 / 전체 재고 수) 반영, 미구현 항목 "(추후 구현)" 표기
8. 수동 실행으로 전체 흐름 확인
```
