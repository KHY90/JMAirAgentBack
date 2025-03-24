package com.jmair.as.controller;

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
import com.jmair.as.service.ASService;
import com.jmair.auth.entity.User;
import com.jmair.common.exeption.ForbiddenException;
import com.jmair.common.exeption.ResourceNotFoundException;
import com.jmair.common.exeption.UnauthorizedException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/service")
@RequiredArgsConstructor
public class ASController {

	private final ASService asService;

	@PostMapping("/post")
	public ResponseEntity<?> createASRequest(@Valid @RequestBody ASDTO dto) {
		try {
			ASDTO created = asService.createASRequest(dto);
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
	public ResponseEntity<?> getAllASRequests(
		@RequestParam(value = "name", required = false) String asName,
		@RequestParam(value = "phone", required = false) String asNumber) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Optional<User> currentUser = Optional.empty();
		if (auth != null && auth.getPrincipal() instanceof User) {
			currentUser = Optional.of((User) auth.getPrincipal());
		}
		try {
			List<ASDTO> list = asService.getAllASRequests(currentUser, asName, asNumber);
			return ResponseEntity.ok(list);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("세척 신청 조회 중 오류가 발생했습니다.");
		}
	}

	// 상세 조회
	@GetMapping("/{asId}")
	public ResponseEntity<?> getCleaningRequestDetail(
		@PathVariable Integer asId,
		@RequestParam(value = "password", required = false) String providedPassword) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Optional<User> currentUser = Optional.empty();
		if (auth != null && auth.getPrincipal() instanceof User) {
			currentUser = Optional.of((User) auth.getPrincipal());
		}
		try {
			ASDTO dto = asService.getASRequestDetail(asId, providedPassword, currentUser);
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
	@PostMapping("/user/{asId}")
	public ResponseEntity<?> getCleaningRequestDetailWithPassword(
		@PathVariable Integer asId,
		@RequestBody Map<String, String> requestBody
	) {
		String providedPassword = requestBody.get("password");
		if (providedPassword == null || providedPassword.isBlank()) {
			return ResponseEntity.badRequest().body("비밀번호는 필수입니다.");
		}
		try {
			ASDTO dto = asService.getASRequestDetail(asId, providedPassword, Optional.empty());
			return ResponseEntity.ok(dto);
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("AS 신청 상세 조회 중 오류가 발생했습니다.");
		}
	}

	// 일반 유저 수정
	@PutMapping("/user/{asId}/edit")
	public ResponseEntity<?> editASRequestForUser(
		@PathVariable Integer asId,
		@Valid @RequestBody ASDTO dto,
		@RequestParam("password") String providedPassword) {
		try {
			ASDTO updated = asService.editASRequestForUser(asId, dto, providedPassword);
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
	@PutMapping("/admin/{asId}/edit")
	public ResponseEntity<?> editCleaningRequestForAdmin(
		@PathVariable Integer asId,
		@Valid @RequestBody ASDTO dto) {
		try {
			ASDTO updated = asService.editASRequestForAdmin(asId, dto);
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
	@DeleteMapping("/{asId}/delete")
	public ResponseEntity<?> deleteASRequest(@PathVariable Integer asId) {
		try {
			asService.deleteASRequest(asId);
			return ResponseEntity.ok("세척 신청이 삭제되었습니다.");
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("세척 신청 삭제 중 오류가 발생했습니다.");
		}
	}
}
