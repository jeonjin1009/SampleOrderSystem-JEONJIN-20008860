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

이 프로젝트는 **TDD(Test-Driven Development)** 방식으로 개발한다.

1. **Red** — 실패하는 테스트를 먼저 작성
2. **Green** — 테스트를 통과하는 최소한의 구현 코드 작성
3. **Refactor** — 중복 제거 및 코드 정리

### 규칙
- 프로덕션 코드 작성 전 반드시 테스트 코드를 먼저 작성한다
- 테스트는 `src/test/java/com/ssemi/sampleorder/` 하위에 프로덕션 패키지 구조와 동일하게 작성한다
- 테스트 클래스명은 `{대상클래스}Test` 형식을 따른다
- 각 테스트는 독립적으로 실행 가능해야 하며 외부 파일에 의존하지 않는다 (임시 디렉터리 사용)
- Phase 1은 전체 테스트 코드 작성으로 진행한다 — 구현보다 테스트 설계가 우선
- **Red 단계 완료 후 반드시 사용자 검토를 받고 Green 단계로 진행한다**
- **Green 단계 완료 후 반드시 사용자 검토를 받고 Refactor 단계로 진행한다**

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
