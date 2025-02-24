package com.jmair.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jmair.auth.dto.UserDTO;
import com.jmair.auth.dto.UserGrade;
import com.jmair.auth.entity.User;
import com.jmair.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// 회원가입
	public void join(UserDTO userDTO) {
		// 유저 ID가 이미 존재하는지 체크
		if (userRepository.existsByUserLogin(userDTO.getUserLogin())) {
			throw new IllegalArgumentException("이미 존재하는 회원입니다.");
		}

		// DTO를 엔티티로 변환하고 비밀번호 암호화
		User user = new User();
		user.setUserLogin(userDTO.getUserLogin());
		user.setUserName(userDTO.getUserName());
		user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		user.setPhoneNumber(userDTO.getPhoneNumber());
		user.setEmail(userDTO.getEmail());
		user.setUserGrade(UserGrade.valueOf("USER"));
		user.setStatus(true);
		userRepository.save(user);
	}
}
