package com.ggcourt;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GgCourtApplicationIntegrationTest {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	StringRedisTemplate redisTemplate;

	@Autowired
	Environment environment;

	private HttpResponse<String> get(String path) throws Exception {
		int port = Integer.parseInt(environment.getProperty("local.server.port"));
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
		return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	}

	@Test
	@DisplayName("AC4: Testcontainers MySQL에 연결하면 8.4.13 버전이 조회된다")
	void mysqlConnection() {
		// when
		String version = jdbcTemplate.queryForObject("select version()", String.class);

		// then
		assertThat(version).startsWith("8.4.13");
	}

	@Test
	@DisplayName("AC4: Testcontainers Redis에 값을 저장하면 같은 값을 읽어온다")
	void redisRoundTrip() {
		// given
		String key = "t001:roundtrip";

		// when
		redisTemplate.opsForValue().set(key, "pong");
		String value = redisTemplate.opsForValue().get(key);

		// then
		assertThat(value).isEqualTo("pong");
		redisTemplate.delete(key);
	}

	@Test
	@DisplayName("AC5: 기동 후 flyway_schema_history에 V1이 성공으로 기록돼 있다")
	void flywayAppliedV1() {
		// when
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
			"select version, success from flyway_schema_history where version = '1' and success = 1");

		// then
		assertThat(rows).hasSize(1);
	}

	@Test
	@DisplayName("AC2: GET /actuator/health는 200과 status UP을 반환한다")
	void healthIsUp() throws Exception {
		// when
		HttpResponse<String> response = get("/actuator/health");

		// then
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"status\":\"UP\"");
	}

	@Test
	@DisplayName("AC3: GET /swagger-ui/index.html은 200을 반환한다")
	void swaggerUiAvailable() throws Exception {
		// when
		HttpResponse<String> response = get("/swagger-ui/index.html");

		// then
		assertThat(response.statusCode()).isEqualTo(200);
	}

	@Test
	@DisplayName("AC3: GET /v3/api-docs는 200을 반환한다")
	void apiDocsAvailable() throws Exception {
		// when
		HttpResponse<String> response = get("/v3/api-docs");

		// then
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("openapi");
	}

	@Test
	@DisplayName("범위 8: health 외 actuator 엔드포인트(env, beans)는 노출되지 않아 404다")
	void nonHealthActuatorNotExposed() throws Exception {
		// when / then
		assertThat(get("/actuator/env").statusCode()).isEqualTo(404);
		assertThat(get("/actuator/beans").statusCode()).isEqualTo(404);
	}

}
