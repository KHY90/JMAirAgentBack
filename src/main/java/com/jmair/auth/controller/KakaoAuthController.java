package com.jmair.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmair.auth.dto.SocialDTO;
import com.jmair.auth.entity.User;
import com.jmair.auth.service.UserService;
import com.jmair.auth.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/kakao")
@RequiredArgsConstructor
public class KakaoAuthController {

	private static final Logger logger = LoggerFactory.getLogger(KakaoAuthController.class);

	private final UserService userService;
	private final WebClient webClient;

	@Value("${spring.kakao.client-id}")
	private String kakaoClientId;

	@Value("${spring.kakao.redirect-uri}")
	private String kakaoRedirectUri;

	@Value("${spring.kakao.redirect}")
	private String RedirectUri;

	@Value("${spring.kakao.token-uri}")
	private String kakaoTokenUri;

	@Value("${spring.kakao.profile-uri}")
	private String kakaoProfileUri;

	@GetMapping("/callback")
	public Mono<ResponseEntity<?>> kakaoCallback(@RequestParam("code") String code,
		@RequestParam("state") String state,
		HttpServletRequest request) {

		// state 검증
		String storedState = null;
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("kakao_oauth_state".equals(cookie.getName())) {
					storedState = cookie.getValue();
					break;
				}
			}
		}
		if (storedState == null || !storedState.equals(state)) {
			return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("잘못된 state값 입니다."));
		}

		// 1. 토큰 발급을 위한 form data 구성
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("grant_type", "authorization_code");
		formData.add("client_id", kakaoClientId);
		formData.add("redirect_uri", kakaoRedirectUri);
		formData.add("code", code);

		// 2. 토큰 발급 요청
		return webClient.post()
			.uri(kakaoTokenUri)
			.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.body(BodyInserters.fromFormData(formData))
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
			})
			.flatMap(tokenData -> {
				if (tokenData == null || !tokenData.containsKey("access_token")) {
					logger.error("카카오 토큰 발급 실패: tokenData={}", tokenData);
					return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body("카카오 토큰 발급에 실패했습니다."));
				}
				String accessToken = (String)tokenData.get("access_token");

				// 3. 카카오 프로필 조회
				return webClient.get()
					.uri(kakaoProfileUri)
					.header("Authorization", "Bearer " + accessToken)
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
					})
					.publishOn(Schedulers.boundedElastic())
					.flatMap(profileData -> {
						Object idObj = profileData.get("id");
						if (idObj == null) {
							logger.error("카카오 프로필 정보가 없습니다: profileData={}", profileData);
							return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
								.body("카카오 프로필 정보가 없습니다."));
						}
						String kakaoId = String.valueOf(idObj);

						// kakao_account 추출
						Object accountObj = profileData.get("kakao_account");
						Map<String, Object> kakaoAccount = null;
						if (accountObj != null) {
							try {
								ObjectMapper mapper = new ObjectMapper();
								kakaoAccount = mapper.convertValue(accountObj,
									new TypeReference<Map<String, Object>>() {
									});
							} catch (IllegalArgumentException e) {
								logger.error("카카오 계정 정보 변환 오류: {}", accountObj, e);
								return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
									.body("카카오 계정 정보 형식이 올바르지 않습니다."));
							}
						} else {
							logger.error("카카오 계정 정보가 없습니다.");
							return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
								.body("카카오 계정 정보가 없습니다."));
						}

						String email = (String)kakaoAccount.get("email");
						String nickname = null;
						Object profileObj = kakaoAccount.get("profile");
						if (profileObj != null) {
							try {
								ObjectMapper mapper = new ObjectMapper();
								Map<String, Object> profile = mapper.convertValue(profileObj,
									new TypeReference<Map<String, Object>>() {
									});
								nickname = (String)profile.get("nickname");
							} catch (IllegalArgumentException e) {
								logger.error("카카오 프로필 정보 변환 오류: {}", profileObj, e);
							}
						}

						SocialDTO socialLoginDTO = new SocialDTO();
						socialLoginDTO.setUserLogin(kakaoId);
						socialLoginDTO.setUserName(nickname);
						socialLoginDTO.setUserEmail(email);

						// 블로킹 호출을 별도의 스레드에서 실행
						return Mono.fromCallable(() -> userService.kakaoLogin(socialLoginDTO))
							.subscribeOn(Schedulers.boundedElastic())
							.flatMap(result -> {
								User user = userService.getUserByLogin(kakaoId);
								result.put("userGrade", user.getUserGrade());

								String jwtAccessToken = (String)result.get("accessToken");
								String jwtRefreshToken = (String)result.get("refreshToken");

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
									.header("Location", RedirectUri)
									.build());
							});
					});
			})
			.onErrorResume(e -> {
				logger.error("카카오 로그인 처리 중 오류 발생", e);
				return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("카카오 로그인 처리 중 오류가 발생했습니다."));
			});
	}
}