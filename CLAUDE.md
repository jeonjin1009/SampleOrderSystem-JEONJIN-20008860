# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# 빌드
./gradlew build          # 전체 빌드
./gradlew clean build    # 클린 빌드

# 테스트
./gradlew test           # 전체 테스트 실행
./gradlew test --tests "com.example.SomeTest"          # 단일 테스트 클래스 실행
./gradlew test --tests "com.example.SomeTest.testName" # 단일 테스트 메서드 실행

# 기타
./gradlew clean          # 빌드 아티팩트 정리
./gradlew tasks          # 사용 가능한 모든 태스크 목록
```

Windows에서는 `./gradlew` 대신 `gradlew.bat` 사용.

## Tech Stack

- **Language**: Java 17
- **Build**: Gradle 9.3.0 (Kotlin DSL — `build.gradle.kts`)
- **Testing**: JUnit 5 (Jupiter)
- **Group / Artifact**: `org.example` / `SampleOrderSystem`

## Development Approach: TDD

이 프로젝트는 `/test-driven-development` 스킬을 기반으로 **Human-in-the-Loop RED-GREEN-REVIEW** 사이클로 개발한다.

### 사이클 흐름

```
RED PLAN 작성 (docs/design/phase*.md)
    → 🙋 사용자 검토 및 승인
    → RED (실패하는 테스트 작성 + 실패 확인)
    → 🙋 사용자 검토 및 승인
    → GREEN (최소 구현 + 통과 확인)
    → REVIEW (코드 검토 + 정리)
    → 🙋 사용자 검토 및 승인
    → 다음 사이클
```

### 규칙
- 프로덕션 코드 작성 전 반드시 테스트 코드를 먼저 작성한다
- 테스트는 `src/test/java/com/ssemi/sampleorder/` 하위에 프로덕션 패키지 구조와 동일하게 작성한다
- 테스트 클래스명은 `{대상클래스}Test` 형식을 따른다
- 각 테스트는 독립적으로 실행 가능해야 하며 외부 파일에 의존하지 않는다 (임시 디렉터리 사용)
- **RED PLAN 완료 후 반드시 사용자 검토를 받고 테스트 작성을 시작한다**
- **RED 완료 후 반드시 사용자 검토를 받고 GREEN 단계로 진행한다**
- **REVIEW 완료 후 반드시 사용자 검토를 받고 다음 사이클을 시작한다**

## Development Workflow: SubAgent 파이프라인

각 Phase 구현 시 아래 SubAgent를 순서대로 활용한다.

### Phase 시작 전
```
doc-consistency-verifier
└── PRD.md, PLAN.md, docs/design/phase*.md 간 충돌/중복 검증
    문제 발견 시 문서 수정 후 재검증, PASS 확인 후 구현 시작
```

### RED PLAN → RED (사용자 검토 후)
```
ai-action-implementer
└── docs/design/phase*.md 를 입력으로 받아
    - 도메인 모델/인터페이스 스켈레톤 작성
    - 테스트 코드 작성
    - 모든 테스트가 실패(Red) 상태인지 확인
```

### GREEN (사용자 검토 후)
```
ai-action-implementer
└── 실패하는 테스트를 통과시키는 최소 구현 코드 작성
```

### GREEN 완료 후 병렬 검증
```
compliance-verifier          test-verifier
(PLAN 요구사항 충족 검증)     (테스트 실행 및 결과 검증)
        └──────────────────────────┘
              둘 다 PASS → REVIEW → 사용자 검토
```

### SubAgent별 역할 요약

| SubAgent | 실행 시점 | 역할 |
|---|---|---|
| `doc-consistency-verifier` | Phase 시작 전 | 문서 간 충돌/중복 검증 |
| `ai-action-implementer` | RED / GREEN 단계 | 스켈레톤·테스트·구현 코드 작성 |
| `compliance-verifier` | GREEN 완료 후 | PLAN 요구사항 충족 여부 검증 |
| `test-verifier` | GREEN 완료 후 | 테스트 실행 및 결과 검증 |

## Design Documents

Phase별 상세 설계 문서는 `docs/design/` 에 위치한다.
새로운 `phase*.md` 가 추가될 때마다 아래 목록에 반영한다.

| Phase | 문서 | 내용 |
|-------|------|------|
| Phase 1 | [docs/design/phase1.md](docs/design/phase1.md) | 테스트 코드 작성 (TDD Red 단계) — 도메인 모델 시그니처, Repository 인터페이스, 테스트 케이스 목록 |

## Architecture

현재 소스 디렉터리(`src/main/java`, `src/test/java`)는 비어 있는 스켈레톤 프로젝트다. PRD.md에 정의된 요구사항을 바탕으로 구현을 시작한다.

표준 Gradle 레이아웃:
```
src/
  main/java/      ← 프로덕션 코드
  main/resources/ ← 설정 파일 (application.properties 등)
  test/java/      ← 테스트 코드
  test/resources/ ← 테스트용 설정
```
