package com.tournamentmanager.api.error;

import com.tournamentmanager.application.exception.AnalysisException;
import com.tournamentmanager.application.exception.LanguageModelException;
import com.tournamentmanager.application.exception.OpenDataUnavailableException;
import com.tournamentmanager.domain.exception.ConflictException;
import com.tournamentmanager.domain.exception.DomainValidationException;
import com.tournamentmanager.domain.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", "Revisa los datos enviados.", request, errors);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiError> handleDomainValidation(DomainValidationException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage(), request, ex.getErrors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", "El cuerpo JSON no es válido.", request, List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflicto", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(LanguageModelException.class)
    public ResponseEntity<ApiError> handleLlm(LanguageModelException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "Proveedor de IA", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(OpenDataUnavailableException.class)
    public ResponseEntity<ApiError> handleOpenData(OpenDataUnavailableException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Datos abiertos no disponibles", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(AnalysisException.class)
    public ResponseEntity<ApiError> handleAnalysis(AnalysisException ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error de análisis", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Intenta de nuevo más tarde.", request, List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message,
                                           HttpServletRequest request, List<String> validationErrors) {
        ApiError body = new ApiError(Instant.now(), status.value(), error, message,
                request.getRequestURI(), validationErrors);
        return ResponseEntity.status(status).body(body);
    }
}
