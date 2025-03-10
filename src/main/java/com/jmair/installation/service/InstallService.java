package com.jmair.installation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jmair.auth.dto.UserGrade;
import com.jmair.auth.entity.User;
import com.jmair.common.exeption.ForbiddenException;
import com.jmair.common.exeption.ResourceNotFoundException;
import com.jmair.common.exeption.UnauthorizedException;
import com.jmair.installation.dto.Install;
import com.jmair.installation.dto.InstallDTO;
import com.jmair.installation.entity.InstallRequest;
import com.jmair.installation.repository.InstallRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstallService {

	private final InstallRepository installRepository;
	private final PasswordEncoder passwordEncoder;

	// 설치 신청 등록
	@Transactional
	public InstallDTO createInstallRequest(InstallDTO installDTO) {
		InstallRequest request = InstallRequest.builder()
			.installName(installDTO.getInstallName())
			.installAddress(installDTO.getInstallAddress())
			.installPhone(installDTO.getInstallPhone())
			.installNumber(installDTO.getInstallNumber())
			.installEmail(installDTO.getInstallEmail())
			// 비밀번호 암호화
			.installPassword(passwordEncoder.encode(installDTO.getInstallPassword()))
			.installDescription(installDTO.getInstallDescription())
			// 신청한 날짜
			.requestDate(LocalDateTime.now())
			// 희망 날짜(1순위)
			.reservationFirstDate(installDTO.getReservationFirstDate())
			// 희망 날짜(2순위)
			.reservationSecondDate(installDTO.getReservationSecondDate())
			// 최초 상태는 REQUEST로 설정
			.installStatus(Install.REQUEST)
			.build();

		InstallRequest saved = installRepository.save(request);

		return InstallDTO.builder()
			.installId(saved.getInstallId())
			.installName(saved.getInstallName())
			.installAddress(saved.getInstallAddress())
			.installPhone(saved.getInstallPhone())
			.installNumber(saved.getInstallNumber())
			.installEmail(saved.getInstallEmail())
			.installPassword(saved.getInstallPassword())
			.installDescription(saved.getInstallDescription())
			.requestDate(saved.getRequestDate())
			.reservationFirstDate(saved.getReservationFirstDate())
			.reservationSecondDate(saved.getReservationSecondDate())
			.installStatus(saved.getInstallStatus())
			.build();
	}

	// 전체 조회
	public List<InstallDTO> getAllInstallRequests() {
		return installRepository.findAll().stream()
			.map(req -> InstallDTO.builder()
				.installId(req.getInstallId())
				.installName(req.getInstallName())
				.installAddress(req.getInstallAddress())
				.installPhone(req.getInstallPhone())
				.installNumber(req.getInstallNumber())
				.installEmail(req.getInstallEmail())
				.installDescription(req.getInstallDescription())
				.requestDate(req.getRequestDate())
				.reservationFirstDate(req.getReservationFirstDate())
				.reservationSecondDate(req.getReservationSecondDate())
				.installStatus(req.getInstallStatus())
				.build())
			.collect(Collectors.toList());
	}

	// 상세 조회
	@Transactional(readOnly = true)
	public InstallDTO getInstallRequestDetail(Integer installId, String providedPassword, Optional<User> currentUser) {
		InstallRequest request = installRepository.findById(installId)
			.orElseThrow(() -> new ResourceNotFoundException("설치 신청을 찾을 수 없습니다."));

		if (request.getInstallStatus() == Install.FALLSE) {
			throw new ResourceNotFoundException("해당 설치 신청은 삭제되었습니다.");
		}

		// 로그인한 사용자가 있으면 등급에 따라 비밀번호 검증
		if (currentUser.isPresent()) {
			User user = currentUser.get();
			if (!(user.getUserGrade() == UserGrade.ENGINEER ||
				user.getUserGrade() == UserGrade.ADMIN ||
				user.getUserGrade() == UserGrade.SUPERADMIN ||
				user.getUserGrade() == UserGrade.ADMINWATCHER)) {
				if (providedPassword == null || !passwordEncoder.matches(providedPassword, request.getInstallPassword())) {
					throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
				}
			}
		} else {
			// 로그인하지 않은 경우 비밀번호가 필수
			if (providedPassword == null || !passwordEncoder.matches(providedPassword, request.getInstallPassword())) {
				throw new UnauthorizedException("비밀번호가 필요합니다.");
			}
		}

		return InstallDTO.builder()
			.installId(request.getInstallId())
			.installName(request.getInstallName())
			.installAddress(request.getInstallAddress())
			.installPhone(request.getInstallPhone())
			.installNumber(request.getInstallNumber())
			.installEmail(request.getInstallEmail())
			.installDescription(request.getInstallDescription())
			.requestDate(request.getRequestDate())
			.reservationFirstDate(request.getReservationFirstDate())
			.reservationSecondDate(request.getReservationSecondDate())
			.installStatus(request.getInstallStatus())
			.installNote(request.getInstallNote())
			.build();
	}

	// 수정 - 관리자(ENGINEER, ADMIN, SUPERADMIN)만 수정 가능. 수정 가능한 필드는 설치 상태와 비고(installNote)만
	@Transactional
	public InstallDTO editInstallRequest(Integer installId, InstallDTO installDTO) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
			throw new UnauthorizedException("로그인한 사용자 정보가 없습니다.");
		}
		if (!(currentUser.getUserGrade() == UserGrade.ENGINEER ||
			currentUser.getUserGrade() == UserGrade.ADMIN ||
			currentUser.getUserGrade() == UserGrade.SUPERADMIN)) {
			throw new ForbiddenException("수정 권한이 없습니다.");
		}

		InstallRequest request = installRepository.findById(installId)
			.orElseThrow(() -> new ResourceNotFoundException("설치 신청을 찾을 수 없습니다."));

		// 수정 가능한 필드 업데이트
		request.setInstallStatus(installDTO.getInstallStatus());
		request.setInstallNote(installDTO.getInstallNote());
		request.setEditTime(LocalDateTime.now());

		InstallRequest updated = installRepository.save(request);

		return InstallDTO.builder()
			.installId(updated.getInstallId())
			.installName(updated.getInstallName())
			.installAddress(updated.getInstallAddress())
			.installPhone(updated.getInstallPhone())
			.installNumber(updated.getInstallNumber())
			.installEmail(updated.getInstallEmail())
			.installDescription(updated.getInstallDescription())
			.requestDate(updated.getRequestDate())
			.reservationFirstDate(updated.getReservationFirstDate())
			.reservationSecondDate(updated.getReservationSecondDate())
			.installStatus(updated.getInstallStatus())
			.installNote(updated.getInstallNote())
			.build();
	}

	// 삭제 - 관리자(ENGINEER, ADMIN, SUPERADMIN)만 삭제 가능 (소프트 삭제: 상태를 CANCEL로 변경)
	@Transactional
	public void deleteInstallRequest(Integer installId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
			throw new UnauthorizedException("로그인한 사용자 정보가 없습니다.");
		}
		if (!(currentUser.getUserGrade() == UserGrade.ENGINEER ||
			currentUser.getUserGrade() == UserGrade.ADMIN ||
			currentUser.getUserGrade() == UserGrade.SUPERADMIN)) {
			throw new ForbiddenException("삭제 권한이 없습니다.");
		}

		InstallRequest request = installRepository.findById(installId)
			.orElseThrow(() -> new ResourceNotFoundException("설치 신청을 찾을 수 없습니다."));
		request.setInstallStatus(Install.CANCEL);
		installRepository.save(request);
	}
}