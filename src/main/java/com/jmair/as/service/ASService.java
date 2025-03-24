package com.jmair.as.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jmair.as.dto.ASDTO;
import com.jmair.as.dto.ASStatus;
import com.jmair.as.entity.ASEntity;
import com.jmair.as.repository.ASRepository;
import com.jmair.auth.dto.UserGrade;
import com.jmair.auth.entity.User;
import com.jmair.auth.service.UserService;
import com.jmair.common.exeption.ForbiddenException;
import com.jmair.common.exeption.ResourceNotFoundException;
import com.jmair.common.exeption.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ASService {

	private final ASRepository asRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserService userService;

	// 등록
	@Transactional
	public ASDTO createASRequest(ASDTO dto) {
		// 현재 인증된 사용자가 있다면 해당 유저의 UserGrade를, 없으면 NOUSER로 처리
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserGrade registerGrade;
		if (auth != null && auth.getPrincipal() instanceof User currentUser) {
			registerGrade = currentUser.getUserGrade();
		} else {
			registerGrade = UserGrade.NOUSER;
		}

		ASEntity request = ASEntity.builder()
			.asName(dto.getAsName())
			.asNumber(dto.getAsNumber())
			.asEmail(dto.getAsEmail())
			.productType(dto.getProductType())
			.asDescription(dto.getAsDescription())
			.asAdress(dto.getAsAdress())
			.asDetailAdress(dto.getAsDetailAdress())
			.asPassword(passwordEncoder.encode(dto.getAsPassword()))
			.asStatus(ASStatus.REQUEST)
			.asFirstReservationTime(dto.getAsFirstReservationTime())
			.asSecondReservationTime(dto.getAsSecondReservationTime())
			.registeredUserGrade(registerGrade)
			.build();

		ASEntity saved = asRepository.save(request);

		return ASDTO.builder()
			.asId(saved.getAsId())
			.asName(saved.getAsName())
			.asNumber(saved.getAsNumber())
			.asEmail(saved.getAsEmail())
			.productType(saved.getProductType())
			.asDescription(saved.getAsDescription())
			.asAdress(saved.getAsAdress())
			.asDetailAdress(saved.getAsDetailAdress())
			.asStartTime(saved.getAsStartTime())
			.asFirstReservationTime(saved.getAsFirstReservationTime())
			.asSecondReservationTime(saved.getAsSecondReservationTime())
			.asStatus(saved.getAsStatus())
			.registeredUserGrade(saved.getRegisteredUserGrade())
			.build();
	}

	// 전체 조회
	@Transactional(readOnly = true)
	public List<ASDTO> getAllASRequests(Optional<User> currentUser, String asName, String asNumber) {
		List<ASEntity> all = asRepository.findAll();
		// 관리자
		if (currentUser.isPresent()) {
			User user = currentUser.get();
			if (user.getUserGrade() == UserGrade.ENGINEER ||
				user.getUserGrade() == UserGrade.ADMIN ||
				user.getUserGrade() == UserGrade.SUPERADMIN ||
				user.getUserGrade() == UserGrade.ADMINWATCHER) {
				return all.stream()
					.filter(entity -> entity.getAsStatus() != ASStatus.FALLSE)
					.map(entity -> ASDTO.builder()
						.asId(entity.getAsId())
						.asName(entity.getAsName())
						.productType(entity.getProductType())
						.asStartTime(entity.getAsStartTime())
						.asEditTime(entity.getAsEditTime())
						.asStatus(entity.getAsStatus())
						.registeredUserGrade(entity.getRegisteredUserGrade())
						.build())
					.collect(Collectors.toList());
			}
		}
		// 일반 사용자 또는 비로그인 사용자의 경우, 이름과 핸드폰 번호를 필수로 받아 일치하는 건만 조회
		if (asName == null || asName.isBlank() || asNumber == null || asNumber.isBlank()) {
			throw new IllegalArgumentException("일반 사용자는 이름과 핸드폰 번호를 제공해야 합니다.");
		}
		return all.stream()
			.filter(entity -> entity.getAsStatus() != ASStatus.FALLSE)
			.filter(entity -> entity.getAsName().equals(asName) && entity.getAsNumber().equals(asNumber))
			.map(entity -> ASDTO.builder()
				.asId(entity.getAsId())
				.asName(entity.getAsName())
				.asNumber(entity.getAsNumber())
				.asEmail(entity.getAsEmail())
				.productType(entity.getProductType())
				.asDescription(entity.getAsDescription())
				.asAdress(entity.getAsAdress())
				.asDetailAdress(entity.getAsDetailAdress())
				.asStartTime(entity.getAsStartTime())
				.asFirstReservationTime(entity.getAsFirstReservationTime())
				.asSecondReservationTime(entity.getAsSecondReservationTime())
				.asStatus(entity.getAsStatus())
				.registeredUserGrade(entity.getRegisteredUserGrade())
				.build())
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ASDTO getASRequestDetail(Integer asId, String providedPassword, Optional<User> currentUser) {
		ASEntity entity = asRepository.findById(asId)
			.orElseThrow(() -> new ResourceNotFoundException("세척 신청을 찾을 수 없습니다."));
		if (entity.getAsStatus() == ASStatus.FALLSE) {
			throw new ResourceNotFoundException("해당 세척 신청은 삭제되었습니다.");
		}
		boolean isAdmin = currentUser.isPresent() &&
			(currentUser.get().getUserGrade() == UserGrade.ENGINEER ||
				currentUser.get().getUserGrade() == UserGrade.ADMIN ||
				currentUser.get().getUserGrade() == UserGrade.SUPERADMIN ||
				currentUser.get().getUserGrade() == UserGrade.ADMINWATCHER);
		if (!isAdmin) {
			if (providedPassword == null || providedPassword.isBlank()) {
				throw new UnauthorizedException("비밀번호가 필요합니다.");
			}
			boolean matches = passwordEncoder.matches(providedPassword, entity.getAsPassword());
			System.out.println("passwordEncoder.matches 결과: " + matches);
			if (!matches) {
				throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
			}
		}

		return ASDTO.builder()
			.asId(entity.getAsId())
			.asName(entity.getAsName())
			.asNumber(entity.getAsNumber())
			.asEmail(entity.getAsEmail())
			.productType(entity.getProductType())
			.asDescription(entity.getAsDescription())
			.asAdress(entity.getAsAdress())
			.asDetailAdress(entity.getAsDetailAdress())
			.asStartTime(entity.getAsStartTime())
			.asEditTime(entity.getAsEditTime())
			.asFirstReservationTime(entity.getAsFirstReservationTime())
			.asSecondReservationTime(entity.getAsSecondReservationTime())
			.asStatus(entity.getAsStatus())
			.registeredUserGrade(entity.getRegisteredUserGrade())
			.build();
	}

	// 일반 사용자 수정(비밀번호 확인 필요)
	@Transactional
	public ASDTO editASRequestForUser(Integer asId, ASDTO dto, String providedPassword) {
		ASEntity entity = asRepository.findById(asId)
			.orElseThrow(() -> new ResourceNotFoundException("세척 신청을 찾을 수 없습니다."));

		if (entity.getAsStatus() == ASStatus.FALLSE) {
			throw new ResourceNotFoundException("해당 세척 신청은 삭제되었습니다.");
		}

		// 비밀번호 검증 (비로그인 혹은 일반 사용자의 경우)
		if (providedPassword == null || !passwordEncoder.matches(providedPassword, entity.getAsPassword())) {
			throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
		}

		// 일반 사용자가 수정 가능한 모든 필드 업데이트
		entity.setAsName(dto.getAsName());
		entity.setAsNumber(dto.getAsNumber());
		entity.setAsEmail(dto.getAsEmail());
		entity.setProductType(dto.getProductType());
		entity.setAsDescription(dto.getAsDescription());
		entity.setAsAdress(dto.getAsAdress());
		entity.setAsDetailAdress(dto.getAsDetailAdress());
		entity.setAsStartTime(dto.getAsStartTime());
		entity.setAsFirstReservationTime(dto.getAsFirstReservationTime());
		entity.setAsSecondReservationTime(dto.getAsSecondReservationTime());
		entity.setAsNote(dto.getAsNote());
		entity.setAsStatus(dto.getAsStatus());

		// 수정 시 수정 시간 업데이트
		entity.setAsEditTime(LocalDateTime.now());

		ASEntity updated = asRepository.save(entity);

		return ASDTO.builder()
			.asId(updated.getAsId())
			.asName(updated.getAsName())
			.asNumber(updated.getAsNumber())
			.asEmail(updated.getAsEmail())
			.productType(updated.getProductType())
			.asDescription(updated.getAsDescription())
			.asAdress(updated.getAsAdress())
			.asDetailAdress(updated.getAsDetailAdress())
			.asEditTime(updated.getAsEditTime())
			.asFirstReservationTime(updated.getAsFirstReservationTime())
			.asSecondReservationTime(updated.getAsSecondReservationTime())
			.asStatus(updated.getAsStatus())
			.asNote(updated.getAsNote())
			.registeredUserGrade(updated.getRegisteredUserGrade())
			.build();
	}

	// 관리자 수정: asStatus와 asNote만 수정 가능
	@Transactional
	public ASDTO editASRequestForAdmin(Integer asId, ASDTO dto) {
		// 현재 로그인한 관리자 사용자 검증
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof User currentUser)) {
			throw new UnauthorizedException("로그인한 사용자 정보가 없습니다.");
		}
		if (!(currentUser.getUserGrade() == UserGrade.ENGINEER ||
			currentUser.getUserGrade() == UserGrade.ADMIN ||
			currentUser.getUserGrade() == UserGrade.SUPERADMIN ||
			currentUser.getUserGrade() == UserGrade.ADMINWATCHER)) {
			throw new ForbiddenException("수정 권한이 없습니다.");
		}

		ASEntity entity = asRepository.findById(asId)
			.orElseThrow(() -> new ResourceNotFoundException("세척 신청을 찾을 수 없습니다."));

		if (entity.getAsStatus() == ASStatus.FALLSE) {
			throw new ResourceNotFoundException("해당 세척 신청은 삭제되었습니다.");
		}

		// 관리자용 수정: asStatus와 asNote만 업데이트
		entity.setAsStatus(dto.getAsStatus());
		entity.setAsNote(dto.getAsNote());
		entity.setAsEditTime(LocalDateTime.now());

		ASEntity updated = asRepository.save(entity);

		return ASDTO.builder()
			.asId(updated.getAsId())
			.asName(updated.getAsName())
			.asNumber(updated.getAsNumber())
			.asEmail(updated.getAsEmail())
			.productType(updated.getProductType())
			.asDescription(updated.getAsDescription())
			.asAdress(updated.getAsAdress())
			.asDetailAdress(updated.getAsDetailAdress())
			.asStartTime(updated.getAsStartTime())
			.asEditTime(updated.getAsEditTime())
			.asFirstReservationTime(updated.getAsFirstReservationTime())
			.asSecondReservationTime(updated.getAsSecondReservationTime())
			.asStatus(updated.getAsStatus())
			.asNote(updated.getAsNote())
			.registeredUserGrade(updated.getRegisteredUserGrade())
			.build();
	}

	// 삭제
	@Transactional
	public void deleteASRequest(Integer asId) {
		ASEntity entity = asRepository.findById(asId)
			.orElseThrow(() -> new ResourceNotFoundException("세척 신청을 찾을 수 없습니다."));
		entity.setAsStatus(ASStatus.FALLSE);
		entity.setAsEndTime(LocalDateTime.now());
		asRepository.save(entity);
	}
}
