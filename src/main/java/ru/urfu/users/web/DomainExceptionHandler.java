package ru.urfu.users.web;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.urfu.users.dto.ApiResponse;
import ru.urfu.users.exception.ApiException;

/**
 * Логические ошибки: запрос корректный, но правило предметной области нарушено.
 * По требованию курса они всегда отдаются с HTTP 200, код и текст лежат в теле.
 */
@RestControllerAdvice
@Order(1)
public class DomainExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(ApiException exception) {
        return ResponseEntity.ok(ApiResponse.error(exception.code(), exception.getMessage()));
    }
}
