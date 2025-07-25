package com.jmair.auth.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.jmair.common.exeption.TokenExpiredException;
import com.jmair.common.exeption.TokenInvalidException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jmair.auth.dto.LoginDTO;
import com.jmair.auth.dto.SocialDTO;
import com.jmair.auth.dto.Tokens;
import com.jmair.auth.dto.UserDTO;
import com.jmair.auth.dto.UserGrade;
import com.jmair.auth.dto.response.EngineerApplicantDTO;
import com.jmair.auth.dto.response.EngineerApplyDTO;
import com.jmair.auth.dto.response.EngineerStatusDTO;
import com.jmair.auth.dto.response.UserResponseDTO;
import com.jmair.auth.entity.User;
import com.jmair.auth.repository.UserRepository;
import com.jmair.auth.util.JwtUtil;
import com.jmair.common.exeption.ForbiddenException;
import com.jmair.common.exeption.UnauthorizedException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements TokenValidator, UserLookupService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	// TokenValidator
	@Override
	public User validateTokenAndGetUser(String token) throws TokenExpiredException {
		try {
			String userLogin = jwtUtil.validateAndExtractUserLogin(token);
			return getUserByLogin(userLogin);
		} catch (ExpiredJwtException e) {
			throw new TokenExpiredException("토큰이 만료되었습니다.");
		} catch (JwtException e) {
			throw new TokenInvalidException("토큰이 유효하지 않습니다.");
		}
	}

	// UserLookupService
	@Override
	public User getUserByLogin(String userLogin) {
		return userRepository.findByUserLogin(userLogin)
				.orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
	}

	// 회원가입
	@Transactional
	public void join(UserDTO userDTO) {
		if (userRepository.existsByUserLogin(userDTO.getUserLogin())) {
			throw new IllegalArgumentException("이미 존재하는 회원입니다.");
		}
		String encodedPassword = passwordEncoder.encode(userDTO.getPassword());

		User user = new User();
		user.setUserLogin(userDTO.getUserLogin());
		user.setUserName(userDTO.getUserName());
		user.setPassword(encodedPassword);
		user.setPhoneNumber(userDTO.getPhoneNumber());
		user.setEmail(userDTO.getEmail());
		user.setUserGrade(UserGrade.USER);
		user.setJoinDate(LocalDateTime.now());
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
			user.setJoinDate(LocalDateTime.now());
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
			user.setJoinDate(LocalDateTime.now());
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

	// 전체 회원 조회 (관리자용)
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersForAdmin(HttpServletRequest request) {
		String accessToken = extractAccessTokenFromRequest(request);
		if (accessToken == null) {
			throw new UnauthorizedException("로그인 정보가 없습니다.");
		}
		User currentUser = validateTokenAndGetUser(accessToken);
		if (!(currentUser.getUserGrade() == UserGrade.ENGINEER ||
				currentUser.getUserGrade() == UserGrade.ADMIN ||
				currentUser.getUserGrade() == UserGrade.SUPERADMIN ||
				currentUser.getUserGrade() == UserGrade.ADMINWATCHER)) {
			throw new ForbiddenException("관리자만 회원 목록을 조회할 수 있습니다.");
		}
        List<User> users = userRepository.findAll();
        return users.stream().map(u -> {
                UserResponseDTO dto = new UserResponseDTO();
                dto.setUserLogin(u.getUserLogin());
                dto.setUserName(u.getUserName());
                dto.setEmail(u.getEmail());
                dto.setPhoneNumber(u.getPhoneNumber());
                dto.setJoinDate(u.getJoinDate());
                dto.setUserGrade(u.getUserGrade());
                dto.setStatus(u.isStatus());
                return dto;
        }).collect(Collectors.toList());
	}

	// 회원 상세 조회 (관리자 또는 자신만 조회)
    public UserResponseDTO getUserDetail(String userLogin, HttpServletRequest request) {
		String accessToken = extractAccessTokenFromRequest(request);
		if (accessToken == null) {
			throw new UnauthorizedException("로그인 정보가 없습니다.");
		}
		User currentUser = validateTokenAndGetUser(accessToken);
		if (!(currentUser.getUserGrade() == UserGrade.ENGINEER ||
				currentUser.getUserGrade() == UserGrade.ADMIN ||
				currentUser.getUserGrade() == UserGrade.SUPERADMIN ||
				currentUser.getUserGrade() == UserGrade.ADMINWATCHER ||
				currentUser.getUserGrade() == UserGrade.WAITING) &&
				!currentUser.getUserLogin().equals(userLogin)) {
			throw new ForbiddenException("자신의 회원 정보만 조회할 수 있습니다.");
		}
        User user = getUserByLogin(userLogin);
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserLogin(user.getUserLogin());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setJoinDate(user.getJoinDate());
        dto.setUserGrade(user.getUserGrade());
        dto.setStatus(user.isStatus());
        return dto;
    }

	// 쿠키에서 access_token 추출
	private String extractAccessTokenFromRequest(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("access_token".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	// 전체 회원 조회 (단순 조회)
	@Transactional(readOnly = true)
	public List<User> getAllUsers() {
		return userRepository.findAll();
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
		user.setDeleteDate(LocalDateTime.now());
				userRepository.save(user);
	}

        // 엔지니어 신청
    @Transactional
    public EngineerApplyDTO applyForEngineer(HttpServletRequest request) {
                String accessToken = extractAccessTokenFromRequest(request);
                if (accessToken == null) {
                        throw new UnauthorizedException("로그인 정보가 없습니다.");
                }
                User user = validateTokenAndGetUser(accessToken);

                if (user.getUserGrade() != UserGrade.USER) {
                        throw new IllegalArgumentException("엔지니어 신청이 불가능한 등급입니다.");
                }

                user.setUserGrade(UserGrade.WAITING);
        user.setEngineerAppliedAt(LocalDateTime.now());

        EngineerApplyDTO dto = new EngineerApplyDTO();
        dto.setUserGrade(user.getUserGrade());
        dto.setAppliedAt(user.getEngineerAppliedAt());
        return dto;
    }

    // 엔지니어 신청 상태 조회
    @Transactional(readOnly = true)
    public EngineerStatusDTO getEngineerStatus(HttpServletRequest request) {
		String accessToken = extractAccessTokenFromRequest(request);
		if (accessToken == null) {
						throw new UnauthorizedException("로그인 정보가 없습니다.");
		}
		User user = validateTokenAndGetUser(accessToken);
        UserGrade grade = user.getUserGrade();
        if (user.getEngineerAppliedAt() != null && grade == UserGrade.USER) {
                        grade = UserGrade.WAITING;
        }
        EngineerStatusDTO dto = new EngineerStatusDTO();
        dto.setStatus(grade);
        dto.setAppliedAt(user.getEngineerAppliedAt());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<EngineerApplicantDTO> getEngineerApplicants(HttpServletRequest request) {
		String accessToken = extractAccessTokenFromRequest(request);
		if (accessToken == null) {
			throw new UnauthorizedException("로그인 정보가 없습니다.");
		}
		User currentUser = validateTokenAndGetUser(accessToken);
		if (!(currentUser.getUserGrade() == UserGrade.ADMIN ||
				currentUser.getUserGrade() == UserGrade.SUPERADMIN)) {
			throw new ForbiddenException("관리자만 조회할 수 있습니다.");
		}

                List<User> waitingUsers = userRepository
                                .findByEngineerAppliedAtIsNotNullAndUserGrade(UserGrade.USER);
        return waitingUsers.stream()
                                .map(u -> {
                                        EngineerApplicantDTO m = new EngineerApplicantDTO();
                                        m.setUserLogin(u.getUserLogin());
                                        m.setUserName(u.getUserName());
                                        m.setAppliedAt(u.getEngineerAppliedAt());
                                        return m;
                                })
                                .collect(Collectors.toList());
	}


	// 엔지니어로 등급 변경 (관리자용)
	@Transactional
	public void promoteToEngineer(String userLogin, HttpServletRequest request) {
		String accessToken = extractAccessTokenFromRequest(request);
		if (accessToken == null) {
			throw new UnauthorizedException("로그인 정보가 없습니다.");
		}
		User currentUser = validateTokenAndGetUser(accessToken);
		if (!(currentUser.getUserGrade() == UserGrade.ADMIN ||
			currentUser.getUserGrade() == UserGrade.SUPERADMIN)) {
			throw new ForbiddenException("관리자만 등급을 변경할 수 있습니다.");
		}

		User userToPromote = userRepository.findByUserLogin(userLogin)
			.orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

		userToPromote.setUserGrade(UserGrade.ENGINEER);
		userRepository.save(userToPromote);
	}
}
