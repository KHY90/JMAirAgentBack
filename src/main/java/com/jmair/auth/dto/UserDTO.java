package com.jmair.auth.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {

	@NotNull(message = "ID는 필수입니다.")
	private String userLogin;

	@NotBlank(message = "이름은 필수입니다.")
	private String userName;

	@NotBlank(message = "비밀번호는 필수입니다.")
	private String password;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime joinDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime deleteDate;

	private String phoneNumber;
	private String email;
	private boolean status;
	private UserGrade userGrade;
}

