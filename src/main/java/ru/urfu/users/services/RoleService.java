package ru.urfu.users.services;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.urfu.users.dao.UserRepository;
import ru.urfu.users.domain.Role;
import ru.urfu.users.domain.RoleCode;
import ru.urfu.users.dto.ItemsResponse;
import ru.urfu.users.dto.RoleResponse;
import ru.urfu.users.exception.NotFoundException;
import ru.urfu.users.exception.ValidationException;
import ru.urfu.users.mapper.UserMapper;

@Service
public class RoleService {

    private final UserRepository users;
    private final UserMapper mapper;

    public RoleService(UserRepository users, UserMapper mapper) {
        this.users = users;
        this.mapper = mapper;
    }

    public Mono<ItemsResponse<RoleResponse>> getRoles() {
        return toItems(users.findRoles());
    }

    public Mono<ItemsResponse<RoleResponse>> getUserRoles(long userId) {
        return users.existsById(userId)
                .flatMap(exists -> exists
                        ? toItems(users.findRolesByUserId(userId))
                        : Mono.error(notFound()));
    }

    public Mono<ItemsResponse<RoleResponse>> addRole(long userId, String role) {
        return users.existsById(userId)
                .flatMap(exists -> exists
                        ? users.addRole(userId, role).then(toItems(users.findRolesByUserId(userId)))
                        : Mono.error(notFound()));
    }

    public Mono<ItemsResponse<RoleResponse>> removeRole(long userId, String role) {
        if (RoleCode.GUEST.value().equals(role)) {
            return Mono.error(new ValidationException(
                    "GUEST_ROLE_REQUIRED", "Роль guest базовая, снять её нельзя"));
        }
        return users.existsById(userId)
                .flatMap(exists -> exists
                        ? users.removeRole(userId, role).then(toItems(users.findRolesByUserId(userId)))
                        : Mono.error(notFound()));
    }

    private Mono<ItemsResponse<RoleResponse>> toItems(Flux<Role> roles) {
        return roles.map(mapper::toRole).collectList().map(ItemsResponse::new);
    }

    private static NotFoundException notFound() {
        return new NotFoundException("USER_NOT_FOUND", "Пользователь не найден");
    }
}
