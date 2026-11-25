# GG-Court Backend

롤 클립 과실 판결 커뮤니티 **GG-Court**의 백엔드 저장소.
기획·요구사항은 `docs/DESIGN.md`, 진행 상황은 `docs/PROGRESS.md`가 기준이다.

## 세션 시작 시 반드시 할 것
1. `docs/PROGRESS.md`를 처음부터 끝까지 읽는다.
2. "진행 중 작업"이 있으면 그것부터 이어서 한다. 없으면 "다음 작업"을 시작한다.
3. 사용자가 다른 작업을 지시하면 그 지시를 따르고, PROGRESS.md에 반영한다.

## 작업이 끝날 때마다 (세션 종료 전 포함)
`docs/PROGRESS.md`를 갱신한다. 다음 세션은 이 문서만 보고 바로 이어서 일할 수 있어야 한다.
- 상단 "마지막 갱신" 날짜와 "현재 상태 요약"
- 완료된 작업 표 (작업 ID, 커밋 해시, 비고)
- 진행 중 작업: 어디까지 했고 다음에 무엇을 할지, 막힌 점
- 결정 로그: 이번에 새로 정한 것, 사용자에게 물어야 할 것
- 세션 로그: 3~5줄 요약 (최신이 위)

## 에이전트 구성

| 역할 | 정의 | 모델 | 하는 일 |
|---|---|---|---|
| 메인 | 이 대화 | Opus 5.5 (`claude-opus-5-5`) | 계획, 작업 명세 작성, 서브에이전트 조율, 커밋, PROGRESS.md 갱신 |
| implementer | `.claude/agents/implementer.md` | Sonnet 5.5 | 프로덕션 코드와 설정 구현 |
| tester | `.claude/agents/tester.md` | Sonnet 5.5 | 테스트 코드 작성·실행. 메인 코드는 고치지 않음 |
| verifier | `.claude/agents/verifier.md` | Sonnet 5.5 | 읽기 전용 검증. PASS / FAIL 판정 |

### 작업 흐름
1. 메인이 작업 명세를 쓴다 (아래 형식). 명세는 서브에이전트에게 그대로 전달한다.
2. **implementer** → 구현과 빌드 확인
3. **tester** → 수용 기준별 테스트 작성·실행
4. **verifier** → 수용 기준, 컨벤션, 보안 점검 후 판정
5. FAIL이면 verifier 보고를 implementer(또는 tester)에게 넘겨 수정 → 다시 verifier. **같은 작업이 2회 연속 FAIL이면 멈추고 사용자에게 보고**한다.
6. PASS면 메인이 커밋하고 PROGRESS.md를 갱신한다.

메인은 코드를 직접 대량으로 작성하지 않는다. 한두 줄 수정이나 문서 수정은 메인이 해도 된다.

### 작업 명세 형식
```
[작업 ID] T-00X 작업명
목표: 한 문장
범위: 만들거나 바꿀 것 목록
제외: 이번에 하지 않을 것
수용 기준: 검증 가능한 문장으로 번호 매겨서
참고: 관련 파일, DESIGN.md 절 번호, 결정 로그 항목
```

## 기술 스택 (고정 버전)

| 항목 | 버전 | 비고 |
|---|---|---|
| Java | 21 (Temurin 21.0.13 이상) | Gradle toolchain으로 고정 |
| Spring Boot | 4.2.0 | Spring Framework 7.x 자동 |
| Gradle | 9.9.2 (wrapper) | Groovy DSL |
| MySQL | 8.4.13 (LTS) | 로컬은 Docker |
| Redis | 8.10.1 | 로컬은 Docker |
| springdoc-openapi | 3.2.0 | Boot 4.2 호환은 T-001에서 확인 |

- Lombok, Hibernate, Flyway, Testcontainers, JUnit 등 **Spring Boot가 관리하는 의존성은 버전을 적지 않는다.**
- 위 표의 버전을 바꾸려면 사용자에게 먼저 묻는다.

## 자주 쓰는 명령어
```bash
docker compose up -d                                        # 로컬 MySQL, Redis
./gradlew bootRun --args='--spring.profiles.active=local'   # 로컬 실행
./gradlew test                                              # 테스트 (Docker 필요)
./gradlew build                                             # 빌드 + 테스트
```

## 프로젝트 구조 (기본값)
```
src/main/java/com/ggcourt/
├── GgCourtApplication.java
├── domain/            # 도메인별 패키지: verdict, vote, comment, user, report, notification ...
│   └── {domain}/      # controller / service / repository / entity / dto
└── global/            # config, exception, security, common
src/main/resources/
├── application.yml, application-{local,test,prod}.yml
└── db/migration/      # Flyway: V{번호}__{설명}.sql
```

## 꼭 지킬 규칙
- **비밀정보 커밋 금지.** 키·비밀번호는 환경변수로만 받고 `.env`는 커밋하지 않는다. `.env.example`만 커밋한다.
- **스키마는 Flyway로만 바꾼다.** `ddl-auto`는 local·test에서도 `validate`.
- **미확정 결정은 임의로 확정하지 않는다.** 결정이 필요하면 기본값으로 진행하되 PROGRESS.md 결정 로그에 "기본값 적용, 사용자 확인 필요"로 남기고 사용자에게 알린다. 되돌리기 어려운 결정이면 멈추고 묻는다.
- **설계와 다르게 구현해야 하면** 구현 전에 사용자에게 보고한다.
- 판결 도메인 이름은 `verdict`(`case`는 Java 예약어). DESIGN.md의 `cases` 테이블은 `verdicts`로 읽는다.
- 상세 코드 컨벤션은 `.claude/skills/gg-implement/SKILL.md`, 테스트는 `gg-test`, 검증 기준은 `gg-verify`에 있다.

## Git
- 브랜치: `main`, `develop`, 작업 브랜치 `feat/T-00X-설명`, `fix/...`
- 커밋: `feat: 설명` 형식(Conventional Commits 접두어, 본문 한글). 끝에 작업 ID `(T-00X)`.
- 커밋은 verifier PASS 후 메인이 한다. **push와 PR 생성은 사용자 확인 후.**
