package com.jmair.cleaning.dto;

public enum CleanStatus {
	REQUEST, // 예약 대기
	CANCEL, // 예약 취소
	RESERVATION, // 예약 확정
	COMPLETION, // 완료
	FALLSE // 신청 취소
}
