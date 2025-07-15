package com.jmair.notice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notice")
@Getter
@Setter
public class Notice {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer noticeId;
	@Column(nullable = false, length = 20)
	private String noticeTitle;
	@Column(nullable = false, length = 3000)
	private String noticeContent;
	@Column(nullable = false)
	private String noticeWriter;
	@CreationTimestamp
	@Column(nullable = false)
	private LocalDateTime noticePostTime;
	private LocalDateTime noticeEditTime;
	private LocalDateTime noticeDeleteTime;
	@Column(nullable = false)
	private boolean status;

}
