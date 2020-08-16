package com.me.work.example.handler.http;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class HttpErrorInfo {

	private final ZonedDateTime timestamp;
    private final String path;
    private final HttpStatus httpStatus;
    private final String message;
    
    public HttpErrorInfo() {
    	
    	this.timestamp = null;
    	this.path = null;
    	this.httpStatus = null;
    	this.message = null;
    }
}
