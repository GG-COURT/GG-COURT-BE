# PROGRESS — 세션 인수인계 문서

> **새 세션은 이 문서부터 읽는다.** 작업이 끝날 때마다 메인 에이전트가 갱신한다.
> 마지막 갱신: 2026-11-25 · 초기 세팅 (작업 시작 전)

## 1. 현재 상태 요약
- 저장소에는 Claude Code 설정(CLAUDE.md, 서브에이전트, 스킬)과 문서만 있다. 코드는 아직 없다.
- 첫 작업은 **T-001 환경 설정**이다.
- 사용자 확인이 필요한 항목: 6절 "미정" 참고. T-001 진행에 막히는 항목은 없다.

## 2. 진행 중 작업
없음

## 3. 다음 작업 — T-001 환경 설정

```
[작업 ID] T-001 환경 설정
목표: 로컬에서 앱이 MySQL·Redis와 함께 뜨고, 빌드·테스트·CI가 도는 빈 프로젝트를 만든다.

범위:
 1. 프로젝트 생성 (start.spring.io 기준)
    - Gradle (Groovy DSL), Java 21, Spring Boot 4.2.0, Jar
    - group: com.ggcourt / artifact: backend / 메인 클래스: GgCourtApplication
    - 의존성: Spring Web, Spring Data JPA, Validation, Spring Security, OAuth2 Client,
      Spring Data Redis, MySQL Driver, Flyway Migration(+ MySQL 지원 모듈), Lombok,
      Spring Boot Actuator, Docker Compose Support, Testcontainers(MySQL)
    - 추가: springdoc-openapi-starter-webmvc-ui 3.2.0
    - 의존성 artifact 이름은 start.spring.io가 만든 것을 그대로 쓴다 (Boot 4에서 바뀐 모듈명 주의)
 2. Gradle wrapper 9.9.2 고정, Java toolchain 21 설정
 3. 로컬 인프라: compose.yaml
    - mysql:8.4.13 (DB명 ggcourt, utf8mb4, 시간대 UTC), redis:8.10.1
    - 접속 정보는 .env에서 읽는다. .env.example 커밋, .env는 .gitignore
 4. 설정 파일
    - application.yml: 공통 (open-in-view false, ddl-auto validate, jdbc time_zone UTC,
      default_batch_fetch_size 100, Flyway 활성)
    - application-local.yml / application-test.yml / application-prod.yml
    - prod는 모든 접속 정보를 ${ENV}로만 참조
 5. Flyway: src/main/resources/db/migration/V1__init.sql (빈 기준점, 주석만)
 6. 패키지 골격: com.ggcourt.domain, com.ggcourt.global.{config,exception,security,common}
 7. 임시 SecurityConfig: /actuator/health, /swagger-ui/**, /v3/api-docs/** 허용,
    나머지도 일단 permitAll (TODO 주석으로 T-003에서 교체한다고 명시)
 8. Actuator: health 엔드포인트만 노출
 9. springdoc: Swagger UI 접속 확인. Boot 4.2에서 동작하지 않으면 멈추고 사용자에게 보고
    (대안: Spring Boot 4.1.x로 내리기)
10. 테스트 기반: Testcontainers(MySQL 8.4.13, Redis 8.10.1)로 컨텍스트 로딩 + DB·Redis 연결 확인 테스트 1개
11. GitHub Actions: .github/workflows/ci.yml — PR과 develop/main push에 ./gradlew build (Temurin 21)
12. .gitignore, .editorconfig (UTF-8, LF, 들여쓰기 4칸), README.md (로컬 실행 방법)

제외: 도메인 코드, 공통 예외·응답 구조(T-002), 실제 인증(T-003), 포매터 도입(결정 대기), AWS 배포

수용 기준:
 1. `./gradlew clean build`가 성공한다 (테스트 포함).
 2. `docker compose up -d` 후 local 프로필로 앱이 기동되고 GET /actuator/health가 {"status":"UP"}를 반환한다.
 3. GET /swagger-ui/index.html이 200을 반환한다.
 4. Testcontainers 테스트가 MySQL과 Redis에 실제로 연결해 통과한다.
 5. Flyway가 기동 시 V1을 적용하고 flyway_schema_history 테이블이 생긴다.
 6. .env가 git 추적 대상이 아니고, 저장소 어디에도 비밀번호·키가 하드코딩되어 있지 않다.
 7. Spring Boot 관리 의존성에 버전이 직접 적혀 있지 않다 (springdoc만 예외).
 8. ci.yml이 존재하고 Java 21로 ./gradlew build를 실행하도록 되어 있다.

참고: CLAUDE.md 기술 스택 표, DESIGN.md 8장
```

## 4. 완료된 작업 (최신이 위)
| 작업 ID | 작업 | 완료일 | 커밋 | 비고 |
|---|---|---|---|---|
| - | - | - | - | - |

