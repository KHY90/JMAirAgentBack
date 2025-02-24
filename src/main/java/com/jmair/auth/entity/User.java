package com.jmair.auth.entity;

import com.jmair.auth.dto.UserGrade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false)
	private Integer id;

	@Column(nullable = false)
	private String userLogin;

	@Column(nullable = false)
	private String userName;

	@Column(nullable = false)
	private String password;

	private String phoneNumber;
	private String email;

	@Column(nullable = false)
	private boolean status;

	@Column(nullable = false)
	private UserGrade userGrade;
}
