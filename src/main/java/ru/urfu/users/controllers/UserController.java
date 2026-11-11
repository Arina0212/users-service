package ru.urfu.users.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.urfu.users.dto.ItemsResponse;
import ru.urfu.users.dto.PublicUserResponse;
import ru.urfu.users.dto.UserResponse;
import ru.urfu.users.dto.UserSummary;
import ru.urfu.users.dto.UserUpdateRequest;
import ru.urfu.users.dto.UsersPage;
import ru.urfu.users.security.JwtUser;
import ru.urfu.users.services.UserService;
import ru.urfu.users.validators.RequestValidator;

@RestController
public class UserController {

    private final RequestValidator validator;
    private final UserService userService;

    public UserController(RequestValidator validator, UserService userService) {
        this.validator = validator;
        this.userService = userService;
    }

    @GetMapping("/users/me")
    public Mono<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getMe(JwtUser.id(jwt));
    }

    @PatchMapping("/users/me")
    public Mono<UserResponse> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) UserUpdateRequest request
    ) {
        UserUpdateRequest valid = validator.validateUpdate(request);
        return userService.updateMe(JwtUser.id(jwt), valid);
    }

    @GetMapping("/users/{id}")
    public Mono<PublicUserResponse> profile(@PathVariable long id) {
        return userService.getPublicProfile(validator.validateUserId(id));
    }

    @GetMapping(value = "/users", params = "ids")
    public Mono<ItemsResponse<UserSummary>> byIds(@RequestParam String ids) {
        return userService.findByIds(validator.validateIds(ids));
    }

    @GetMapping(value = "/users", params = "role")
    public Mono<UsersPage> byRole(
            @RequestParam String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        validator.validatePage(page, limit);
        return userService.findByRole(validator.validateRole(role), page, limit);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable long id) {
        return userService.deleteUser(validator.validateUserId(id));
    }
}
