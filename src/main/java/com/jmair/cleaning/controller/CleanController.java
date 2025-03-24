package com.jmair.cleaning.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.jmair.cleaning.dto.CleaningDTO;
import com.jmair.cleaning.service.CleanService;
import com.jmair.common.exeption.ForbiddenException;
import com.jmair.common.exeption.ResourceNotFoundException;
import com.jmair.common.exeption.UnauthorizedException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clean")
@RequiredArgsConstructor
public class CleanController {

	private final CleanService cleanService;

	@PostMapping("/post")
	public ResponseEntity<?> createCleaningRequest(@Valid @RequestBody CleaningDTO dto) {
		try {
			CleaningDTO created = cleanService.createCleaningRequest(dto);
			return ResponseEntity.ok(created);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("에어컨 세척 신청 등록 중 오류가 발생했습니다.");
		}
	}

	// 전체 조회
	@GetMapping
	public ResponseEntity<?> getAllCleaningRequests(
		@RequestParam(value = "name", required = false) String cleanName,
		@RequestParam(value = "phone", required = false) String cleanNumber) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Optional<User> currentUser = Optional.empty();
		if (auth != null && auth.getPrincipal() instanceof User) {
			currentUser = Optional.of((User) auth.getPrincipal());
		}
		try {
			List<CleaningDTO> list = cleanService.getAllCleaningRequests(currentUser, cleanName, cleanNumber);
			return ResponseEntity.ok(list);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("세척 신청 조회 중 오류가 발생했습니다.");
		}
	}

	// 관리자 상세 조회
	@GetMapping("/{cleanId}")
	public ResponseEntity<?> getCleaningRequestDetail(
		@PathVariable Integer cleanId,
		@RequestParam(value = "password", required = false) String providedPassword) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Optional<User> currentUser = Optional.empty();
		if (auth != null && auth.getPrincipal() instanceof User) {
			currentUser = Optional.of((User) auth.getPrincipal());
		}
		try {
			CleaningDTO dto = cleanService.getCleaningRequestDetail(cleanId, providedPassword, currentUser);
			return ResponseEntity.ok(dto);
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("세척 신청 상세 조회 중 오류가 발생했습니다.");
		}
	}

	// 유저 상세 조회
	@PostMapping("/user/{cleanId}")
	public ResponseEntity<?> getCleaningRequestDetail(
		@PathVariable Integer cleanId,
		@RequestBody Map<String, String> requestBody
	){
		String providedPassword = requestBody.get("password");
		if (providedPassword == null || providedPassword.isBlank()) {
			return ResponseEntity.badRequest().body("비밀번호는 필수입니다.");
		}
		try {
			CleaningDTO dto = cleanService.getCleaningRequestDetail(cleanId, providedPassword, Optional.empty());
			return ResponseEntity.ok(dto);
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("세척 신청 상세 조회 중 오류가 발생했습니다.");
		}
	}

	// 일반 유저 수정
	@PutMapping("/user/{cleanId}/edit")
	public ResponseEntity<?> editCleaningRequestForUser(
		@PathVariable Integer cleanId,
		@Valid @RequestBody CleaningDTO dto,
		@RequestParam("password") String providedPassword) {
		try {
			CleaningDTO updated = cleanService.editCleaningRequestForUser(cleanId, dto, providedPassword);
			return ResponseEntity.ok(updated);
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("세척 신청 수정 중 오류가 발생했습니다.");
		}
	}

	// 관리자 수정
	@PutMapping("/admin/{cleanId}/edit")
	public ResponseEntity<?> editCleaningRequestForAdmin(
		@PathVariable Integer cleanId,
		@Valid @RequestBody CleaningDTO dto) {
		try {
			CleaningDTO updated = cleanService.editCleaningRequestForAdmin(cleanId, dto);
			return ResponseEntity.ok(updated);
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ForbiddenException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("세척 신청 수정 중 오류가 발생했습니다.");
		}
	}

	// 삭제
	@DeleteMapping("/{cleanId}/delete")
	public ResponseEntity<?> deleteCleaningRequest(@PathVariable Integer cleanId) {
		try {
			cleanService.deleteCleaningRequest(cleanId);
			return ResponseEntity.ok("세척 신청이 삭제되었습니다.");
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("세척 신청 삭제 중 오류가 발생했습니다.");
		}
	}

}