## 5. 백로그 (MVP)
| 작업 ID | 작업 | 선행 | 메모 |
|---|---|---|---|
| T-001 | 환경 설정 | - | 3절 |
| T-002 | 공통 기반: BaseTimeEntity, ErrorCode/BusinessException/GlobalExceptionHandler, 페이지 응답 형식 | T-001 | 응답·에러 형식은 프론트와 확인 필요 |
| T-003 | 회원: 소셜 로그인, 고정닉 설정, 로그인 유저 주입 | T-002 | 인증 방식·로그인 제공자 결정 필요 |
| T-004 | 판결(verdict) 등록·조회·목록·삭제, A/B 입장 | T-003 | 영상 링크 허용 도메인 |
| T-005 | 투표와 결과 (고닉·유동, 세그먼트 집계, Redis 카운터) | T-004 | 결과 공개 조건 |
| T-006 | 댓글 (고닉·유동, 2단계, 진영 배지) | T-005 | 유동 비밀번호, IP 해시 |
| T-007 | 신고·제재·IP 차단·작성 속도 제한 | T-006 | 자동 블라인드 기준 N |
| T-008 | 인기 판결 랭킹, 오늘의 판결 | T-005 | 가중치 |
| T-009 | 알림 (사이트 내) | T-006 | |
| T-010 | 관리자 API | T-007 | |
| T-011 | 티어 인증 (Riot API, 아이콘 방식) | T-003 | Development 키로 개발, 공개는 Production 키 승인 후 |
| T-012 | 판결 마감 처리 (스케줄러, 최종 결과 확정) | T-005 | 마감 기간 |

## 6. 결정 로그

### 확정
| 날짜 | 결정 |
|---|---|
| 2026-11-12 | 롤 단일 게임, 반응형 웹, 2인 개발 (백엔드·프론트), 포폴 겸 실제 운영 |
| 2026-11-12 | 유동(비회원)·고닉(회원) 구조. 판결 등록은 고닉만, 투표·댓글은 유동도 가능. 티어 인증은 선택 |
| 2026-11-12 | 투표 변경 불가. 댓글은 투표자만, 진영 배지 표시 |
| 2026-11-12 | 영상은 외부 링크 임베드만. 제재 3단계(경고·일시 정지·영구 정지) |
| 2026-11-12 | 스택: Spring Boot, JPA, Redis, React/Next.js, 소셜 로그인, AWS. 투표는 즉시 집계 반영 |
| 2026-11-25 | Java 21, Spring Boot 4.2.0, Gradle 9.9.2 |
| 2026-11-25 | 에이전트 구성: 메인 Opus 5.5, 서브(implementer·tester·verifier) Sonnet 5.5 |

### 기본값 적용 (사용자 확인 필요)
| 항목 | 현재 기본값 |
|---|---|
| DB | MySQL 8.4.13 (PostgreSQL과 비교 후 추천안) |
| 판결 도메인 이름 | `verdict` (`case`는 Java 예약어) |
| 패키지 루트 | `com.ggcourt`, 도메인형 구조 |
| 결과 공개 시점 | 투표 직후, 마감 후에는 비투표자도 |
| 판결 마감 | 72시간 |
| 대댓글 | 2단계 |
| API 경로·응답·에러 형식 | `gg-implement` 스킬 4절 (프론트와 확인 필요) |
| Git | develop + 기능 브랜치, Conventional Commits |

### 미정
- 일정, AWS 구성과 예산
- 소셜 로그인 제공자 (카카오 / 구글 / 디스코드 등), 세션 vs JWT
- 코드 포매터 (네이버 핵데이 / Google Java Style), QueryDSL 사용 여부
- 자동 블라인드 신고 기준 N, 랭킹 가중치, 티어 갱신 주기
- 프론트 낙관적 업데이트

## 7. 알려진 이슈·주의사항
- Spring Boot 4.2.0은 2026-11-19 출시라 라이브러리 호환이 늦을 수 있다. springdoc 3.2.0은 공식적으로 Boot 4.1.2 기준이다.
- Boot 4는 Jackson 3, Hibernate 7 기반이라 Boot 3 예제 코드의 패키지명·설정이 맞지 않을 수 있다.
- Riot Personal 키는 공개 서비스(오픈 베타 포함)에 쓸 수 없다. 개발은 Development 키(24시간마다 재발급), 공개는 Production 키.
- Java 21은 Temurin 21.0.13 이상. 11월 보안 업데이트가 나오면 올린다.

## 8. 세션 로그 (최신이 위)
- **2026-11-25 · 초기 세팅**: CLAUDE.md, 서브에이전트 3개(implementer·tester·verifier)와 스킬, 이 문서를 만들었다. 다음 세션은 T-001부터 시작한다.

<!-- 세션 로그 작성 형식
- **YYYY-MM-DD · 작업 ID**: 한 일 / 결과(PASS·FAIL·중단) / 다음에 할 일 / 사용자에게 물은 것
-->
