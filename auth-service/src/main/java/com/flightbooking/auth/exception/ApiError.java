package com.flightbooking.auth.exception;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ApiError {
	private LocalDateTime timeStamp;
	private int status;
	private String error;
	private String message;
	private String path;

	public ApiError(int status, String error, String message, String path) {
		this.timeStamp = LocalDateTime.now();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}


}
