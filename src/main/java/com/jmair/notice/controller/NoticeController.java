package com.jmair.notice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jmair.common.exeption.ForbiddenException;
import com.jmair.common.exeption.ResourceNotFoundException;
import com.jmair.common.exeption.UnauthorizedException;
import com.jmair.notice.dto.NoticeDTO;
import com.jmair.notice.service.NoticeService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/notices")
public class NoticeController {

	private final NoticeService noticeService;
	private final Logger logger = LoggerFactory.getLogger(NoticeController.class);

	@Autowired
	public NoticeController(NoticeService noticeService) {
		this.noticeService = noticeService;
	}

	// 등록
	@PostMapping("/post")
	public ResponseEntity<?> createNotice(@Valid @RequestBody NoticeDTO noticeDTO) {
		try {
			NoticeDTO createdNotice = noticeService.createNotice(noticeDTO);
			return ResponseEntity.ok(createdNotice);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("공지사항 등록 중 오류가 발생했습니다.");
		}
	}

	// 전체 조회
	@GetMapping
	public ResponseEntity<?> getAllNotices() {
		try {
			List<NoticeDTO> notices = noticeService.getAllNotices();
			return ResponseEntity.ok(notices);
		} catch(Exception e) {
			logger.error("공지사항 조회 중 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("공지사항 조회 중 오류가 발생했습니다.");
		}
	}


	// 상세 조회
	@GetMapping("/{noticeId}")
	public ResponseEntity<?> getNoticeById(@PathVariable Integer noticeId) {
		try {
			NoticeDTO noticeDetail = noticeService.getDetailNotice(noticeId);
			return ResponseEntity.ok(noticeDetail);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(e.getMessage());
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("공지사항 상세 조회 중 오류가 발생했습니다.");
		}
	}

	// 수정
	@PutMapping("/{noticeId}/edit")
	public ResponseEntity<?> editNotice(@PathVariable Integer noticeId, @Valid @RequestBody NoticeDTO noticeDTO) {
		try {
			NoticeDTO updatedNotice = noticeService.editNotice(noticeId, noticeDTO);
			return ResponseEntity.ok(updatedNotice);
		} catch (ResourceNotFoundException | UnauthorizedException | ForbiddenException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(Exception e) {
			logger.error("공지사항 수정 중 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("공지사항 수정 중 오류가 발생했습니다.");
		}
	}

	// 삭제
	@DeleteMapping("/{noticeId}/delete")
	public ResponseEntity<?> deleteNotice(@PathVariable Integer noticeId) {
		try {
			noticeService.deleteNotice(noticeId);
			return ResponseEntity.ok("공지사항이 삭제되었습니다.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("공지사항 삭제 중 오류가 발생했습니다.");
		}
	}
}
