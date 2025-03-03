package com.jmair.notice.controller;

import static org.springframework.data.jpa.domain.AbstractPersistable_.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jmair.notice.dto.NoticeDTO;
import com.jmair.notice.service.NoticeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/notices")
public class NoticeController {

	private final NoticeService noticeService;

	@Autowired
	public NoticeController(NoticeService noticeService) {
		this.noticeService = noticeService;
	}

	// 등록
	@PostMapping("/post")
	public ResponseEntity<?> createNotice(@Valid @RequestBody NoticeDTO noticeDTO) {
		NoticeDTO createdNotice = noticeService.createNotice(noticeDTO);
		return ResponseEntity.ok(createdNotice);
	}

	// 전체 조회
	@GetMapping
	public ResponseEntity<List<NoticeDTO>> getAllNotices() {
		List<NoticeDTO> notices = noticeService.getAllNotices();
		return ResponseEntity.ok(notices);
	}

	// 상세조회
	@GetMapping("/{noticeId}")
	public ResponseEntity<NoticeDTO> getNoticeById(@PathVariable Integer noticeId) {
		NoticeDTO noticeDetail = noticeService.getDetailNotice();
		return ResponseEntity.ok(noticeDetail);
	}

	// 수정
	@PutMapping("/{noticeId}/edit")
	public ResponseEntity<NoticeDTO> editNotice(@PathVariable Integer noticeId, @Valid @RequestBody NoticeDTO noticeDTO) {

	}

	// 삭제(소프트)
	@DeleteMapping("{noticeId}/delete")
}
