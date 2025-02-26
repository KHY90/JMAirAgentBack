package com.jmair.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
	@NotBlank(message = "ID는 필수입니다.")
	private String userLogin;

	@NotBlank(message = "비밀번호는 필수입니다.")
	private String password;
}
