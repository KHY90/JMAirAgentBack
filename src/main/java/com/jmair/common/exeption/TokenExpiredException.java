package com.jmair.common.exeption;

public class TokenExpiredException extends RuntimeException {
	public TokenExpiredException(String message) {
		super(message);
	}
}
