package ru.urfu.users.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApiException {

    public static final String CODE = "VALIDATION_ERROR";

    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, CODE, message);
    }

    public ValidationException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}
