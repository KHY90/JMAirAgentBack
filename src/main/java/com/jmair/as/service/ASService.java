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
import com.jmair.as.entity.ASEntity;
import com.jmair.as.repository.ASRepository;
import com.jmair.auth.dto.UserGrade;
import com.jmair.auth.entity.User;
import com.jmair.auth.service.UserService;
import com.jmair.cleaning.dto.CleanStatus;
import com.jmair.cleaning.dto.CleaningDTO;
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

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserGrade registerGrade;
		if (auth != null && auth.getPrincipal() instanceof User currentUser) {
			registerGrade = currentUser.getUserGrade();
		} else {
			registerGrade = UserGrade.NOUSER;
		}

		ASEntity request = ASEntity.builder()
			.cleanName(dto.getCleanName())
			.cleanNumber(dto.getCleanNumber())
			.cleanEmail(dto.getCleanEmail())
			.productType(dto.getProductType())
			.cleanDescription(dto.getCleanDescription())
			.cleanAdress(dto.getCleanAdress())
			.cleanDetailAdress(dto.getCleanDetailAdress())
			.cleanPassword(passwordEncoder.encode(dto.getCleanPassword()))
			.cleanStatus(CleanStatus.REQUEST)
			.cleanFirstReservationTime(dto.getCleanFirstReservationTime())
			.cleanSecondReservationTime(dto.getCleanSecondReservationTime())
			.registeredUserGrade(registerGrade)
			.build();

		ASEntity saved = asRepository.save(request);

		return CleaningDTO.builder()
			.cleanId(saved.getCleanId())
			.cleanName(saved.getCleanName())
			.cleanNumber(saved.getCleanNumber())
			.cleanEmail(saved.getCleanEmail())
			.productType(saved.getProductType())
			.cleanDescription(saved.getCleanDescription())
			.cleanAdress(saved.getCleanAdress())
			.cleanDetailAdress(saved.getCleanDetailAdress())
			.cleanStartTime(saved.getCleanStartTime())
			.cleanFirstReservationTime(saved.getCleanFirstReservationTime())
			.cleanSecondReservationTime(saved.getCleanSecondReservationTime())
			.cleanStatus(saved.getCleanStatus())
			.registeredUserGrade(saved.getRegisteredUserGrade())
			.build();
	}

	// 전체 조회
	@Transactional(readOnly = true)
	public List<CleaningDTO> getAllASRequests(Optional<User> currentUser, String asName, String asNumber) {
		List<ASEntity> all = asRepository.findAll();
		// 관리자
		if (currentUser.isPresent()) {
			User user = currentUser.get();
			if (user.getUserGrade() == UserGrade.ENGINEER ||
				user.getUserGrade() == UserGrade.ADMIN ||
				user.getUserGrade() == UserGrade.SUPERADMIN ||
				user.getUserGrade() == UserGrade.ADMINWATCHER) {
				return all.stream()
					.filter(entity -> entity.getCleanStatus() != CleanStatus.FALLSE)
					.map(entity -> CleaningDTO.builder()
						.cleanId(entity.getCleanId())
						.cleanName(entity.getCleanName())
						.cleanNumber(entity.getCleanNumber())
						.cleanEmail(entity.getCleanEmail())
						.productType(entity.getProductType())
						.cleanDescription(entity.getCleanDescription())
						.cleanAdress(entity.getCleanAdress())
						.cleanDetailAdress(entity.getCleanDetailAdress())
						.cleanStartTime(entity.getCleanStartTime())
						.cleanFirstReservationTime(entity.getCleanFirstReservationTime())
						.cleanSecondReservationTime(entity.getCleanSecondReservationTime())
						.cleanStatus(entity.getCleanStatus())
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
			.filter(entity -> entity.getASStatus() != CleanStatus.FALLSE)
			.filter(entity -> entity.getCleanName().equals(cleanName) && entity.getCleanNumber().equals(cleanNumber))
			.map(entity -> CleaningDTO.builder()
				.cleanId(entity.getCleanId())
				.cleanName(entity.getCleanName())
				.cleanNumber(entity.getCleanNumber())
				.cleanEmail(entity.getCleanEmail())
				.productType(entity.getProductType())
				.cleanDescription(entity.getCleanDescription())
				.cleanAdress(entity.getCleanAdress())
				.cleanDetailAdress(entity.getCleanDetailAdress())
				.cleanStartTime(entity.getCleanStartTime())
				.cleanFirstReservationTime(entity.getCleanFirstReservationTime())
				.cleanSecondReservationTime(entity.getCleanSecondReservationTime())
				.cleanStatus(entity.getCleanStatus())
				.registeredUserGrade(entity.getRegisteredUserGrade())
				.build())
			.collect(Collectors.toList());
	}

	// 상세 조회
	@Transactional(readOnly = true)
	public ASDTO getASRequestDetail(Integer asId, String providedPassword, Optional<User> currentUser) {
		ASEntity entity = asRepository.findById(asId)
			.orElseThrow(() -> new ResourceNotFoundException("세척 신청을 찾을 수 없습니다."));

		if (entity.getASStatus() == CleanStatus.FALLSE) {
			throw new ResourceNotFoundException("해당 세척 신청은 삭제되었습니다.");
		}

		// 관리 권한이 없는 경우 비밀번호 검증 (로그인하지 않은 경우도 포함)
		if (currentUser.isEmpty() || !(currentUser.get().getUserGrade() == UserGrade.ENGINEER ||
			currentUser.get().getUserGrade() == UserGrade.ADMIN ||
			currentUser.get().getUserGrade() == UserGrade.SUPERADMIN ||
			currentUser.get().getUserGrade() == UserGrade.ADMINWATCHER)) {
			if (providedPassword == null || !passwordEncoder.matches(providedPassword, entity.getCleanPassword())) {
				throw new UnauthorizedException("비밀번호가 일치하지 않거나 비밀번호가 필요합니다.");
			}
		}

		return CleaningDTO.builder()
			.cleanId(entity.getCleanId())
			.cleanName(entity.getCleanName())
			.cleanNumber(entity.getCleanNumber())
			.cleanEmail(entity.getCleanEmail())
			.productType(entity.getProductType())
			.cleanDescription(entity.getCleanDescription())
			.cleanAdress(entity.getCleanAdress())
			.cleanDetailAdress(entity.getCleanDetailAdress())
			.cleanStartTime(entity.getCleanStartTime())
			.cleanEditTime(entity.getCleanEditTime())
			.cleanFirstReservationTime(entity.getCleanFirstReservationTime())
			.cleanSecondReservationTime(entity.getCleanSecondReservationTime())
			.cleanStatus(entity.getCleanStatus())
			.registeredUserGrade(entity.getRegisteredUserGrade())
			.build();
	}

	// 일반 사용자 수정(비밀번호 확인 필요)
	@Transactional
	public ASDTO editASRequestForUser(Integer asId, ASDTO dto, String providedPassword) {
		ASEntity entity = asRepository.findById(asId)
			.orElseThrow(() -> new ResourceNotFoundException("세척 신청을 찾을 수 없습니다."));

		if (entity.getASStatus() == CleanStatus.FALLSE) {
			throw new ResourceNotFoundException("해당 세척 신청은 삭제되었습니다.");
		}

		// 비밀번호 검증 (비로그인 혹은 일반 사용자의 경우)
		if (providedPassword == null || !passwordEncoder.matches(providedPassword, entity.getCleanPassword())) {
			throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
		}

		// 일반 사용자가 수정 가능한 모든 필드 업데이트
		entity.setCleanName(dto.getCleanName());
		entity.setCleanNumber(dto.getCleanNumber());
		entity.setCleanEmail(dto.getCleanEmail());
		entity.setProductType(dto.getProductType());
		entity.setCleanDescription(dto.getCleanDescription());
		entity.setCleanAdress(dto.getCleanAdress());
		entity.setCleanDetailAdress(dto.getCleanDetailAdress());
		entity.setCleanStartTime(dto.getCleanStartTime());
		entity.setCleanFirstReservationTime(dto.getCleanFirstReservationTime());
		entity.setCleanSecondReservationTime(dto.getCleanSecondReservationTime());
		entity.setCleanNote(dto.getCleanNote());
		entity.setCleanStatus(dto.getCleanStatus());

		// 수정 시 수정 시간 업데이트
		entity.setASEditTime(LocalDateTime.now());

		ASEntity updated = asRepository.save(entity);

		return CleaningDTO.builder()
			.cleanId(updated.getCleanId())
			.cleanName(updated.getCleanName())
			.cleanNumber(updated.getCleanNumber())
			.cleanEmail(updated.getCleanEmail())
			.productType(updated.getProductType())
			.cleanDescription(updated.getCleanDescription())
			.cleanAdress(updated.getCleanAdress())
			.cleanDetailAdress(updated.getCleanDetailAdress())
			.cleanEditTime(updated.getCleanEditTime())
			.cleanFirstReservationTime(updated.getCleanFirstReservationTime())
			.cleanSecondReservationTime(updated.getCleanSecondReservationTime())
			.cleanStatus(updated.getCleanStatus())
			.cleanNote(updated.getCleanNote())
			.registeredUserGrade(updated.getRegisteredUserGrade())
			.build();
	}

	// 관리자 수정: cleanStatus와 cleanNote만 수정 가능
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

		if (entity.getASStatus() == CleanStatus.FALLSE) {
			throw new ResourceNotFoundException("해당 세척 신청은 삭제되었습니다.");
		}

		// 관리자용 수정: cleanStatus와 cleanNote만 업데이트
		entity.setCleanStatus(dto.getCleanStatus());
		entity.setCleanNote(dto.getCleanNote());
		entity.setCleanEditTime(LocalDateTime.now());

		ASEntity updated = asRepository.save(entity);

		return ASDTO.builder()
			.cleanId(updated.getCleanId())
			.cleanName(updated.getCleanName())
			.cleanNumber(updated.getCleanNumber())
			.cleanEmail(updated.getCleanEmail())
			.productType(updated.getProductType())
			.cleanDescription(updated.getCleanDescription())
			.cleanAdress(updated.getCleanAdress())
			.cleanDetailAdress(updated.getCleanDetailAdress())
			.cleanStartTime(updated.getCleanStartTime())
			.cleanEditTime(updated.getCleanEditTime())
			.cleanFirstReservationTime(updated.getCleanFirstReservationTime())
			.cleanSecondReservationTime(updated.getCleanSecondReservationTime())
			.cleanStatus(updated.getCleanStatus())
			.cleanNote(updated.getCleanNote())
			.registeredUserGrade(updated.getRegisteredUserGrade())
			.build();
	}

	// 삭제
	@Transactional
	public void deleteASRequest(Integer asId) {
		ASEntity entity = asRepository.findById(asId)
			.orElseThrow(() -> new ResourceNotFoundException("세척 신청을 찾을 수 없습니다."));
		entity.setASStatus(CleanStatus.FALLSE);
		entity.setASEndTime(LocalDateTime.now());
		asRepository.save(entity);
	}
}
