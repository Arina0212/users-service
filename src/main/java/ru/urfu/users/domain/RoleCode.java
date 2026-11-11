package ru.urfu.users.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum RoleCode {

    GUEST("guest"),
    SUBSCRIBER("subscriber"),
    PRODUCER("producer"),
    ADMIN("admin");

    private final String value;

    RoleCode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Optional<RoleCode> from(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(code -> code.value.equals(normalized))
                .findFirst();
    }
}
