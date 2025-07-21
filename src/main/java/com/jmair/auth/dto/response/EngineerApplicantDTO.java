package com.jmair.auth.dto.response;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class EngineerApplicantDTO {
    private String userLogin;
    private String userName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime appliedAt;
}
