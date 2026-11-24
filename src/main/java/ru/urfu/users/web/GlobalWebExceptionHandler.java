package ru.urfu.users.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import ru.urfu.users.dto.ApiResponse;

/**
 * Отвечает за ошибки, которые появляются до контроллера: неизвестный маршрут,
 * неподдерживаемый метод. Тело тоже в общем конверте.
 */
@Component
@Order(-2)
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalWebExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        if (!(throwable instanceof ResponseStatusException exception)) {
            log.error("Необработанная ошибка веб-слоя", throwable);
            return write(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Внутренняя ошибка сервиса"));
        }

        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        HttpStatus resolved = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        String message = exception.getReason() == null ? resolved.getReasonPhrase() : exception.getReason();
        return write(exchange, resolved, ApiResponse.error(resolved.name(), message));
    }

    private Mono<Void> write(ServerWebExchange exchange, HttpStatus status, ApiResponse<Void> body) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] payload;
        try {
            payload = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException exception) {
            return Mono.error(exception);
        }
        DataBuffer buffer = response.bufferFactory().wrap(payload);
        return response.writeWith(Mono.just(buffer));
    }
}
