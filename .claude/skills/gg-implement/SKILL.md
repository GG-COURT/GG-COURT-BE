---
name: gg-implement
description: GG-Court 백엔드(Spring Boot 4.2, Java 24) 코드를 구현할 때의 절차와 코드 컨벤션. implementer 서브에이전트가 사용한다.
user-invocable: false
---

# GG-Court 구현 가이드

> 이 문서의 컨벤션 중 "(기본값)" 표시는 팀이 아직 확정하지 않은 항목이다. 그대로 따르되, 바꿔야 할 이유가 보이면 보고서에 적는다.

## 1. 절차
1. 작업 명세, `CLAUDE.md`, `docs/PROGRESS.md`의 결정 로그, 관련 `docs/DESIGN.md` 절을 읽는다.
2. 기존 코드에서 같은 종류의 파일(다른 도메인의 Controller, Service 등)을 찾아 패턴을 맞춘다. 처음 만드는 종류면 이 문서를 따른다.
3. 작은 단위로 구현하고 자주 `./gradlew compileJava`로 확인한다.
4. 끝나면 `./gradlew build -x test`가 성공하는지 확인한다. **테스트는 실행하지 않는다**(사용자가 지시할 때만, `gg-test` 0절). 기존 테스트가 영향을 받을 것 같으면 보고서의 "tester에게"에 적는다.
5. 아래 형식으로 보고한다.

## 2. 패키지와 레이어
- 루트 패키지 `com.ggcourt`. 도메인형 구조: `domain/{도메인}/{controller,service,repository,entity,dto}`, 공통은 `global/{config,exception,security,common}`.
- 도메인 이름: `verdict`(판결), `side`(A/B 입장, verdict 하위), `vote`, `comment`, `user`, `report`, `sanction`, `notification`, `riot`(티어 인증).
- 호출 방향: Controller → Service → Repository. 다른 도메인의 Repository를 직접 쓰지 않고 그 도메인의 Service를 호출한다.
- Controller는 요청 검증(`@Valid`)과 응답 변환만. 비즈니스 로직은 Service나 Entity에.

## 3. 네이밍
- 클래스: `VerdictController`, `VerdictService`, `VerdictRepository`, `VerdictCreateRequest`, `VerdictResponse`, `VerdictSummaryResponse`.
- 조회 메서드: 없으면 예외를 던지는 것은 `get...`, `Optional`을 돌려주는 것은 `find...`.
- 테이블은 snake_case 복수형(`verdicts`, `verdict_sides`), 컬럼은 snake_case.
- 제약·인덱스 이름: `pk_`, `fk_{테이블}_{대상}`, `uk_{테이블}_{컬럼}`, `idx_{테이블}_{컬럼}`.
- Enum 상수는 대문자 스네이크. DB에는 `@Enumerated(EnumType.STRING)`.

## 4. API (기본값)
- 경로: `/api/v1/{복수형-kebab-case}`. 예: `POST /api/v1/verdicts/{verdictId}/votes`.
- JSON 필드는 camelCase. 날짜·시간은 ISO-8601 문자열, 서버 내부와 DB는 UTC.
- 성공 응답은 DTO를 그대로 반환. 목록은 `{ "content": [...], "page": 0, "size": 20, "totalElements": 0, "hasNext": false }`.
- 실패 응답은 공통 형식:
  ```json
  { "code": "VOTE_ALREADY_EXISTS", "message": "이미 투표했습니다.", "errors": [] }
  ```
  `errors`는 필드 검증 실패 시 `[{ "field": "title", "reason": "..." }]`.
- 상태 코드: 400 검증 실패, 401 미인증, 403 권한 없음(미투표자의 결과 조회 포함), 404 없음, 409 중복·상태 충돌.

## 5. 예외
- `global/exception`에 `ErrorCode`(enum: HTTP 상태 + code + 기본 메시지), `BusinessException`(ErrorCode 보유), `GlobalExceptionHandler`(`@RestControllerAdvice`).
- 도메인 에러 코드는 도메인 접두어: `VERDICT_NOT_FOUND`, `VOTE_ALREADY_EXISTS`, `COMMENT_NOT_VOTED`.
- 예외를 삼키지 않는다. 처리 못 하는 예외는 핸들러의 500 처리로 넘긴다.

## 6. Entity
- `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`만 사용. `@Setter`, `@Data`, `@AllArgsConstructor` 금지.
- 생성은 정적 팩토리(`Verdict.create(...)`) 또는 private 생성자 + `@Builder`. 상태 변경은 의도가 드러나는 메서드(`close()`, `blind()`)로.
- 공통 시간 필드는 `BaseTimeEntity`(`createdAt`, `updatedAt`, JPA Auditing) 상속.
- 연관관계는 단방향 `@ManyToOne(fetch = LAZY)` 우선. 양방향·cascade는 필요할 때만, 이유를 주석으로.
- 삭제는 하드 삭제 대신 `status` 변경(`DELETED`, `BLINDED`).
- ID는 `@GeneratedValue(strategy = IDENTITY)`, 타입 `Long`.

## 7. DTO
- Java `record`. 요청 DTO에 Bean Validation(`@NotBlank`, `@Size`, `@NotNull`).
- Entity → DTO 변환은 DTO의 정적 팩토리 `from(entity)`. Entity를 Controller 밖으로 반환하지 않는다.

## 8. 트랜잭션과 JPA
- Service 클래스에 `@Transactional(readOnly = true)`, 쓰기 메서드에만 `@Transactional`.
- `spring.jpa.open-in-view=false`. 지연 로딩은 Service 안에서 끝낸다.
- 목록 조회의 N+1은 fetch join 또는 `@EntityGraph`로 막고, 기본으로 `hibernate.default_batch_fetch_size=100`.
- 카운트 컬럼(`vote_count` 등) 증가는 `UPDATE ... SET x = x + 1` 쿼리로 원자적으로.
- 스키마 변경은 반드시 새 Flyway 파일 `V{n}__{설명}.sql`. 이미 커밋된 마이그레이션 파일은 수정하지 않는다.

## 9. Redis
- 키: `ggcourt:{도메인}:{id}:{세부}`. 예: `ggcourt:verdict:12:votes:HIGH:A`, `ggcourt:ratelimit:comment:{ipHash}`.
- 모든 키에 용도별 TTL을 정한다(영구 키는 이유를 주석으로).

## 10. 보안과 개인정보
- 비밀번호(유동 댓글 포함)는 `PasswordEncoder`(BCrypt)로 해시.
- IP는 원본 저장 금지. `ip_hash`(salt 포함 SHA-256)와 표시용 `ip_prefix`(앞 두 옥텟)만 저장.
- 로그에 비밀번호, 토큰, 원본 IP, 이메일을 남기지 않는다.
- 설정값은 `application-*.yml`에서 `${ENV_NAME}`으로 참조. 실제 값은 `.env`(커밋 금지).

## 11. 로깅
- `@Slf4j`. INFO: 주요 상태 변화(판결 마감, 제재). WARN: 예상 가능한 실패(외부 API 재시도). ERROR: 처리 못 한 예외.
- `System.out.println` 금지.

## 12. 완료 보고 형식
```
## 구현 보고 — T-00X
변경 파일: (경로 목록, 새 파일은 [신규])
구현 내용: 수용 기준 번호별로 무엇을 했는지
빌드: 실행한 명령과 결과 (테스트 제외)
확인 필요: 기본값으로 정한 것, 설계와 다른 점, 미해결 사항 (없으면 "없음")
tester에게: 테스트할 때 알아야 할 점 (테스트 데이터, 주의할 경계 조건)
```
