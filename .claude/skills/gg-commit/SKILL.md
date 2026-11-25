---
name: gg-commit
description: GG-Court 커밋 메시지 컨벤션(gitmoji + type). git commit을 실행하거나 커밋 메시지를 작성·제안할 때마다 반드시 먼저 읽는다. 프론트(GG-COURT-FE)의 commitlint-config-gitmoji와 같은 규칙이며, 어기면 commit-msg 훅이 커밋을 거부한다.
---

# GG-Court 커밋 메시지 컨벤션

프론트엔드 저장소(GG-COURT-FE)와 같은 규칙이다. 프론트는 husky + commitlint(`commitlint-config-gitmoji`), 백엔드는 `.githooks/commit-msg`가 검사한다. **형식이 틀리면 커밋이 만들어지지 않는다.**

## 1. 형식
```
<gitmoji> <type>[(scope)]: <subject>

[본문 (선택)]

[꼬리말 (선택)]
```
- `<gitmoji>`: gitmoji 공식 목록의 이모지(✨) 또는 코드(`:sparkles:`). 이모지 뒤에 공백 **한 칸**.
- `<type>`: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`, `build`, `ci`, `perf`, `revert`, `wip` 중 하나. 소문자.
- `(scope)`: 선택. 소문자. 예: `(vote)`, `(verdict)`.
- `:` 뒤에 공백 한 칸, 그다음 `<subject>`.
- `<subject>`: 한글, 무엇을 했는지 짧게. **끝에 마침표 없음.**
- 헤더(첫 줄)는 **100자 이하**.
- 본문이 있으면 헤더 다음 줄은 **빈 줄**.

## 2. 이 저장소의 추가 규칙
- 이슈 작업의 커밋은 subject 끝에 이슈 번호: `✨ feat: 판결 등록 API 추가 (#12)`. 이슈가 없는 작업은 생략.
- 본문은 한글. 무엇을 왜 바꿨는지 목록으로 적는다.
- Claude가 만든 커밋은 맨 끝에 `Co-Authored-By:` 꼬리말을 붙인다(본문과 빈 줄로 구분).
- `--no-verify`로 훅을 건너뛰지 않는다. 거부되면 메시지를 고쳐 다시 커밋한다.
- merge·revert 커밋(`Merge branch ...`, `Revert "..."`)은 git 기본 메시지를 그대로 써도 된다(검사 제외).

## 3. type별 gitmoji (권장 조합)
| type | gitmoji | 언제 |
|---|---|---|
| feat | ✨ `:sparkles:` | 새 기능 |
| feat | 🎉 `:tada:` | 프로젝트 시작, 첫 커밋 |
| fix | 🐛 `:bug:` | 버그 수정 |
| fix | 🚑️ `:ambulance:` | 긴급 수정 |
| fix | 🔒️ `:lock:` | 보안 문제 수정 |
| refactor | ♻️ `:recycle:` | 동작 변화 없는 구조 개선 |
| refactor | 🚚 `:truck:` | 파일·패키지 이동, 이름 변경 |
| chore | 🔧 `:wrench:` | 설정 파일 추가·수정 |
| chore | 🔨 `:hammer:` | 개발 스크립트 |
| chore | 🙈 `:see_no_evil:` | .gitignore |
| docs | 📝 `:memo:` | 문서 |
| test | ✅ `:white_check_mark:` | 테스트 추가·수정 |
| style | 🎨 `:art:` | 포맷, 코드 구조·스타일 |
| build | 📦️ `:package:` | 빌드 설정, 의존성 묶음 |
| build | ➕ `:heavy_plus_sign:` / ➖ `:heavy_minus_sign:` | 의존성 추가 / 제거 |
| build | ⬆️ `:arrow_up:` / ⬇️ `:arrow_down:` | 의존성 버전 올림 / 내림 |
| ci | 👷 `:construction_worker:` | CI 설정 |
| ci | 💚 `:green_heart:` | CI 실패 수정 |
| perf | ⚡️ `:zap:` | 성능 개선 |
| revert | ⏪️ `:rewind:` | 되돌리기 |
| wip | 🚧 `:construction:` | 작업 중 |
| (DB) | 🗃️ `:card_file_box:` | Flyway 마이그레이션 등 DB 변경 (type은 feat/fix/refactor 중 내용에 맞게) |

위 표에 없는 gitmoji도 공식 목록(https://gitmoji.dev)에 있으면 허용된다. 같은 type에는 가능하면 위 조합을 쓴다.

## 4. 예시
```
✨ feat: 판결 등록 API 추가 (#12)
🐛 fix(vote): 유동 중복 투표 검증 누락 수정 (#15)
🗃️ feat: 투표 테이블 마이그레이션 추가 (#15)
🔧 chore: 로컬 MySQL 포트 설정 변경
📝 docs: README 로컬 실행 방법 보완 (#3)
✅ test: 투표 마감 경계 조건 테스트 추가 (#20)
👷 ci: PR에서 커밋 메시지 검사 추가
```
틀린 예: `feat: 설명`(gitmoji 없음), `✨ feature: ...`(허용 안 된 type), `✨ feat:설명`(콜론 뒤 공백 없음), `✨ feat: 설명.`(마침표).

## 5. 커밋 실행 방법 (Claude)
이모지와 여러 줄 본문이 깨지지 않게 heredoc으로 넘긴다.
```bash
git commit -F - <<'EOF'
✨ feat: 판결 등록 API 추가 (#12)

- 판결 등록·조회 엔드포인트
- 제목·영상 링크 검증

Co-Authored-By: ...
EOF
```
커밋 전 체크: gitmoji와 type이 표와 맞는가 → 헤더 100자 이하 → 마침표 없음 → 이슈 번호 → 꼬리말.

## 6. 훅 설치
- `./gradlew` 빌드(compileJava) 시 `git config core.hooksPath .githooks`가 자동 실행된다.
- 직접 설치: `git config core.hooksPath .githooks`
- 훅 단독 확인: `printf '✨ feat: 테스트' > /tmp/m && .githooks/commit-msg /tmp/m`
