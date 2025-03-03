package com.jmair.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import com.jmair.auth.dto.SocialDTO;
import com.jmair.auth.entity.User;
import com.jmair.auth.service.UserService;
import com.jmair.auth.util.JwtUtil;

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

	@GetMapping("/naver/callback")
	public Mono<ResponseEntity<?>> naverCallback(@RequestParam("code") String code,
		@RequestParam("state") String state) {
		// 1. 네이버 토큰 발급 URL 구성
		String tokenUrl = "https://nid.naver.com/oauth2.0/token"
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
					.uri("https://openapi.naver.com/v1/nid/me")
					.header("Authorization", "Bearer " + accessToken)
					.retrieve()
					.bodyToMono(Map.class)
					.flatMap(profileData -> {
						Map<String, Object> response = (Map<String, Object>) profileData.get("response");
						if (response == null) {
							return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
								.body("네이버 프로필 정보가 없습니다."));
						}
						String naverId = (String) response.get("id");
						String name = (String) response.get("name");
						String email = (String) response.get("email");

						SocialDTO socialLoginDTO = new SocialDTO();
						socialLoginDTO.setUserLogin(naverId);
						socialLoginDTO.setUserName(name);
						socialLoginDTO.setUserEmail(email);

						// 사용자 로그인/회원가입 처리
						Map<String, Object> result = userService.naverLogin(socialLoginDTO);
						String jwtAccessToken = (String) result.get("accessToken");
						String jwtRefreshToken = (String) result.get("refreshToken");

						// httpOnly 쿠키 생성
						ResponseCookie accessCookie = ResponseCookie.from("access_token", jwtAccessToken)
							.httpOnly(true)
							.secure(false) // 배포시 true
							.path("/")
							.maxAge(15 * 60)
							.build();
						ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", jwtRefreshToken)
							.httpOnly(true)
							.secure(false) // 배포시 true
							.path("/")
							.maxAge(7 * 24 * 60 * 60)
							.build();

						return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
							.header("Set-Cookie", accessCookie.toString())
							.header("Set-Cookie", refreshCookie.toString())
							.header("Location", naverRedirectUri)
							.build());
					});
			})
			.onErrorResume(e ->
				Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("네이버 로그인 처리 중 오류가 발생했습니다."))
			);
	}

	@GetMapping("/naver/current")
	public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
		String accessToken = null;

		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("access_token".equals(cookie.getName())) {
					accessToken = cookie.getValue();
					break;
				}
			}
		}

		if (accessToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("로그인 정보가 없습니다.");
		}

		try {
			String userLogin = jwtUtil.validateAndExtractUserLogin(accessToken);
			User user = userService.getUserByLogin(userLogin);
			Map<String, Object> result = new HashMap<>();
			result.put("user", Map.of(
				"userLogin", user.getUserLogin(),
				"userName", user.getUserName()
			));
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("유효하지 않은 토큰입니다.");
		}
	}
}
