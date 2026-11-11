package ru.urfu.users.dao;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;
import ru.urfu.users.domain.User;

@Component
public class UserRowMapper implements BiFunction<Row, RowMetadata, User> {

    @Override
    public User apply(Row row, RowMetadata metadata) {
        return new User(
                row.get("id", Long.class),
                row.get("firstname", String.class),
                row.get("lastname", String.class),
                row.get("email", String.class),
                row.get("phone_number", String.class),
                row.get("password_hash", String.class),
                toInstant(row.get("created_at")),
                toInstant(row.get("updated_at")),
                roles(row, metadata)
        );
    }

    private static List<String> roles(Row row, RowMetadata metadata) {
        if (!metadata.contains("roles")) {
            return List.of();
        }
        String csv = row.get("roles", String.class);
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .toList();
    }

    private static Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        throw new IllegalStateException("Неожиданный тип времени: " + value.getClass().getName());
    }
}
