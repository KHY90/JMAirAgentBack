package com.jmair.common.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jmair.auth.service.UserService;
import com.jmair.auth.util.JwtAuthenticationFilter;
import com.jmair.auth.util.JwtUtil;

@Configuration
@EnableWebSecurity
public class CorsAndSecurityConfig {

	@Value("${spring.cors.allowed-origins}")
	private String allowedOrigins;

	@Value("${spring.cors.allowed-methods}")
	private String allowedMethods;

	@Value("${spring.cors.allowed-headers}")
	private String allowedHeaders;

	private final JwtUtil jwtUtil;
	private final UserService userService;

	public CorsAndSecurityConfig(@Lazy JwtUtil jwtUtil, @Lazy UserService userService) {
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.csrf().disable()
			// JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 등록
			.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userService),
				UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(authz -> authz
				.requestMatchers(HttpMethod.GET, "/api/v1/notices/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/install/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/install/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/used/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/used/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/service/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/service/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/clean/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/clean/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/as/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/as/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/user/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/user/**").permitAll()
				// 인증 관련 URL은 모두 허용
				.requestMatchers("/api/v1/auth/**").permitAll()
				.anyRequest().authenticated()
			);
		return http.build();
	}

	// SecurityFilterChain에 사용할 CORS 설정 빈
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

	// WebMvcConfigurer를 통한 글로벌 CORS 설정
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
					.allowedOrigins(allowedOrigins.split(","))
					.allowedMethods(allowedMethods.split(","))
					.allowedHeaders(allowedHeaders.split(","))
					.allowCredentials(true);
			}
		};
	}
}