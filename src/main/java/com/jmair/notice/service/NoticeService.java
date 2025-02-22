package com.jmair.notice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jmair.notice.repository.NotiveRepository;

@Service
public class NoticeService {

	private final NotiveRepository	noticeRepository;

	@Autowired
	public NoticeService(NotiveRepository noticeRepository) {
		this.noticeRepository = noticeRepository;
	}
}
