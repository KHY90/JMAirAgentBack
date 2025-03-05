package com.jmair.auth.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import com.jmair.auth.entity.User;
import com.jmair.auth.service.UserService;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserService userService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String token = null;
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("access_token".equals(cookie.getName())) {
					token = cookie.getValue();
					break;
				}
			}
		}

		if (token != null) {
			try {
				String userLogin = jwtUtil.validateAndExtractUserLogin(token);
				User user = userService.getUserByLogin(userLogin);
				// 필요에 따라 사용자 권한(GrantedAuthority) 설정
				UsernamePasswordAuthenticationToken auth =
					new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (Exception e) {
				// 유효하지 않은 토큰이면 인증 해제
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}
}
