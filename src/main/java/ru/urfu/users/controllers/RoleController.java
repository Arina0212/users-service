package ru.urfu.users.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.urfu.users.dto.ItemsResponse;
import ru.urfu.users.dto.RoleRequest;
import ru.urfu.users.dto.RoleResponse;
import ru.urfu.users.services.RoleService;
import ru.urfu.users.validators.RequestValidator;

@RestController
public class RoleController {

    private final RequestValidator validator;
    private final RoleService roleService;

    public RoleController(RequestValidator validator, RoleService roleService) {
        this.validator = validator;
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public Mono<ItemsResponse<RoleResponse>> roles() {
        return roleService.getRoles();
    }

    @GetMapping("/users/{id}/roles")
    public Mono<ItemsResponse<RoleResponse>> userRoles(@PathVariable long id) {
        return roleService.getUserRoles(validator.validateUserId(id));
    }

    @PutMapping("/users/{id}/roles/{role}")
    public Mono<ItemsResponse<RoleResponse>> grantByPath(@PathVariable long id, @PathVariable String role) {
        return roleService.addRole(validator.validateUserId(id), validator.validateRole(role));
    }

    @PostMapping("/users/{id}/roles")
    public Mono<ItemsResponse<RoleResponse>> grantByBody(
            @PathVariable long id,
            @RequestBody(required = false) RoleRequest request
    ) {
        String role = validator.validateRole(request == null ? null : request.getRole());
        return roleService.addRole(validator.validateUserId(id), role);
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    public Mono<ItemsResponse<RoleResponse>> revoke(@PathVariable long id, @PathVariable String role) {
        return roleService.removeRole(validator.validateUserId(id), validator.validateRole(role));
    }
}
