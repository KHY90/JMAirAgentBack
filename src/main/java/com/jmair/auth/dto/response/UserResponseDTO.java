package com.jmair.auth.dto.response;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jmair.auth.dto.UserGrade;
import lombok.Data;

@Data
public class UserResponseDTO {
    private String userLogin;
    private String userName;
    private String email;
    private String phoneNumber;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime joinDate;
    private UserGrade userGrade;
    private boolean status;
}
