package ru.urfu.users.mapper;

import org.springframework.stereotype.Component;
import ru.urfu.users.domain.Role;
import ru.urfu.users.domain.User;
import ru.urfu.users.dto.PublicUserResponse;
import ru.urfu.users.dto.RoleResponse;
import ru.urfu.users.dto.UserResponse;
import ru.urfu.users.dto.UserSummary;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPhone(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public PublicUserResponse toPublic(User user) {
        return new PublicUserResponse(user.getId(), user.getFirstname(), user.getLastname(), user.getCreatedAt());
    }

    public UserSummary toSummary(User user) {
        return new UserSummary(user.getId(), user.getFirstname(), user.getLastname());
    }

    public RoleResponse toRole(Role role) {
        return new RoleResponse(role.getId(), role.getName());
    }
}
