package com.jmair.notice.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Getter
@Setter
public class NoticeDTO {

	private Integer id;
	@NotBlank(message = "제목은 필수입니다.")
	@Size(max = 20, message = "제목은 20자 이내여야 합니다.")
	private String title;
	@NotBlank(message = "내용은 필수입니다.")
	@Size(max = 3000, message = "내용은 3000자 이내여야 합니다.")
	private String content;
	private String writer;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime postTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime editTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime deleteTime;
	private Boolean status;
}
