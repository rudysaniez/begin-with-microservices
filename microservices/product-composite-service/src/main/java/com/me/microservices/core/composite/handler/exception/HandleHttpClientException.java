package com.me.microservices.core.composite.handler.exception;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.handler.http.HttpErrorInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HandleHttpClientException {

	private final ObjectMapper jack;
	
	@Autowired
	public HandleHttpClientException(ObjectMapper jack) {
		this.jack = jack;
	}
	
	/**
	 * @param ex
	 * @return {@link RuntimeException}
	 */
	public Throwable handleHttpClientException(WebClientResponseException e) {
		
		switch(e.getStatusCode()) {
		
			case NOT_FOUND:
				return new NotFoundException(getMessage(e));
	
			case UNPROCESSABLE_ENTITY:
				return new InvalidInputException(getMessage(e));
				
			default:
				log.warn("Got a unexpected http error: {}", e.getStatusCode());
				log.warn("{}", e.getResponseBodyAsString());
				return e;
		}
	}
	
	/**
	 * @param ex
	 * @return the error message
	 */
	private String getMessage(WebClientResponseException ex) {
		
		try {
			HttpErrorInfo info = jack.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
			return info.getMessage();
		}
		catch(IOException io) {
			return ex.getMessage();
		}
	}
}
