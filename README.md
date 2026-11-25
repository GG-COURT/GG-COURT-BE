# GG-Court Backend

롤 클립 과실 판결 커뮤니티 GG-Court의 백엔드. 설계는 `docs/DESIGN.md`, 진행 상황은 `docs/PROGRESS.md`.

## 요구 사항
- Docker (로컬 MySQL, Redis, 테스트용 Testcontainers)
- JDK는 Gradle toolchain이 Java 24를 사용한다(로컬에 없으면 자동으로 받는다) (wrapper 실행용 JDK는 아무 버전이나 가능).

## 로컬 실행
```bash
cp .env.example .env                                        # 값은 필요하면 수정
docker compose up -d                                        # MySQL 8.4.13, Redis 8.10.1
./gradlew bootRun --args='--spring.profiles.active=local'
```
- 헬스체크: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui/index.html

종료: `Ctrl+C` 후 `docker compose down` (데이터까지 지우려면 `-v`).

## 빌드와 테스트
```bash
./gradlew build   # 테스트 포함, Docker 필요
./gradlew test
```

## 프로필
`local`, `test`, `prod`. prod는 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` 환경변수로 접속 정보를 받는다.
