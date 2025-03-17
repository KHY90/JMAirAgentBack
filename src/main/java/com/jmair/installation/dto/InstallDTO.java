package com.jmair.installation.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jmair.auth.dto.UserDTO;
import com.jmair.auth.dto.UserGrade;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallDTO {
	private Integer installId;
	@NotBlank(message = "이름은 필수입니다.")
	private String installName;
	@NotBlank(message = "주소는 필수입니다.")
	private String installAddress;
	@NotBlank(message = "상세 주소는 필수입니다.")
	private String installDetailAddress;
	@NotBlank(message = "핸드폰 번호는 필수입니다.")
	private String installPhone;
	private String installNumber;
	@Email(message = "올바른 이메일 형식을 입력하세요.")
	private String installEmail;
	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 4, max =4, message = "비밀번호는 4자로 해주세요.")
	private String installPassword;
	@Size(max = 1000, message = "요청사항은 최대 1000자까지 입력 가능합니다.")
	private String installDescription;
	// 예약 신청 날짜
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime requestDate;
	// 예약 수정 날짜
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime editTime;
	// 취소 날짜
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime cancelTime;
	// 예약 희망 날짜
	private String reservationFirstDate;
	private String reservationSecondDate;
	private Install installStatus;
	// 관리자용 비고
	@Size(max = 1000, message = "비고는 최대 1000자까지 입력 가능합니다.")
	private String installNote;
	private UserGrade registeredUserGrade;
}
