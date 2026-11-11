package ru.urfu.users.dao;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.urfu.users.domain.Role;
import ru.urfu.users.domain.User;

public interface UserRepository {

    Mono<User> createUser(User user);

    Mono<User> findByEmail(String email);

    Mono<User> findByPhone(String phone);

    Mono<User> findById(long id);

    Flux<User> findByIds(List<Long> ids);

    Flux<User> findByRole(String role, int limit, int offset);

    Mono<Long> countByRole(String role);

    Mono<User> updateUser(long id, String firstname, String lastname, String phone, String passwordHash);

    Mono<Void> addRole(long userId, String role);

    Mono<Void> removeRole(long userId, String role);

    Mono<Boolean> deleteUser(long id);

    Flux<Role> findRoles();

    Flux<Role> findRolesByUserId(long userId);

    Mono<Boolean> existsById(long id);
}
