package com.jmair.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {

	@Value("${spring.cors.allowed-origins}")
	private String allowedOrigins;

	@Value("${spring.cors.allowed-methods}")
	private String allowedMethods;

	@Value("${spring.cors.allowed-headers}")
	private String allowedHeaders;

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
					.allowedOrigins(allowedOrigins.split(","))
					.allowedMethods(allowedMethods.split(","))
					.allowedHeaders(allowedHeaders.split(","));
			}
		};
	}
}
