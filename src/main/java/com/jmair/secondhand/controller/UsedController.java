package com.jmair.secondhand.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.jmair.auth.controller.UserController;
import com.jmair.auth.entity.User;
import com.jmair.common.exeption.ForbiddenException;
import com.jmair.common.exeption.ResourceNotFoundException;
import com.jmair.common.exeption.UnauthorizedException;
import com.jmair.secondhand.dto.UsedDTO;
import com.jmair.secondhand.service.UsedService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/used")
@RequiredArgsConstructor
public class UsedController {

	private final UsedService usedService;
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	// 등록
	@PostMapping("/admin/post")
	public ResponseEntity<?> createUsedRequest(@Valid @RequestBody UsedDTO dto) {
		try {
			UsedDTO created = usedService.createUsedRequest(dto);
			return ResponseEntity.ok(created);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (UnauthorizedException | ForbiddenException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (Exception e) {
			logger.error("중고 에어컨 등록 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("중고 에어컨 등록 중 오류가 발생했습니다.");
		}
	}

	// 전체 조회
	@GetMapping
	public ResponseEntity<?> getAllUsedRequests() {
		try {
			return ResponseEntity.ok(usedService.getAllUsedRequests());
		} catch (Exception e) {
			logger.error("중고 에어컨 전체 조회 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("중고 에어컨 조회 중 오류가 발생했습니다.");
		}
	}

	// 상세 조회
	@GetMapping("/{usedId}")
	public ResponseEntity<?> getUsedRequestDetail(@PathVariable Integer usedId) {
		try {
			UsedDTO dto = usedService.getUsedRequestDetail(usedId);
			return ResponseEntity.ok(dto);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			logger.error("중고 에어컨 상세 조회 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("중고 에어컨 상세 조회 중 오류가 발생했습니다.");
		}
	}

	// 관리자 수정
	@PutMapping("/{usedId}/edit")
	public ResponseEntity<?> updateUsedRequest(@PathVariable Integer usedId,
		@Valid @RequestBody UsedDTO dto) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Optional<User> currentUser = Optional.empty();
		if (auth != null && auth.getPrincipal() instanceof User) {
			currentUser = Optional.of((User) auth.getPrincipal());
		}
		try {
			UsedDTO updated = usedService.updateUsedRequest(usedId, dto, currentUser);
			return ResponseEntity.ok(updated);
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ForbiddenException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			logger.error("중고 에어컨 수정 중 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("중고 에어컨 수정 중 오류가 발생했습니다.");
		}
	}

	// 구매 요청
	@PutMapping("/{usedId}/sale")
	public ResponseEntity<?> updateUsedSaleRequest(@PathVariable Integer usedId,
		@Valid @RequestBody UsedDTO dto) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Optional<User> currentUser = Optional.empty();
		if (auth != null && auth.getPrincipal() instanceof User) {
			currentUser = Optional.of((User) auth.getPrincipal());
		}
		try {
			UsedDTO updated = usedService.updateUsedSaleRequest(usedId, dto, currentUser);
			return ResponseEntity.ok(updated);
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ForbiddenException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			logger.error("중고 에어컨 수정 중 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("중고 에어컨 수정 중 오류가 발생했습니다.");
		}
	}

	// 삭제
	@DeleteMapping("/{usedId}/delete")
	public ResponseEntity<?> deleteUsedRequest(@PathVariable Integer usedId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Optional<User> currentUser = Optional.empty();
		if (auth != null && auth.getPrincipal() instanceof User) {
			currentUser = Optional.of((User) auth.getPrincipal());
		}
		try {
			usedService.deleteUsedRequest(usedId, currentUser);
			return ResponseEntity.ok("중고 에어컨이 삭제되었습니다.");
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			logger.error("중고 에어컨 삭제 중 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("중고 에어컨 삭제 중 오류가 발생했습니다.");
		}
	}
}
