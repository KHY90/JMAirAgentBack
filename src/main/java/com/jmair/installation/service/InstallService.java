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
		// 현재 인증된 사용자를 확인 (로그인한 사용자가 있다면)
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserGrade registerGrade;
		if (authentication != null && authentication.getPrincipal() instanceof User currentUser) {
			registerGrade = currentUser.getUserGrade();
		} else {
			// 비로그인 상태이면 NOUSER 처리
			registerGrade = UserGrade.NOUSER;
		}

		InstallRequest request = InstallRequest.builder()
			.installName(installDTO.getInstallName())
			.installAddress(installDTO.getInstallAddress())
			.installDetailAddress(installDTO.getInstallDetailAddress())
			.installPhone(installDTO.getInstallPhone())
			.installNumber(installDTO.getInstallNumber())
			.installEmail(installDTO.getInstallEmail())
			// 비밀번호 암호화 적용
			.installPassword(passwordEncoder.encode(installDTO.getInstallPassword()))
			.installDescription(installDTO.getInstallDescription())
			// 신청한 날짜는 현재 시간으로 설정
			.requestDate(LocalDateTime.now())
			.reservationFirstDate(installDTO.getReservationFirstDate())
			.reservationSecondDate(installDTO.getReservationSecondDate())
			// 최초 상태는 REQUEST로 설정
			.installStatus(Install.REQUEST)
			// 등록자의 UserGrade 저장 (로그인했으면 해당 등급, 아니면 NOUSER)
			.registeredUserGrade(registerGrade)
			.build();

		InstallRequest saved = installRepository.save(request);

		return InstallDTO.builder()
			.installId(saved.getInstallId())
			.installName(saved.getInstallName())
			.installAddress(saved.getInstallAddress())
			.installDetailAddress(saved.getInstallDetailAddress())
			.installPhone(saved.getInstallPhone())
			.installNumber(saved.getInstallNumber())
			.installEmail(saved.getInstallEmail())
			.installPassword(saved.getInstallPassword())
			.installDescription(saved.getInstallDescription())
			.requestDate(saved.getRequestDate())
			.reservationFirstDate(saved.getReservationFirstDate())
			.reservationSecondDate(saved.getReservationSecondDate())
			.installStatus(saved.getInstallStatus())
			.registeredUserGrade(saved.getRegisteredUserGrade())
			.build();
	}

	// 전체 조회
	public List<InstallDTO> getAllInstallRequests(Optional<User> currentUser, String installName, String installPhone) {
		List<InstallRequest> all = installRepository.findAll().stream()
			.filter(req -> req.getInstallStatus() != Install.FALLSE)
			.toList();

		// 관리자 등급이면 전체 조회
		if (currentUser.isPresent()) {
			User user = currentUser.get();
			if (user.getUserGrade() == UserGrade.ENGINEER ||
				user.getUserGrade() == UserGrade.ADMIN ||
				user.getUserGrade() == UserGrade.SUPERADMIN ||
				user.getUserGrade() == UserGrade.ADMINWATCHER) {
				return all.stream()
					.map(req -> InstallDTO.builder()
						.installId(req.getInstallId())
						.installName(req.getInstallName())
						.installPhone(req.getInstallPhone())
						.installNumber(req.getInstallNumber())
						.installDescription(req.getInstallDescription())
						.requestDate(req.getRequestDate())
						.installStatus(req.getInstallStatus())
						.registeredUserGrade(req.getRegisteredUserGrade())
						.build())
					.collect(Collectors.toList());
			}
		}
		// 일반 사용자 또는 비로그인 사용자는 이름과 핸드폰 번호 필수
		if (installName == null || installName.isBlank() || installPhone == null || installPhone.isBlank()) {
			throw new IllegalArgumentException("일반 사용자는 설치 신청 조회 시 이름과 핸드폰 번호를 제공해야 합니다.");
		}
		return all.stream()
			.filter(req -> req.getInstallName().equals(installName) && req.getInstallPhone().equals(installPhone))
			.map(req -> InstallDTO.builder()
				.installId(req.getInstallId())
				.installName(req.getInstallName())
				.installPhone(req.getInstallPhone())
				.requestDate(req.getRequestDate())
				.installStatus(req.getInstallStatus())
				.registeredUserGrade(req.getRegisteredUserGrade())
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
			.installDetailAddress(request.getInstallDetailAddress())
			.installPhone(request.getInstallPhone())
			.installNumber(request.getInstallNumber())
			.installEmail(request.getInstallEmail())
			.installDescription(request.getInstallDescription())
			.requestDate(request.getRequestDate())
			.reservationFirstDate(request.getReservationFirstDate())
			.reservationSecondDate(request.getReservationSecondDate())
			.installStatus(request.getInstallStatus())
			.installNote(request.getInstallNote())
			.registeredUserGrade(request.getRegisteredUserGrade())
			.build();
	}

	// 유저 수정
	@Transactional
	public InstallDTO editInstallRequestByUser(Integer installId, InstallDTO installDTO, String providedPassword) {

		InstallRequest request = installRepository.findById(installId)
			.orElseThrow(() -> new ResourceNotFoundException("설치 신청을 찾을 수 없습니다."));

		if (providedPassword == null || !passwordEncoder.matches(providedPassword, request.getInstallPassword())) {
			throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
		}

		InstallRequest updatedRequest = request.toBuilder()
			.installName(installDTO.getInstallName())
			.installAddress(installDTO.getInstallAddress())
			.installDetailAddress(installDTO.getInstallDetailAddress())
			.installPhone(installDTO.getInstallPhone())
			.installNumber(installDTO.getInstallNumber())
			.installEmail(installDTO.getInstallEmail())
			.installPassword(passwordEncoder.encode(installDTO.getInstallPassword()))
			.installDescription(installDTO.getInstallDescription())
			.reservationFirstDate(installDTO.getReservationFirstDate())
			.reservationSecondDate(installDTO.getReservationSecondDate())
			.editTime(LocalDateTime.now())
			.build();

		InstallRequest saved = installRepository.save(updatedRequest);

		return InstallDTO.builder()
			.installId(saved.getInstallId())
			.installName(saved.getInstallName())
			.installAddress(saved.getInstallAddress())
			.installDetailAddress(saved.getInstallDetailAddress())
			.installPhone(saved.getInstallPhone())
			.installNumber(saved.getInstallNumber())
			.installEmail(saved.getInstallEmail())
			.installPassword(saved.getInstallPassword())
			.installDescription(saved.getInstallDescription())
			.requestDate(saved.getRequestDate())
			.reservationFirstDate(saved.getReservationFirstDate())
			.reservationSecondDate(saved.getReservationSecondDate())
			.installStatus(saved.getInstallStatus())
			.registeredUserGrade(saved.getRegisteredUserGrade())
			.build();
	}

	// 관리자용 수정 - 수정 가능한 필드는 설치 상태와 비고(installNote)만
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
			.installDetailAddress(updated.getInstallDetailAddress())
			.installPhone(updated.getInstallPhone())
			.installNumber(updated.getInstallNumber())
			.installEmail(updated.getInstallEmail())
			.installDescription(updated.getInstallDescription())
			.editTime(updated.getEditTime())
			.reservationFirstDate(updated.getReservationFirstDate())
			.reservationSecondDate(updated.getReservationSecondDate())
			.installStatus(updated.getInstallStatus())
			.installNote(updated.getInstallNote())
			.build();
	}

	// 삭제 - 관리자(ENGINEER, ADMIN, SUPERADMIN)만 삭제 가능
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
		request.setInstallStatus(Install.FALLSE);
		request.setCancelTime(LocalDateTime.now());
		installRepository.save(request);
	}
}