---
name: gg-test
description: GG-Court 백엔드 테스트를 작성·실행하는 규칙. tester 서브에이전트가 수용 기준별 테스트를 만들 때 사용한다.
user-invocable: false
---

# GG-Court 테스트 가이드

## 1. 절차
1. 작업 명세의 수용 기준과 implementer 보고서를 읽는다.
2. 수용 기준마다 어떤 테스트로 증명할지 먼저 표로 정리한다 (아래 보고 형식의 표).
3. 테스트를 작성하고 `./gradlew test`로 전체를 실행한다.
4. 실패하면 원인을 구분한다.
   - 테스트 코드 문제 → 고친다.
   - 메인 코드 버그 → **고치지 않는다.** 실패하는 테스트를 그대로 두고 보고서에 재현 방법을 적는다.

## 2. 무엇을 어떤 테스트로
| 대상 | 방식 |
|---|---|
| Entity 메서드, 계산 로직 | 순수 단위 테스트 (Spring 없이) |
| Service 비즈니스 규칙 | 단위 테스트 + Mockito, 또는 DB가 핵심이면 통합 테스트 |
| Repository 커스텀 쿼리 | `@DataJpaTest` + Testcontainers MySQL |
| Controller 요청·응답·상태 코드 | `@WebMvcTest` + MockMvc |
| 투표·댓글 권한·제재처럼 여러 층이 얽힌 흐름 | `@SpringBootTest` 통합 테스트 + Testcontainers (MySQL, Redis) |

- 테스트 DB는 H2가 아니라 **Testcontainers MySQL 8.4.13**. Redis도 Testcontainers(`redis:8.10.1`).
- 컨테이너는 공통 추상 클래스나 `@ServiceConnection` 설정 하나로 재사용해 테스트 속도를 지킨다.

## 3. 작성 규칙
- 위치: `src/test/java/` 아래 대상 클래스와 같은 패키지. 이름은 `{대상}Test`, 통합은 `{대상}IntegrationTest`.
- `@DisplayName`에 한글로 "무엇을 하면 어떻게 된다"를 쓴다. 예: `@DisplayName("이미 투표한 유저가 다시 투표하면 VOTE_ALREADY_EXISTS 예외가 난다")`
- 본문은 `// given`, `// when`, `// then` 구분.
- 단언은 AssertJ. 예외는 `assertThatThrownBy(...).isInstanceOf(BusinessException.class)`와 에러 코드까지 확인.
- 정상 흐름 1개 + 실패·경계 조건을 반드시 함께. GG-Court에서 자주 빠지는 경계:
  - 중복 투표 (고닉, 유동 각각)
  - 미투표자의 결과 조회·댓글 작성
  - 마감된 판결에 투표
  - 제재 중인 유저·IP의 작성
  - 유동 댓글 비밀번호 불일치
- 테스트끼리 데이터를 공유하지 않는다. 각 테스트가 필요한 데이터를 직접 만든다(테스트 픽스처 메서드 사용 가능).
- 시간 의존 로직은 `Clock`을 주입받아 고정 시간으로 테스트한다.
- `Thread.sleep` 금지. `@Disabled`로 실패를 숨기지 않는다.

## 4. 보고 형식
```
## 테스트 보고 — T-00X
| 수용 기준 | 테스트 클래스#메서드 | 결과 |
|---|---|---|
| 1. ... | VoteServiceTest#duplicateVote | PASS |

실행: ./gradlew test → (통과 N / 실패 M / 소요 시간)
새로 만든 테스트 파일: (경로 목록)
메인 코드 버그: (재현 테스트 이름, 기대값과 실제값) 없으면 "없음"
커버하지 못한 기준: (이유) 없으면 "없음"
```
