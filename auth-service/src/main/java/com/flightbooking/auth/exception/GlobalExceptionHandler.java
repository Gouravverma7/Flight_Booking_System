package com.flightbooking.auth.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request){
		ApiError error = new ApiError(409, "Conflict", ex.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiError> handlerUnauthorized(UnauthorizedException ex, HttpServletRequest request){
		ApiError error = new ApiError(401, "Unauthorized", ex.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request){
		Map<String, String> validationErrors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));
		ApiError error = new ApiError(400, "Validation Failed", validationErrors.toString(), request.getRequestURI());
		return ResponseEntity.badRequest().body(error);
 	}


	public ResponseEntity<ApiError> handleGeneral(Exception ex, HttpServletRequest request){
		ApiError error = new ApiError(500, "Internal Server Error", "Something went wrong", request.getRequestURI());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}





















}
