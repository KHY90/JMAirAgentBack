package com.jmair.common.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${spring.cors.allowed-origins}")
	private String allowedOrigins;

	@Value("${spring.cors.allowed-methods}")
	private String allowedMethods;

	@Value("${spring.cors.allowed-headers}")
	private String allowedHeaders;

	// 비밀번호 인코더
	@Bean
	public PasswordEncoder passwordEncoder() {
		// 기본값은 10인데 12로 올려서 보안 강화. 다만 연산 속도 느려짐
		return new BCryptPasswordEncoder(12);
	}

	// SecurityFilterChain을 통한 보안 설정
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors()
			.and()
			.csrf().disable();
		return http.build();
	}

	// 글로벌 CORS 설정
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
		configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
		configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
