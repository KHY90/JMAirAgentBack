package com.jmair.auth.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialDTO {

	@NotBlank(message = "ID는 필수입니다.")
	private String userLogin;
	private String userName;
	private String userEmail;
	private UserGrade userGrade;

}
