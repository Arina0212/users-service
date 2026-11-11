package ru.urfu.users.security;

import org.springframework.security.oauth2.jwt.Jwt;
import ru.urfu.users.exception.UnauthorizedException;

public final class JwtUser {

    private JwtUser() {
    }

    public static long id(Jwt jwt) {
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException | NullPointerException exception) {
            throw new UnauthorizedException("INVALID_TOKEN", "В токене нет корректного идентификатора пользователя");
        }
    }
}
