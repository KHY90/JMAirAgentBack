package com.jmair.as.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.jmair.as.dto.ASStatus;
import com.jmair.auth.dto.UserGrade;

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
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ASEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer asId;
	@Column(nullable = false, length = 50)
	private String asName;
	@Column(nullable = false, length = 20)
	private String asNumber;
	private String asEmail;
	private String productType;
	@Column(length = 1000)
	private String asDescription;
	@Column(nullable = false, length = 200)
	private String asAdress;
	@Column(nullable = false, length = 200)
	private String asDetailAdress;
	@Column(nullable = false)
	private String asPassword;
	@Column(nullable = false, length = 200)
	private String asFirstReservationTime;
	private String asSecondReservationTime;
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime asStartTime;
	private LocalDateTime asEditTime;
	private LocalDateTime asEndTime;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ASStatus asStatus;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserGrade registeredUserGrade;
	@Column(length = 1000)
	private String asNote;
}
