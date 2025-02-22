package com.jmair.notice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jdk.jfr.Timestamp;

@Entity
public class Notice {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer noticeId;
	@Column(nullable = false)
	private String noticeTitle;
	@Column(nullable = false)
	private String noticeContent;
	@Timestamp
	@Column(nullable = false)
	private LocalDateTime noticePostTime;
	private LocalDateTime noticeDeleteTime;
	private boolean noticeStatus;


}
