package com.jmair.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

import com.jmair.auth.dto.SocialDTO;
import com.jmair.auth.entity.User;
import com.jmair.auth.service.TokenService;
import com.jmair.auth.service.UserService;
import com.jmair.auth.util.JwtUtil;
import com.jmair.common.exeption.TokenExpiredException;
import com.jmair.common.exeption.TokenInvalidException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class NaverAuthController {

	private final UserService userService;
	private final WebClient webClient;
	private final JwtUtil jwtUtil;

	@Value("${spring.naver.client-id}")
	private String naverClientId;

	@Value("${spring.naver.client-secret}")
	private String naverClientSecret;

	@Value("${spring.naver.redirect-uri}")
	private String naverRedirectUri;

	@Value("${spring.naver.uri}")
	private String naverUri;

	@Value("${spring.naver.check-id}")
	private String checkIdUri;

	@GetMapping("/naver/callback")
	public Mono<ResponseEntity<?>> naverCallback(@RequestParam("code") String code,
		@RequestParam("state") String state,
		HttpServletRequest request) {

		// state 검증
		String storedState = null;
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("oauth_state".equals(cookie.getName())) {
					storedState = cookie.getValue();
					break;
				}
			}
		}
		if (storedState == null || !storedState.equals(state)) {
			return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("잘못된 state값 입니다."));
		}

		// 1. 네이버 토큰 발급 URL 구성
		String tokenUrl = naverUri
			+ "?grant_type=authorization_code"
			+ "&client_id=" + naverClientId
			+ "&client_secret=" + naverClientSecret
			+ "&code=" + code
			+ "&state=" + state;

		return webClient.get()
			.uri(tokenUrl)
			.retrieve()
			.bodyToMono(Map.class)
			.flatMap(tokenData -> {
				if (tokenData == null || !tokenData.containsKey("access_token")) {
					return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body("네이버 토큰 발급에 실패했습니다."));
				}
				String accessToken = (String) tokenData.get("access_token");

				// 2. 네이버 프로필 조회
				return webClient.get()
					.uri(checkIdUri)
					.header("Authorization", "Bearer " + accessToken)
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
					.publishOn(Schedulers.boundedElastic())
					.publishOn(Schedulers.boundedElastic())
					.flatMap(profileData -> {
						Object respObj = profileData.get("response");
						if (!(respObj instanceof Map)) {
							return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
								.body("네이버 프로필 정보가 없습니다."));
						}
						@SuppressWarnings("unchecked")
						Map<String, Object> response = (Map<String, Object>) respObj;

						String naverId = (String) response.get("id");
						String name = (String) response.get("name");
						String email = (String) response.get("email");

						SocialDTO socialLoginDTO = new SocialDTO();
						socialLoginDTO.setUserLogin(naverId);
						socialLoginDTO.setUserName(name);
						socialLoginDTO.setUserEmail(email);

						// 사용자 로그인/회원가입 처리 // 블로킹 호출을 별도의 스레드에서 실행
						return Mono.fromCallable(() -> userService.naverLogin(socialLoginDTO))
							.subscribeOn(Schedulers.boundedElastic())
							.flatMap(result -> {
								User user = userService.getUserByLogin(naverId);
								result.put("userGrade", user.getUserGrade());

								String jwtAccessToken = (String) result.get("accessToken");
								String jwtRefreshToken = (String) result.get("refreshToken");

								ResponseCookie accessCookie = ResponseCookie.from("access_token", jwtAccessToken)
									.httpOnly(true)
									.secure(false) // 배포시 true로 변경
									.path("/")
									.maxAge(15 * 60)
									.build();
								ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", jwtRefreshToken)
									.httpOnly(true)
									.secure(false) // 배포시 true로 변경
									.path("/")
									.maxAge(7 * 24 * 60 * 60)
									.build();

								return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
									.header("Set-Cookie", accessCookie.toString())
									.header("Set-Cookie", refreshCookie.toString())
									.header("Location", naverRedirectUri)
									.build());
							});
					});
			})
			.onErrorResume(e ->
				Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("네이버 로그인 처리 중 오류가 발생했습니다."))
			);
	}

}
