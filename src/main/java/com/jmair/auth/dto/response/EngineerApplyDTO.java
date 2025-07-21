package com.jmair.auth.dto.response;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jmair.auth.dto.UserGrade;
import lombok.Data;

@Data
public class EngineerApplyDTO {
    private UserGrade userGrade;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime appliedAt;
}
