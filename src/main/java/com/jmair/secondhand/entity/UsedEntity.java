package com.jmair.secondhand.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.jmair.secondhand.dto.Used;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UsedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer usedId;
	@Column(nullable = false)
	private String usedName;
	@Column(nullable = false)
	private int usedCost;
	private String productType;
	// 제품 설명
	@Column(length = 2000)
	private String usedDescription;
	// 물건 제작연도
	private String usedYear;
	// 게시물 등록, 수정, 삭제 시간
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime usedStartTime;
	private LocalDateTime usedEditTime;
	private LocalDateTime usedEndTime;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Used usedState;
	private boolean status;
	@Size(max = 1000, message = "비고는 최대 1000자까지 입력 가능합니다.")
	private String usedNote;

}


