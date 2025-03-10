package com.jmair.auth.dto;

public enum UserGrade {
	SUPERADMIN, // 전체 관리자
	ADMIN, // 웹 관리자
	ADMINWATCHER, // 어드민 관찰 가능
	ENGINEER, // 설치 기사
	USER, // 회원
	NOUSER // 비회원
}
