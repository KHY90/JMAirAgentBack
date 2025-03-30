package com.jmair.auth.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jmair.auth.dto.LoginDTO;
import com.jmair.auth.dto.Tokens;
import com.jmair.auth.dto.UserDTO;
import com.jmair.auth.dto.UserGrade;
import com.jmair.auth.entity.User;
import com.jmair.auth.service.TokenService;
import com.jmair.auth.service.UserService;
import com.jmair.auth.util.JwtUtil;
import com.jmair.common.exeption.ForbiddenException;
import com.jmair.common.exeption.TokenExpiredException;
import com.jmair.common.exeption.TokenInvalidException;
import com.jmair.common.exeption.UnauthorizedException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@Validated
public class UserController {

	private final UserService userService;
	private final TokenService tokenService;
	private final JwtUtil jwtUtil;
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	public UserController(UserService userService, TokenService tokenService, JwtUtil jwtUtil) {
		this.userService = userService;
		this.tokenService = tokenService;
		this.jwtUtil = jwtUtil;
	}

	// 회원가입
	@PostMapping("/join")
	public ResponseEntity<?> join(@Valid @RequestBody UserDTO userDTO) {
		try {
			userService.join(userDTO);
			return ResponseEntity.ok("회원가입 성공");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 중 오류가 발생했습니다.");
		}
	}

	// 로그인- 토큰 httpOnly 쿠키에 저장
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
		try {
			Tokens tokens = userService.login(loginDTO);

			// 액세스 토큰 쿠키 (15분 만료)
			ResponseCookie accessCookie = ResponseCookie.from("access_token", tokens.getAccessToken())
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(15 * 60)
				.build();

			// 리프레시 토큰 쿠키 (7일 만료)
			ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.getRefreshToken())
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(7 * 24 * 60 * 60)
				.build();

			response.addHeader("Set-Cookie", accessCookie.toString());
			response.addHeader("Set-Cookie", refreshCookie.toString());

			// 로그인한 사용자 정보 조회
			User user = userService.getUserByLogin(loginDTO.getUserLogin());

			// JSON 응답 생성
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("message", "로그인 성공");
			responseBody.put("user", Map.of(
				"userLogin", user.getUserLogin(),
				"userName", user.getUserName(),
				"userGrade", user.getUserGrade()
			));

			return ResponseEntity.ok(responseBody);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 중 오류가 발생했습니다.");
		}
	}

	// 로그인 상태 관리용
	@GetMapping("/current")
	public ResponseEntity<?> getCurrentUser(HttpServletRequest request, HttpServletResponse response) {
		String accessToken = null;
		String refreshToken = null;
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("access_token".equals(cookie.getName())) {
					accessToken = cookie.getValue();
				} else if ("refresh_token".equals(cookie.getName())) {
					refreshToken = cookie.getValue();
				}
			}
		}
		if (accessToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 없습니다.");
		}
		try {
			// 액세스 토큰 검증 및 사용자 조회
			User user = tokenService.validateTokenAndGetUser(accessToken);
			Map<String, Object> result = new HashMap<>();
			result.put("user", Map.of("userLogin", user.getUserLogin(), "userName", user.getUserName(), "userGrade", user.getUserGrade()));
			return ResponseEntity.ok(result);
		} catch (TokenExpiredException e) {
			// 액세스 토큰 만료 시 리프레시 토큰 검증
			if (refreshToken == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 없습니다.");
			}
			try {
				// 리프레시 토큰으로 사용자 정보 추출
				String userLogin = jwtUtil.validateAndExtractUserLogin(refreshToken);
				User user = userService.getUserByLogin(userLogin);
				// 새 토큰 발급
				Map<String, String> newTokens = tokenService.refreshToken(user);
				String newAccessToken = newTokens.get("accessToken");
				String newRefreshToken = newTokens.get("refreshToken");

				// 새 토큰을 쿠키에 설정
				ResponseCookie newAccessCookie = ResponseCookie.from("access_token", newAccessToken)
					.httpOnly(true)
					.secure(false) // 배포시 true로 변경
					.path("/")
					.maxAge(15 * 60)
					.build();
				ResponseCookie newRefreshCookie = ResponseCookie.from("refresh_token", newRefreshToken)
					.httpOnly(true)
					.secure(false) // 배포시 true로 변경
					.path("/")
					.maxAge(7 * 24 * 60 * 60)
					.build();
				response.addHeader("Set-Cookie", newAccessCookie.toString());
				response.addHeader("Set-Cookie", newRefreshCookie.toString());

				Map<String, Object> result = new HashMap<>();
				result.put("user", Map.of("userLogin", user.getUserLogin(), "userName", user.getUserName()));
				return ResponseEntity.ok(result);
			} catch (Exception ex) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
			}
		} catch (TokenInvalidException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
		}
	}

	// 관리자용: 전체 회원 목록 조회
	@GetMapping("/all")
	public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
		try {
			List<Map<String, Object>> dtos = userService.getAllUsersForAdmin(request);
			return ResponseEntity.ok(dtos);
		} catch (UnauthorizedException | ForbiddenException e) {
			logger.error("회원 목록 조회 권한 오류", e);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Exception e) {
			logger.error("회원 목록 조회 중 오류가 발생했습니다.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("회원 목록 조회 중 오류가 발생했습니다.");
		}
	}

	// 회원 상세 조회 (관리자, 회원은 자신의 정보만 조회 가능)
	@GetMapping("/{userLogin}")
	public ResponseEntity<?> getUserDetail(@PathVariable String userLogin, HttpServletRequest request) {
		try {
			Map<String, Object> dto = userService.getUserDetail(userLogin, request);
			return ResponseEntity.ok(dto);
		} catch (UnauthorizedException | ForbiddenException e) {
			logger.error("회원 상세 조회 권한 오류", e);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Exception e) {
			logger.error("회원 상세 조회 중 오류가 발생했습니다.", e);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body("회원 정보를 찾을 수 없습니다.");
		}
	}

	// 로그아웃
	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletResponse response) {
		ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
			.httpOnly(true)
			.secure(false) // 배포시 true
			.path("/")
			.maxAge(0)
			.build();
		ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
			.httpOnly(true)
			.secure(false) // 배포시 true
			.path("/")
			.maxAge(0)
			.build();
		ResponseCookie stateCooke = ResponseCookie.from("oauth_state", "")
			.httpOnly(true)
			.secure(false) // 배포시 true
			.path("/")
			.maxAge(0)
			.build();
		ResponseCookie kakaoStateCooke = ResponseCookie.from("kakao_oauth_state", "")
			.httpOnly(true)
			.secure(false) // 배포시 true
			.path("/")
			.maxAge(0)
			.build();

		response.addHeader("Set-Cookie", accessCookie.toString());
		response.addHeader("Set-Cookie", refreshCookie.toString());
		response.addHeader("Set-Cookie", stateCooke.toString());
		response.addHeader("Set-Cookie", kakaoStateCooke.toString());

		return ResponseEntity.ok("로그아웃 성공");
	}

	// 회원탈퇴
	@PutMapping("/delete")
	public ResponseEntity<?> deleteUser(@RequestParam String userLogin) {
		try {
			userService.deleteUser(userLogin);
			return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 중 오류가 발생했습니다.");
		}
	}
}
