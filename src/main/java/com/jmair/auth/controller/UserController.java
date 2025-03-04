package com.jmair.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jmair.auth.dto.LoginDTO;
import com.jmair.auth.dto.Tokens;
import com.jmair.auth.dto.UserDTO;
import com.jmair.auth.entity.User;
import com.jmair.auth.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@Validated
public class UserController {

	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
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
				"userName", user.getUserName()
			));

			return ResponseEntity.ok(responseBody);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 중 오류가 발생했습니다.");
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
