package com.ggcourt.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 임시 보안 설정.
 * TODO(T-003): 실제 인증·인가 규칙으로 교체한다. 지금은 모든 요청을 허용한다.
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.anyRequest().permitAll())
			.httpBasic(Customizer.withDefaults());
		return http.build();
	}

}
