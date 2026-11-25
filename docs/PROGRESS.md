# PROGRESS — 세션 인수인계 문서

> **새 세션은 이 문서부터 읽는다.** 작업이 끝날 때마다 메인 에이전트가 갱신한다.
> 마지막 갱신: 2026-11-25 · 원격 연결, 이슈 기반 작업 흐름 도입

## 1. 현재 상태 요약
- T-001 환경 설정 완료 (verifier PASS). 빈 Spring Boot 4.2.0 프로젝트가 MySQL·Redis와 함께 뜨고 테스트 8개가 통과한다.
- 브랜치: `main`(초기 세팅 커밋), `develop`, `feat/T-001-project-setup`(T-001 커밋). 원격 저장소 없음 → push·develop 병합은 사용자 확인 후.
- 원격 `origin` = GG-COURT/GG-COURT-BE. `main`은 push 완료. `develop`은 토큰에 `workflow` 권한이 없어 push 거부됨(사용자 조치 필요).
- **이후 작업은 GitHub 이슈 단위**: 이슈 선택 → develop 동기화 → 브랜치 → 커밋 → PR → 사용자·팀원 리뷰 후 머지(Claude는 머지 금지). CLAUDE.md "Git" 절.
- 다음 작업은 사용자가 고르는 이슈. 백로그(5절) T-002 공통 기반이 다음 후보.
- **테스트는 사용자가 지시할 때만 실행한다** (CLAUDE.md "테스트 실행 규칙", gg-test 0절). 에이전트는 `./gradlew build -x test`까지만 돌린다.
- Java 24로 전환했다(toolchain·CI·문서). `build -x test`만 확인, 테스트는 Java 24에서 아직 미실행.
- 사용자 확인 필요: Java 24 전환 후 테스트 실행 여부.

## 2. 진행 중 작업
없음

## 3. 다음 작업 — T-002 공통 기반
- 범위(백로그): BaseTimeEntity, ErrorCode/BusinessException/GlobalExceptionHandler, 페이지 응답 형식.
- 응답·에러 형식은 `gg-implement` 스킬 4절 기본값으로 진행하되 프론트와 확인 필요로 결정 로그에 남긴다.
- 명세 작성 전 참고: T-001의 테스트 기반(`TestcontainersConfiguration`, `@ActiveProfiles("test")`, RANDOM_PORT + JDK HttpClient)을 그대로 재사용한다.

## 4. 완료된 작업 (최신이 위)
| 작업 ID | 작업 | 완료일 | 커밋 | 비고 |
|---|---|---|---|---|
| T-001 | 환경 설정 (Boot 4.2.0, compose, 프로필, Flyway V1, Testcontainers, CI) | 2026-11-25 | 22168b0 | 테스트 8개. CI는 원격 저장소가 없어 실제 실행 미확인 |

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
| 2026-11-25 | 테스트는 자동 실행하지 않는다. 사용자가 지시할 때만 실행하고, 그 전에는 권장 명령만 안내 |
| 2026-11-25 | 작업은 GitHub 이슈 단위. 브랜치 `{type}/{이슈번호}-{설명}`, 커밋 subject 끝 `(#이슈번호)`, develop 대상 PR, 머지는 사용자·팀원만 |
| 2026-11-25 | 커밋 메시지는 프론트(GG-COURT-FE)와 같은 gitmoji 형식 `<gitmoji> <type>: <subject>`. `.githooks/commit-msg`가 강제(Gradle compileJava 시 hooksPath 자동 설정), Claude는 커밋 전 `gg-commit` 스킬을 읽는다 |
| 2026-11-25 | Java 21 → **Java 24 (Temurin 24.0.2)**. Spring Boot 4.2.0 유지(Java 24 지원). 비LTS·업데이트 종료 버전임을 알리고 사용자가 선택 |

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
| Git | develop + 기능 브랜치. 커밋 메시지는 gitmoji 컨벤션(확정, 위 표) |
| Java toolchain (2026-11-25) | settings.gradle에 foojay-resolver 1.0.0 (로컬에 JDK 24가 없는 환경에서 자동 다운로드). 이 Mac은 SDKMAN의 Temurin 24.0.2 사용 |
| 로컬 포트 (2026-11-25) | `.env.example` 기본값은 MYSQL_PORT=3306 유지. 이 Mac은 Oracle 설치본 MySQL 8.4.11(LaunchDaemon `com.oracle.oss.mysql.mysqld`)이 3306을 쓰므로 사용자가 `.env`를 MYSQL_PORT=3307로 변경. 앱은 8080 그대로 |
| docker compose 연동 (2026-11-25) | local은 `lifecycle-management: start-only`(앱 종료 시 컨테이너 유지), test는 compose 비활성(Testcontainers 사용) |

