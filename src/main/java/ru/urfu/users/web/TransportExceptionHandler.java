package ru.urfu.users.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import ru.urfu.users.dto.ApiResponse;

/**
 * Транспортные ошибки: запрос не удалось разобрать, маршрут не найден,
 * метод или тип содержимого не поддерживаются. Здесь допустимы 4xx и 5xx.
 */
@RestControllerAdvice
@Order(2)
public class TransportExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TransportExceptionHandler.class);

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiResponse<Void>> handleInput(ServerWebInputException exception) {
        String message = exception.getReason() == null ? "Некорректное тело или параметры запроса" : exception.getReason();
        return ResponseEntity.badRequest().body(ApiResponse.error("BAD_REQUEST", message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        HttpStatus resolved = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        String message = exception.getReason() == null ? resolved.getReasonPhrase() : exception.getReason();
        return ResponseEntity.status(resolved).body(ApiResponse.error(resolved.name(), message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleIntegrity(DataIntegrityViolationException exception) {
        log.warn("Нарушено ограничение базы: {}", exception.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("CONFLICT", "Данные конфликтуют с ограничениями базы"));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Throwable throwable) {
        log.error("Необработанная ошибка", throwable);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "Внутренняя ошибка сервиса"));
    }
}
