package com.me.work.example.handler.http;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.me.work.example.handler.exception.InvalidInputException;
import com.me.work.example.handler.exception.NotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class MainControllerExceptionHandler {

	/**
	 * @param request
	 * @param ex
	 * @return {@link HttpErrorInfo}
	 */
	@ResponseStatus(value=HttpStatus.NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
	public @ResponseBody HttpErrorInfo handlerNotFoundException(ServerHttpRequest request, Exception ex) {
		return createHttpErrorInfo(HttpStatus.NOT_FOUND, request, ex);
	}
	
	/**
	 * @param request
	 * @param ex
	 * @return {@link HttpErrorInfo}
	 */
	@ResponseStatus(value=HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(InvalidInputException.class)
	public @ResponseBody HttpErrorInfo handlerInvalidInputException(ServerHttpRequest request, Exception ex) {
		return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, ex);
	}
	
	/**
	 * @param httpStatus
	 * @param request
	 * @param ex
	 * @return {@link HttpErrorInfo}
	 */
	private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception ex) {
		
        final String path = request.getPath().pathWithinApplication().value();
        final String message = ex.getMessage();

        if(log.isDebugEnabled())
        	log.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);
        
        return new HttpErrorInfo(ZonedDateTime.now(), path, httpStatus, message);
    }
}
