package ru.urfu.users.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.urfu.users.dto.AuthResponse;
import ru.urfu.users.dto.LoginRequest;
import ru.urfu.users.dto.RegisterRequest;
import ru.urfu.users.services.AuthService;
import ru.urfu.users.validators.RequestValidator;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RequestValidator validator;
    private final AuthService authService;

    public AuthController(RequestValidator validator, AuthService authService) {
        this.validator = validator;
        this.authService = authService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponse>> register(@RequestBody(required = false) RegisterRequest request) {
        RegisterRequest valid = validator.validateRegister(request);
        return authService.register(valid)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PostMapping("/login")
    public Mono<AuthResponse> login(@RequestBody(required = false) LoginRequest request) {
        return authService.login(validator.validateLogin(request));
    }
}
