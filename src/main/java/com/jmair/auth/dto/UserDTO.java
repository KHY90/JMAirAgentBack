package com.jmair.auth.dto;

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

	private String phoneNumber;
	private String email;
	private boolean status;
	private UserGrade userGrade;
}

