package com.jmair.installation.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.jmair.installation.dto.Install;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자
@AllArgsConstructor // 전체 생성자
@Builder
public class InstallRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer installId;
	@Column(nullable = false, length = 50)
	private String installName;
	@Column(nullable = false, length = 200)
	private String installAddress;
	@Column(nullable = false, length = 20)
	private String installPhone;
	private String installNumber;
	private String installEmail;
	@Column(nullable = false)
	private String installPassword;
	@Column(length = 1000)
	private String installDescription;
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime requestDate;
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime editTime;
	@Column(nullable = false)
	private String reservationFirstDate;
	@Column
	private String reservationSecondDate;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Install installStatus;
	private String installNote;
}
