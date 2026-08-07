package io.github.humphreymahlangu.votetrust.exception;

import io.github.humphreymahlangu.votetrust.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateResource(
            DuplicateResourceException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, "Request conflicts with existing persisted data", request.getRequestURI());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(EligibilityException.class)
    ResponseEntity<ApiErrorResponse> handleEligibility(
            EligibilityException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(RegistrationClosedException.class)
    ResponseEntity<ApiErrorResponse> handleRegistrationClosed(
            RegistrationClosedException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(VotingClosedException.class)
    ResponseEntity<ApiErrorResponse> handleVotingClosed(
            VotingClosedException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidVotingCredentialException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidVotingCredential(
            InvalidVotingCredentialException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ResultsUnavailableException.class)
    ResponseEntity<ApiErrorResponse> handleResultsUnavailable(
            ResultsUnavailableException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request validation failed",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(response);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message, path));
    }
}
