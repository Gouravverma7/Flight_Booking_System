package com.flightbooking.exception;

import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.flightbooking.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingNotFound(
            BookingNotFoundException ex
    ) {

        return ResponseEntity
                .status(404)
                .body(
                        new ApiErrorResponse(
                                404,
                                "BOOKING_NOT_FOUND",
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(BookingOwnershipException.class)
    public ResponseEntity<ApiErrorResponse> handleOwnership(
            BookingOwnershipException ex
    ) {

        return ResponseEntity
                .status(403)
                .body(
                        new ApiErrorResponse(
                                403,
                                "BOOKING_ACCESS_DENIED",
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidState(
            InvalidBookingStateException ex
    ) {

        return ResponseEntity
                .badRequest()
                .body(
                        new ApiErrorResponse(
                                400,
                                "INVALID_BOOKING_STATE",
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .badRequest()
                .body(
                        new ApiErrorResponse(
                                400,
                                "VALIDATION_ERROR",
                                message
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex
    ) {

        return ResponseEntity
                .internalServerError()
                .body(
                        new ApiErrorResponse(
                                500,
                                "INTERNAL_SERVER_ERROR",
                                ex.getMessage()
                        )
                );
    }

}