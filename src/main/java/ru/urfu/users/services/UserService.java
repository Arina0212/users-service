package ru.urfu.users.services;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.urfu.users.dao.UserRepository;
import ru.urfu.users.dto.ItemsResponse;
import ru.urfu.users.dto.PublicUserResponse;
import ru.urfu.users.dto.UserResponse;
import ru.urfu.users.dto.UserSummary;
import ru.urfu.users.dto.UserUpdateRequest;
import ru.urfu.users.dto.UsersPage;
import ru.urfu.users.exception.NotFoundException;
import ru.urfu.users.mapper.UserMapper;


@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder, UserMapper mapper) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    public Mono<PublicUserResponse> getPublicProfile(long id) {
        return users.findById(id)
                .map(mapper::toPublic)
                .switchIfEmpty(Mono.error(notFound()));
    }

    public Mono<UserResponse> getMe(long id) {
        return users.findById(id)
                .map(mapper::toResponse)
                .switchIfEmpty(Mono.error(notFound()));
    }

    public Mono<UserResponse> updateMe(long id, UserUpdateRequest patch) {
        return users.findById(id)
                .switchIfEmpty(Mono.error(notFound()))
                .flatMap(existing -> {
                    if (patch.getPassword() == null) {
                        return users.updateUser(id, patch.getFirstname(), patch.getLastname(), patch.getPhone(), null);
                    }
                    return hashPassword(patch.getPassword())
                            .flatMap(hash -> users.updateUser(
                                    id, patch.getFirstname(), patch.getLastname(), patch.getPhone(), hash));
                })
                .switchIfEmpty(Mono.error(notFound()))
                .map(mapper::toResponse);
    }

    public Mono<UsersPage> findByRole(String role, int page, int limit) {
        int offset = (page - 1) * limit;
        return users.findByRole(role, limit, offset)
                .map(mapper::toSummary)
                .collectList()
                .map(items -> new UsersPage(items, page, limit));
    }

    public Mono<ItemsResponse<UserSummary>> findByIds(List<Long> ids) {
        return users.findByIds(ids)
                .map(mapper::toSummary)
                .collectList()
                .map(ItemsResponse::new);
    }

    public Mono<Void> deleteUser(long id) {
        return users.deleteUser(id)
                .flatMap(deleted -> deleted
                        ? Mono.<Void>empty()
                        : Mono.error(notFound()));
    }

    private Mono<String> hashPassword(String password) {
        return Mono.fromCallable(() -> passwordEncoder.encode(password))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static NotFoundException notFound() {
        return new NotFoundException("USER_NOT_FOUND", "Пользователь не найден");
    }
}
