package ru.urfu.users.config;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.urfu.users.dao.UserRepository;
import ru.urfu.users.domain.RoleCode;
import ru.urfu.users.domain.User;

@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AdminProperties properties;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(AdminProperties properties, UserRepository users, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        if (properties.getEmail() == null || properties.getPassword() == null) {
            log.warn("app.admin.email и app.admin.password не заданы, администратор не создан");
            return;
        }
        users.findByEmail(properties.getEmail())
                .hasElement()
                .flatMap(exists -> exists ? ensureAdminRole() : createAdmin())
                .doOnSuccess(ignored -> log.info("Администратор {} готов", properties.getEmail()))
                .onErrorResume(error -> {
                    log.error("Не удалось подготовить администратора", error);
                    return Mono.empty();
                })
                .block(Duration.ofSeconds(30));
    }

    private Mono<Void> createAdmin() {
        return Mono.fromCallable(() -> passwordEncoder.encode(properties.getPassword()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(hash -> users.createUser(User.newUser(
                        properties.getFirstname(),
                        properties.getLastname(),
                        properties.getEmail(),
                        null,
                        hash)))
                .flatMap(admin -> users.addRole(admin.getId(), RoleCode.ADMIN.value()))
                .then();
    }

    private Mono<Void> ensureAdminRole() {
        return users.findByEmail(properties.getEmail())
                .flatMap(admin -> admin.hasRole(RoleCode.ADMIN.value())
                        ? Mono.empty()
                        : users.addRole(admin.getId(), RoleCode.ADMIN.value()))
                .then();
    }
}