### 미정
- 일정, AWS 구성과 예산
- 소셜 로그인 제공자 (카카오 / 구글 / 디스코드 등), 세션 vs JWT
- 코드 포매터 (네이버 핵데이 / Google Java Style), QueryDSL 사용 여부
- 자동 블라인드 신고 기준 N, 랭킹 가중치, 티어 갱신 주기
- 프론트 낙관적 업데이트

## 7. 알려진 이슈·주의사항
- (T-001 확인) springdoc 3.2.0은 Boot 4.2.0에서 정상 동작. Boot 4 artifact 이름: `spring-boot-starter-webmvc`, `-flyway`, `-security-oauth2-client`, `testcontainers-mysql` 등.
- start.spring.io는 `bootVersion=4.2.0`(접미사 없이)으로 요청해야 한다. `4.2.0.RELEASE`는 500.
- `/actuator/health` 응답은 Boot 4에서 `{"groups":["liveness","readiness"],"status":"UP"}` 형태다.
- Spring Boot 4.2.0은 2026-11-19 출시라 라이브러리 호환이 늦을 수 있다. springdoc 3.2.0은 공식적으로 Boot 4.1.2 기준이다.
- Boot 4는 Jackson 3, Hibernate 7 기반이라 Boot 3 예제 코드의 패키지명·설정이 맞지 않을 수 있다.
- Riot Personal 키는 공개 서비스(오픈 베타 포함)에 쓸 수 없다. 개발은 Development 키(24시간마다 재발급), 공개는 Production 키.
- Java 24는 비LTS이고 2025-09 이후 보안 업데이트가 없다(24.0.2가 마지막). AWS 운영 배포 전에 Java 25 LTS로 올릴지 다시 검토한다.

## 8. 세션 로그 (최신이 위)
- **2026-11-25 · 원격 연결**: feat/T-001을 develop에 --no-ff 머지(099c882, 초기 세팅이라 사용자 지시로 직접 머지) / origin 연결, main push 완료, develop은 PAT `workflow` 스코프 없음으로 거부 / CLAUDE.md에 이슈 기반 흐름 반영(브랜치 docs/git-workflow) / 사용자에게: 토큰 권한, gh CLI 설치.
- **2026-11-25 · 커밋 컨벤션**: FE 저장소의 commitlint-config-gitmoji 규칙을 확인해 백엔드에 perl 기반 commit-msg 훅, Gradle 자동 설치 태스크, gg-commit 스킬, CLAUDE.md 규칙 추가 / 훅 샘플 16건 확인 / 이전 커밋 5개도 사용자 요청으로 gitmoji 형식으로 메시지를 다시 씀(filter-branch, 내용 변경 없음, push 전이라 영향 없음).
- **2026-11-25 · 로컬 포트**: 3306 점유 원인은 Mac에 직접 설치된 MySQL 8.4.11 → 사용자가 `.env` MYSQL_PORT=3307로 변경 / compose 재기동, local 프로필 기동 확인(health UP, swagger 200, JDBC 127.0.0.1:3307) / 앱은 종료, compose 컨테이너는 실행 중.
- **2026-11-25 · 설정 변경**: 테스트 자동 실행 금지 규칙을 CLAUDE.md·스킬 3개·에이전트 3개에 반영 / Java 24 전환, `build -x test` 통과(class major 68) / 테스트는 미실행 / 사용자에게: Java 24에서 테스트 실행 권장.
- **2026-11-25 · T-001**: git 초기화(main/develop/feat 브랜치), implementer→tester→verifier 1회 만에 PASS, 커밋 22168b0 / 다음은 T-002 명세 작성 / 사용자에게: JDK 21.0.13 설치, 로컬 MySQL 포트 충돌, push·develop 병합 여부.
- **2026-11-25 · 초기 세팅**: CLAUDE.md, 서브에이전트 3개(implementer·tester·verifier)와 스킬, 이 문서를 만들었다. 다음 세션은 T-001부터 시작한다.

<!-- 세션 로그 작성 형식
- **YYYY-MM-DD · 작업 ID**: 한 일 / 결과(PASS·FAIL·중단) / 다음에 할 일 / 사용자에게 물은 것
-->
