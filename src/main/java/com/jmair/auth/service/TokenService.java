package com.jmair.auth.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jmair.auth.entity.User;
import com.jmair.auth.util.JwtUtil;
import com.jmair.common.exeption.TokenExpiredException;
import com.jmair.common.exeption.TokenInvalidException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@Service
public class TokenService {

	private final JwtUtil jwtUtil;
	private final UserService userService;

	public TokenService(JwtUtil jwtUtil, UserService userService) {
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	public User validateTokenAndGetUser(String token) throws TokenExpiredException {
		try {
			String userLogin = jwtUtil.validateAndExtractUserLogin(token);
			return userService.getUserByLogin(userLogin);
		} catch (ExpiredJwtException e) {
			throw new TokenExpiredException("토큰이 만료되었습니다.");
		} catch (JwtException e) {
			throw new TokenInvalidException("토큰이 유효하지 않습니다.");
		}
	}

	public Map<String, String> refreshToken(User user) {
		String newAccessToken = jwtUtil.generateAccessToken(user);
		String newRefreshToken = jwtUtil.generateRefreshToken(user);
		Map<String, String> tokens = new HashMap<>();
		tokens.put("accessToken", newAccessToken);
		tokens.put("refreshToken", newRefreshToken);
		return tokens;
	}
}
