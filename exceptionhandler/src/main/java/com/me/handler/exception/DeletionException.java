package com.me.handler.exception;

public class DeletionException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public DeletionException() {}
	
	public DeletionException(String message) {
		super(message);
	}
	
	public DeletionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DeletionException(Throwable cause) {
		super(cause);
	}
}
