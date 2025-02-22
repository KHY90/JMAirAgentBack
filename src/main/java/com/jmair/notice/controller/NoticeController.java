package com.jmair.notice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jmair.notice.dto.NoticeDTO;
import com.jmair.notice.entity.Notice;
import com.jmair.notice.service.NoticeService;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

	private final NoticeService noticeService;

	@Autowired
	public NoticeController(NoticeService noticeService) {
		this.noticeService = noticeService;
	}

	@GetMapping
	public ResponseEntity<List<NoticeDTO>> getNotice(){
		List<Notice>
	}
}
