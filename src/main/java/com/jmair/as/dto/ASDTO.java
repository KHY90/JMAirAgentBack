package com.jmair.as.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jmair.auth.dto.UserGrade;
import com.jmair.cleaning.dto.CleanStatus;

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
public class ASDTO {

	private Integer asId;
	@NotBlank(message = "이름은 필수입니다.")
	private String asName;
	@NotBlank(message = "핸드폰 번호는 필수입니다.")
	private String asNumber;
	@Email(message = "올바른 이메일 형식을 입력하세요.")
	private String asEmail;
	private String productType;
	@Size(max = 1000, message = "설명은 최대 1000자까지 입력 가능합니다.")
	private String asDescription;
	@NotBlank(message = "주소는 필수입니다.")
	private String asAdress;
	@NotBlank(message = "상세주소는 필수입니다.")
	private String asDetailAdress;
	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 4, max = 4, message = "비밀번호는 4자리 숫자로 해주세요.")
	private String asPassword;
	private String asFirstReservationTime;
	private String asSecondReservationTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime asStartTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime asEditTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime asEndTime;
	private ASStatus asStatus;
	private UserGrade registeredUserGrade;
	@Size(max = 1000, message = "최대 1000자")
	private String asNote;
}
