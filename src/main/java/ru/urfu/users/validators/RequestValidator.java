package ru.urfu.users.validators;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import ru.urfu.users.domain.RoleCode;
import ru.urfu.users.dto.LoginRequest;
import ru.urfu.users.dto.RegisterRequest;
import ru.urfu.users.dto.UserUpdateRequest;
import ru.urfu.users.exception.ValidationException;

@Component
public class RequestValidator {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{10,15}$");

    private static final int MAX_NAME = 100;
    private static final int MAX_EMAIL = 255;
    private static final int MIN_PASSWORD = 8;
    private static final int MAX_PASSWORD = 72;
    private static final int MAX_IDS = 100;
    private static final int MAX_LIMIT = 100;

    public RegisterRequest validateRegister(RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("Тело запроса обязательно");
        }
        return new RegisterRequest(
                name(request.getFirstname(), "firstname"),
                name(request.getLastname(), "lastname"),
                email(request.getEmail()),
                phone(request.getPhone()),
                password(request.getPassword())
        );
    }

    public LoginRequest validateLogin(LoginRequest request) {
        if (request == null) {
            throw new ValidationException("Тело запроса обязательно");
        }
        String email = email(request.getEmail());
        String password = password(request.getPassword());
        return new LoginRequest(email, password);
    }

    public UserUpdateRequest validateUpdate(UserUpdateRequest request) {
        if (request == null) {
            throw new ValidationException("Тело запроса обязательно");
        }
        boolean empty = request.getFirstname() == null
                && request.getLastname() == null
                && request.getPhone() == null
                && request.getPassword() == null;
        if (empty) {
            throw new ValidationException("Нужно передать хотя бы одно поле: firstname, lastname, phone, password");
        }
        return new UserUpdateRequest(
                request.getFirstname() == null ? null : name(request.getFirstname(), "firstname"),
                request.getLastname() == null ? null : name(request.getLastname(), "lastname"),
                request.getPhone() == null ? null : phoneForUpdate(request.getPhone()),
                request.getPassword() == null ? null : password(request.getPassword())
        );
    }

    private String phoneForUpdate(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : phone(trimmed);
    }

    public String validateRole(String role) {
        return RoleCode.from(role)
                .map(RoleCode::value)
                .orElseThrow(() -> new ValidationException(
                        "Неизвестная роль «" + (role == null ? "" : role) + "». "
                                + "Допустимые: guest, subscriber, producer, admin"));
    }

    public void validatePage(int page, int limit) {
        if (page < 1) {
            throw new ValidationException("page должен быть не меньше 1");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new ValidationException("limit должен быть в диапазоне 1.." + MAX_LIMIT);
        }
    }

    public long validateUserId(long userId) {
        if (userId < 1) {
            throw new ValidationException("id пользователя должен быть положительным");
        }
        return userId;
    }

    public List<Long> validateIds(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Параметр ids обязателен, например ids=2,3,7");
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            long id;
            try {
                id = Long.parseLong(trimmed);
            } catch (NumberFormatException exception) {
                throw new ValidationException("ids должен состоять из чисел: " + raw);
            }
            if (id < 1) {
                throw new ValidationException("id пользователя должен быть положительным: " + id);
            }
            ids.add(id);
        }
        if (ids.isEmpty()) {
            throw new ValidationException("Параметр ids не содержит ни одного идентификатора");
        }
        if (ids.size() > MAX_IDS) {
            throw new ValidationException("За один запрос можно запросить не более " + MAX_IDS + " пользователей");
        }
        return new ArrayList<>(ids);
    }

    private String name(String value, String field) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException("Поле " + field + " обязательно");
        }
        if (trimmed.length() > MAX_NAME) {
            throw new ValidationException("Поле " + field + " длиннее " + MAX_NAME + " символов");
        }
        return trimmed;
    }

    private String email(String value) {
        String trimmed = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            throw new ValidationException("Поле email обязательно");
        }
        if (trimmed.length() > MAX_EMAIL) {
            throw new ValidationException("email длиннее " + MAX_EMAIL + " символов");
        }
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new ValidationException("email не похож на адрес электронной почты");
        }
        return trimmed;
    }

    private String phone(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!PHONE.matcher(trimmed).matches()) {
            throw new ValidationException("Телефон должен содержать 10–15 цифр, допускается ведущий +");
        }
        return trimmed;
    }

    private String password(String value) {
        if (value == null || value.isEmpty()) {
            throw new ValidationException("Поле password обязательно");
        }
        if (value.length() < MIN_PASSWORD || value.length() > MAX_PASSWORD) {
            throw new ValidationException(
                    "Пароль должен быть длиной от " + MIN_PASSWORD + " до " + MAX_PASSWORD + " символов");
        }
        return value;
    }
}
