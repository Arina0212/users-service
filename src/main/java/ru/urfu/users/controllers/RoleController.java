package ru.urfu.users.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.urfu.users.dto.ApiResponse;
import ru.urfu.users.dto.ItemsResponse;
import ru.urfu.users.dto.RoleRequest;
import ru.urfu.users.dto.RoleResponse;
import ru.urfu.users.security.JwtUser;
import ru.urfu.users.services.RoleService;
import ru.urfu.users.validators.RequestValidator;

/**
 * Справочник ролей и управление ролями. Права проверяет сервис, а не фильтр:
 * отказ должен быть логической ошибкой с кодом в теле.
 */
@RestController
public class RoleController {

    private final RequestValidator validator;
    private final RoleService roleService;

    public RoleController(RequestValidator validator, RoleService roleService) {
        this.validator = validator;
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public Mono<ApiResponse<ItemsResponse<RoleResponse>>> roles(@AuthenticationPrincipal Jwt jwt) {
        return roleService.getRoles(JwtUser.hasRole(jwt, "admin")).map(ApiResponse::ok);
    }

    @GetMapping("/users/{id}/roles")
    public Mono<ApiResponse<ItemsResponse<RoleResponse>>> userRoles(@PathVariable long id) {
        return roleService.getUserRoles(validator.validateUserId(id)).map(ApiResponse::ok);
    }

    @PutMapping("/users/{id}/roles/{role}")
    public Mono<ApiResponse<ItemsResponse<RoleResponse>>> grantByPath(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long id,
            @PathVariable String role
    ) {
        return roleService.addRole(
                        validator.validateUserId(id),
                        validator.validateRole(role),
                        JwtUser.hasRole(jwt, "admin"))
                .map(ApiResponse::ok);
    }

    @PostMapping("/users/{id}/roles")
    public Mono<ApiResponse<ItemsResponse<RoleResponse>>> grantByBody(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long id,
            @RequestBody(required = false) RoleRequest request
    ) {
        String role = validator.validateRole(request == null ? null : request.getRole());
        return roleService.addRole(
                        validator.validateUserId(id),
                        role,
                        JwtUser.hasRole(jwt, "admin"))
                .map(ApiResponse::ok);
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    public Mono<ApiResponse<ItemsResponse<RoleResponse>>> revoke(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long id,
            @PathVariable String role
    ) {
        return roleService.removeRole(
                        validator.validateUserId(id),
                        validator.validateRole(role),
                        JwtUser.hasRole(jwt, "admin"))
                .map(ApiResponse::ok);
    }
}
