package com.digiteen.wallet.exception;

import com.digiteen.wallet.tracing.TraceContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientBalanceException.class)
    ResponseEntity<ApiError> insufficient(InsufficientBalanceException ex) {
        return error(HttpStatus.CONFLICT, "INSUFFICIENT_BALANCE", ex.getMessage());
    }

    @ExceptionHandler({ConflictException.class, IdempotencyConflictException.class})
    ResponseEntity<ApiError> conflict(RuntimeException ex) {
        return error(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ApiError> unauthorized(UnauthorizedException ex) {
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> badRequest(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code, message, TraceContext.currentTraceId()));
    }
}
