package ru.urfu.users.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.urfu.users.dao.UserRepository;
import ru.urfu.users.domain.User;
import ru.urfu.users.dto.AuthResponse;
import ru.urfu.users.dto.LoginRequest;
import ru.urfu.users.dto.RegisterRequest;
import ru.urfu.users.exception.ConflictException;
import ru.urfu.users.exception.UnauthorizedException;
import ru.urfu.users.mapper.UserMapper;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserMapper mapper;

    public AuthService(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            UserMapper mapper
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.mapper = mapper;
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        return ensureEmailFree(request.getEmail())
                .then(ensurePhoneFree(request.getPhone()))
                .then(Mono.defer(() -> hashPassword(request.getPassword())))
                .flatMap(hash -> users.createUser(User.newUser(
                        request.getFirstname(),
                        request.getLastname(),
                        request.getEmail(),
                        request.getPhone(),
                        hash
                )))
                .map(this::authResponse);
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return users.findByEmail(request.getEmail())
                .switchIfEmpty(Mono.error(invalidCredentials()))
                .flatMap(user -> matches(request.getPassword(), user.getPasswordHash())
                        .flatMap(matches -> matches
                                ? Mono.just(authResponse(user))
                                : Mono.error(invalidCredentials())));
    }

    private Mono<Void> ensureEmailFree(String email) {
        return users.findByEmail(email)
                .flatMap(existing -> Mono.<Void>error(
                        new ConflictException("EMAIL_TAKEN", "Этот email уже зарегистрирован")))
                .then();
    }

    private Mono<Void> ensurePhoneFree(String phone) {
        if (phone == null) {
            return Mono.empty();
        }
        return users.findByPhone(phone)
                .flatMap(existing -> Mono.<Void>error(
                        new ConflictException("PHONE_TAKEN", "Этот телефон уже зарегистрирован")))
                .then();
    }

    private Mono<String> hashPassword(String password) {
        return Mono.fromCallable(() -> passwordEncoder.encode(password))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Boolean> matches(String rawPassword, String passwordHash) {
        return Mono.fromCallable(() -> passwordEncoder.matches(rawPassword, passwordHash))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(
                tokenService.createToken(user),
                "Bearer",
                tokenService.ttlSeconds(),
                mapper.toResponse(user)
        );
    }

    private static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("INVALID_CREDENTIALS", "Неверный email или пароль");
    }
}
