package com.jmair.common.exeption;

public class TokenInvalidException extends RuntimeException {
	public TokenInvalidException(String message) {
		super(message);
	}
}