package com.jmair.installation.dto;

public enum Install {
	REQUEST, // 예약 대기
	CANCEL, // 예약 취소
	RESERVATION, // 예약 확정
	COMPLETION, // 설치 완료
	FALLSE // 신청 취소
}
