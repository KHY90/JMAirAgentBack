package com.jmair.installation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jmair.installation.dto.InstallDTO;
import com.jmair.installation.service.InstallService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/install")
public class InstallController {

	private final InstallService installService;

	public InstallController(InstallService installService) {
		this.installService = installService;
	}

	// 설치 신청 등록
	@PostMapping
	public ResponseEntity<?> createInstallRequest(@Valid @RequestBody InstallDTO dto) {
		try {
			InstallDTO created = installService.createInstallRequest(dto);
			return ResponseEntity.ok(created);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("에어컨 설치 신청 등록 중 오류가 발생했습니다.");
		}
	}

	// 전체 조회
	@GetMapping
	public ResponseEntity<?> getAllInstallRequests() {
		try {
			List<InstallDTO> requests = installService.getAllInstallRequests();
			return ResponseEntity.ok(requests);
		} catch (Exception e) {
			return ResponseEntity.status(500).body("에어컨 설치 신청 조회 중 오류가 발생했습니다.");
		}
	}
}
