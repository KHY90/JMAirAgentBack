package com.jmair.cleaning.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.jmair.auth.dto.UserGrade;
import com.jmair.cleaning.dto.CleanStatus;

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
public class CleanEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer cleanId;
	@Column(nullable = false, length = 50)
	private String cleanName;
	@Column(nullable = false, length = 20)
	private String cleanNumber;
	private String cleanEmail;
	private String productType;
	@Column(length = 1000)
	private String cleanDescription;
	@Column(nullable = false, length = 200)
	private String cleanAdress;
	@Column(nullable = false, length = 200)
	private String cleanDetailAdress;

	@Column(nullable = false, length = 200)
	private String cleanFirstReservationTime;
	private String cleanSecondReservationTime;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime cleanStartTime;
	private LocalDateTime cleanEditTime;
	private LocalDateTime cleanEndTime;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CleanStatus cleanStatus;
	@Column(nullable = false)
	private String cleanPassword;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserGrade registeredUserGrade;
	@Column(length = 1000)
	private String cleanNote;
}
