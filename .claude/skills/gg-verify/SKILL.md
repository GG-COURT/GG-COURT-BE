---
name: gg-verify
description: GG-Court 백엔드 작업 결과를 커밋 전에 검증하는 체크리스트와 판정 기준. verifier 서브에이전트가 PASS/FAIL을 낼 때 사용한다.
user-invocable: false
---

# GG-Court 검증 가이드

## 1. 절차
1. 작업 명세, implementer 보고서, tester 보고서를 읽는다.
2. `git status`, `git diff`로 실제 변경 파일을 확인한다. 보고서에 없는 변경이 있으면 기록한다.
3. 직접 실행한다. 보고서의 실행 결과를 그대로 믿지 않는다.
   ```bash
   ./gradlew clean build -x test
   ./gradlew compileTestJava
   ```
   **테스트는 실행하지 않는다.** 작업 명세에 `테스트 실행: 허용`이 있을 때만 `./gradlew clean build`로 테스트까지 돌린다(`gg-test` 0절).
   작업 명세에 실행 확인 항목(예: 앱 기동, health 체크)이 있으면 그것은 직접 확인한다.
4. 아래 체크리스트를 순서대로 본다.
5. 판정하고 보고한다.

## 2. 체크리스트

### A. 수용 기준 (하나라도 실패하면 FAIL)
- 수용 기준마다 증거가 있는가: 테스트 이름(미실행이면 그 테스트의 단언 위치), 명령 출력, 파일·줄 위치.
- 테스트가 실제로 그 기준을 확인하는가 (단언 없이 실행만 하는 테스트, 너무 약한 단언은 증거로 인정하지 않는다).

### B. 빌드와 테스트 (실패하면 FAIL)
- `./gradlew clean build -x test`와 `./gradlew compileTestJava` 성공. 테스트 실행이 허용된 작업이면 `./gradlew clean build` 성공.
- 건너뛴 테스트(`@Disabled`)가 없다. 테스트를 실행한 경우 실패 테스트가 없다. 있으면 tester 보고서에 이유가 적혀 있어야 하고, 그래도 FAIL.

### C. 범위 (위반 시 FAIL)
- 작업 명세의 "제외" 항목을 건드리지 않았다.
- 명세와 무관한 파일 변경, 불필요한 리팩터링이 없다.
- tester가 `src/main/`을 고치지 않았다.

### D. 보안과 개인정보 (위반 시 FAIL)
- 비밀정보(키, 비밀번호, 토큰)가 코드·yml·테스트에 하드코딩되지 않았다. `.env`가 git에 포함되지 않았다.
- IP 원본을 저장하거나 로그로 남기지 않는다. 비밀번호는 해시 저장.
- 인증·권한이 필요한 API가 열려 있지 않다 (임시 허용이면 명세에 적혀 있어야 함).

### E. 데이터베이스 (위반 시 FAIL)
- 스키마 변경이 새 Flyway 파일로만 이루어졌다. 기존 마이그레이션 파일을 수정하지 않았다.
- `ddl-auto`가 `validate`이다.

### F. 컨벤션 (심각하지 않으면 PASS + 권고)
- `gg-implement` 스킬의 패키지, 네이밍, Entity, DTO, 예외, 트랜잭션 규칙.
- Spring Boot가 관리하는 의존성에 버전을 직접 적지 않았다.
- 설계(`docs/DESIGN.md`)와 결정 로그에 어긋나는 부분.

## 3. 판정
- **PASS**: A~E 모두 통과. F는 권고 사항으로만 남긴다. 테스트를 실행하지 않았으면 판정을 `PASS (테스트 미실행)`으로 적는다.
- **FAIL**: A~E 중 하나라도 위반.

## 4. 보고 형식
```
## 검증 보고 — T-00X
판정: PASS | PASS (테스트 미실행) | FAIL

실행: ./gradlew clean build -x test → (결과 요약), 테스트: 미실행 | 실행 결과
사용자에게 권장할 테스트 명령: (tester 보고서의 권장 명령을 검토해 그대로 또는 고쳐서)

| 수용 기준 | 판정 | 증거 |
|---|---|---|
| 1. ... | OK | VoteServiceTest#duplicateVote 통과 |

체크리스트: A OK / B OK / C OK / D OK / E OK / F 권고 2건

FAIL 사유와 수정 요청 (FAIL일 때):
- [implementer] 파일:줄 — 무엇을 어떻게
- [tester] ...

권고 (선택):
- ...

메인에게: PROGRESS.md 결정 로그에 남길 것, 사용자 확인이 필요한 것
```
