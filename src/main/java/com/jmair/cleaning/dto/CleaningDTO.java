package com.jmair.cleaning.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CleaningDTO {

	private Integer cleanId;
	@NotBlank(message = "이름은 필수입니다.")
	private String cleanName;
	@NotBlank(message = "핸드폰 번호는 필수입니다.")
	private String cleanNumber;
	@Email(message = "올바른 이메일 형식을 입력하세요.")
	private String cleanEmail;
	private String productType;
	@Size(max = 1000, message = "설명은 최대 1000자까지 입력 가능합니다.")
	private String cleanDescription;
	@NotBlank(message = "주소는 필수입니다.")
	private String cleanAdress;
	@NotBlank(message = "상세주소는 필수입니다.")
	private String cleanDetailAdress;
	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 4, max = 4, message = "비밀번호는 4자리 숫자로 해주세요.")
	private String cleanPassword;

	private String cleanFirstReservationTime;
	private String cleanSecondReservationTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime cleanStartTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime cleanEditTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime cleanEndTime;
	private CleanStatus cleanStatus;
	private UserGrade registeredUserGrade;
	@Size(max = 1000, message = "최대 1000자")
	private String cleanNote;
}
