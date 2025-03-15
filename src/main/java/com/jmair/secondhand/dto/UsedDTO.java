package com.jmair.secondhand.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jmair.installation.dto.Install;

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
public class UsedDTO {

	private Integer usedId;
	@NotBlank(message = "상품 이름은 필수입니다.")
	private String usedName;
	@NotBlank(message = "상품 가격은 필수입니다.")
	private String usedCost;
	private String productType;
	// 제품 설명
	private String usedDescription;
    // 물건 제작연도
	private String usedYear;
	// 물건 사용 시간
	private String usedTime;
	// 게시물 등록, 수정, 삭제 시간
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime usedPostTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime usedEditTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime usedEndTime;
	private Used usedState;
	@Size(max = 1000, message = "비고는 최대 1000자까지 입력 가능합니다.")
	private String usedNote;
	// 이미지
	private List<String> usedImages;
	private Integer registeredUserId;
}
