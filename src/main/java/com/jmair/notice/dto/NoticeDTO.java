package com.jmair.notice.dto;

import java.time.LocalDateTime;

import com.jmair.notice.entity.Notice;

import lombok.Data;

public class NoticeDTO {

	@Data
	public static class Response {
		private Integer noticeId;
		private String title;
		private String content;
		private Notice.Status status;
		private String createdBy;
		private LocalDateTime createdAt;
		private LocalDateTime updatedAt;


		public Response(Notice notice) {
			this.noticeId = notice.getNoticeId();
			this.title = notice.getTitle();
			this.content = notice.getContent();
			this.status = notice.getStatus();
			this.createdBy = notice.getCreatedBy().getUsername();
			this.createdAt = notice.getCreatedAt();
			this.updatedAt = notice.getUpdatedAt();
		}
	}

	@Data
	public static class CreateRequest {
		private String title;
		private String content;
		private String status;
	}

	@Data
	public static class UpdateRequest {
		private String title;
		private String content;
		private String status;
	}

}
