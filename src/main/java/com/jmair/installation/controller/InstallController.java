package com.jmair.installation.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jmair.as.dto.ASDTO;
import com.jmair.auth.entity.User;
import com.jmair.common.exeption.ForbiddenException;
import com.jmair.common.exeption.ResourceNotFoundException;
import com.jmair.common.exeption.UnauthorizedException;
import com.jmair.installation.dto.InstallDTO;
import com.jmair.installation.service.InstallService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/install")
@RequiredArgsConstructor
public class InstallController {

	private static final Logger logger = LoggerFactory.getLogger(InstallController.class);
	private final InstallService installService;

	// 설치 신청 등록
	@PostMapping("/post")
	public ResponseEntity<?> createInstallRequest(@Valid @RequestBody InstallDTO dto) {
		try {
			InstallDTO created = installService.createInstallRequest(dto);
			return ResponseEntity.ok(created);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("에어컨 설치 신청 등록 중 오류가 발생했습니다.");
		}
	}

	// 전체 조회
	@GetMapping
	public ResponseEntity<?> getAllInstallRequests(
		@RequestParam(value = "name", required = false) String installName,
		@RequestParam(value = "phone", required = false) String installPhone,
		HttpServletRequest request) {
		try {
			// 현재 로그인한 사용자가 있다면 가져오기 (없으면 empty)
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Optional<User> currentUser = Optional.empty();
			if (auth != null && auth.getPrincipal() instanceof User) {
				currentUser = Optional.of((User) auth.getPrincipal());
			}
			List<InstallDTO> requests = installService.getAllInstallRequests(currentUser, installName, installPhone);
			return ResponseEntity.ok(requests);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(Exception e) {
			logger.error("에어컨 설치 신청 조회 중 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("에어컨 설치 신청 조회 중 오류가 발생했습니다.");
		}
	}

	// 상세 조회
	@GetMapping("/{installId}")
	public ResponseEntity<?> getInstallRequestDetail(
		@PathVariable Integer installId,
		@RequestParam(value = "password", required = false) String providedPassword,
		HttpServletRequest request) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Optional<User> currentUser = Optional.empty();
		if (auth != null && auth.getPrincipal() instanceof User) {
			currentUser = Optional.of((User) auth.getPrincipal());
		}
		try {
			InstallDTO dto = installService.getInstallRequestDetail(installId, providedPassword, currentUser);
			return ResponseEntity.ok(dto);
		} catch (UnauthorizedException | ForbiddenException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("설치 신청 상세 조회 중 오류가 발생했습니다.");
		}
	}

	// 유저 상세 조회
	@PostMapping("/user/{installId}")
	public ResponseEntity<?> getInstallRequestDetail(
		@PathVariable Integer installId,
		@RequestBody Map<String, String> requestBody
	) {
		String providedPassword = requestBody.get("password");
		if (providedPassword == null || providedPassword.isBlank()) {
			return ResponseEntity.badRequest().body("비밀번호는 필수입니다.");
		}
		try {
			InstallDTO dto = installService.getInstallRequestDetail(installId, providedPassword, Optional.empty());
			return ResponseEntity.ok(dto);
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("견적 신청 상세 조회 중 오류가 발생했습니다.");
		}
	}

	// 관리자 전용 수정
	@PutMapping("/{installId}/edit")
	public ResponseEntity<?> editInstallRequest(@PathVariable Integer installId,
		@Valid @RequestBody InstallDTO dto) {
		try {
			InstallDTO updated = installService.editInstallRequest(installId, dto);
			return ResponseEntity.ok(updated);
		} catch (UnauthorizedException | ForbiddenException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			logger.error("설치 신청 수정 중 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("설치 신청 수정 중 오류가 발생했습니다.");
		}
	}

	// 유저 수정
	@PutMapping("/{installId}/user/edit")
	public ResponseEntity<?> editInstallRequestByUser(
		@PathVariable Integer installId,
		@Valid @RequestBody InstallDTO dto,
		@RequestParam("password") String providedPassword) {
		try {
			InstallDTO updated = installService.editInstallRequestByUser(installId, dto, providedPassword);
			return ResponseEntity.ok(updated);
		} catch (UnauthorizedException | ForbiddenException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			logger.error("설치 신청 수정 중 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("설치 신청 수정 중 오류가 발생했습니다.");
		}
	}

	// 삭제
	@DeleteMapping("/{installId}/delete")
	public ResponseEntity<?> deleteInstallRequest(@PathVariable Integer installId) {
		try {
			installService.deleteInstallRequest(installId);
			return ResponseEntity.ok("설치 신청이 삭제되었습니다.");
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("설치 신청 삭제 중 오류가 발생했습니다.");
		}
	}
}
