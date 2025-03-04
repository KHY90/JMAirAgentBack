package com.jmair.auth.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jmair.auth.dto.LoginDTO;
import com.jmair.auth.dto.SocialDTO;
import com.jmair.auth.dto.Tokens;
import com.jmair.auth.dto.UserDTO;
import com.jmair.auth.dto.UserGrade;
import com.jmair.auth.entity.User;
import com.jmair.auth.repository.UserRepository;
import com.jmair.auth.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	// 회원가입
	@Transactional
	public void join(UserDTO userDTO) {
		// 유저 ID가 이미 존재하는지 체크
		if (userRepository.existsByUserLogin(userDTO.getUserLogin())) {
			throw new IllegalArgumentException("이미 존재하는 회원입니다.");
		}
		// 비밀번호를 BCrypt 해싱하여 저장
		String encodedPassword = passwordEncoder.encode(userDTO.getPassword());

		User user = new User();
		user.setUserLogin(userDTO.getUserLogin());
		user.setUserName(userDTO.getUserName());
		user.setPassword(encodedPassword);
		user.setPhoneNumber(userDTO.getPhoneNumber());
		user.setEmail(userDTO.getEmail());
		user.setUserGrade(UserGrade.USER);
		user.setStatus(true);

		userRepository.save(user);
	}

	// 로그인 - JWT 토큰 반환
	@Transactional
	public Tokens login(LoginDTO loginDTO) {
		User user = userRepository.findByUserLogin(loginDTO.getUserLogin())
			.orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

		if (!user.isStatus()) {
			throw new IllegalArgumentException("탈퇴한 회원입니다. 로그인할 수 없습니다.");
		}

		if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
		}

		String accessToken = jwtUtil.generateAccessToken(user);
		String refreshToken = jwtUtil.generateRefreshToken(user);

		return new Tokens(accessToken, refreshToken);
	}

	// 네이버 로그인
	@Transactional
	public Map<String, Object> naverLogin(SocialDTO socialDTO) {
		String userLogin = socialDTO.getUserLogin();
		String name = socialDTO.getUserName();
		String email = socialDTO.getUserEmail();

		User user = userRepository.findByUserLogin(userLogin).orElse(null);

		if (user == null) {
			user = new User();
			user.setUserLogin(userLogin);
			user.setUserName(name);
			user.setEmail(email);
			String dummyPassword = UUID.randomUUID().toString();
			user.setPassword(passwordEncoder.encode(dummyPassword));
			user.setUserGrade(UserGrade.USER);
			user.setStatus(true);
			user = userRepository.save(user);
		}

		String accessToken = jwtUtil.generateAccessToken(user);
		String refreshToken = jwtUtil.generateRefreshToken(user);

		Map<String, Object> result = new HashMap<>();
		result.put("message", "네이버 로그인 성공");
		result.put("accessToken", accessToken);
		result.put("refreshToken", refreshToken);
		result.put("user", Map.of(
			"userLogin", user.getUserLogin(),
			"userName", user.getUserName()
		));
		return result;
	}

	// 카카오 로그인
	@Transactional
	public Map<String, Object> kakaoLogin(SocialDTO socialDTO) {
		String userLogin = socialDTO.getUserLogin();
		String name = socialDTO.getUserName();
		String email = socialDTO.getUserEmail();

		User user = userRepository.findByUserLogin(userLogin).orElse(null);

		if (user == null) {
			user = new User();
			user.setUserLogin(userLogin);
			user.setUserName(name);
			user.setEmail(email);
			String dummyPassword = UUID.randomUUID().toString();
			user.setPassword(passwordEncoder.encode(dummyPassword));
			user.setUserGrade(UserGrade.USER);
			user.setStatus(true);
			user = userRepository.save(user);
		}

		String accessToken = jwtUtil.generateAccessToken(user);
		String refreshToken = jwtUtil.generateRefreshToken(user);

		Map<String, Object> result = new HashMap<>();
		result.put("message", "카카오 로그인 성공");
		result.put("accessToken", accessToken);
		result.put("refreshToken", refreshToken);
		result.put("user", Map.of(
			"userLogin", user.getUserLogin(),
			"userName", user.getUserName()
		));
		return result;
	}

	// 로그인한 사용자 정보 조회
	public User getUserByLogin(String userLogin) {
		return userRepository.findByUserLogin(userLogin)
			.orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
	}

	// 회원탈퇴
	@Transactional
	public void deleteUser(String userLogin) {
		User user = userRepository.findByUserLogin(userLogin)
			.orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

		if (!user.isStatus()) {
			throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
		}

		user.setStatus(false);
		userRepository.save(user);
	}

}