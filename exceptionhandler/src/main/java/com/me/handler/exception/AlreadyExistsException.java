package com.me.handler.exception;

public class AlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public AlreadyExistsException() {
		super();
	}
	
	public AlreadyExistsException(String message) {
		super(message);
	}
	
	public AlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public AlreadyExistsException(Throwable cause) {
		super(cause);
	}
}
