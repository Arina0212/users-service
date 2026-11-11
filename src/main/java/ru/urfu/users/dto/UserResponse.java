package ru.urfu.users.dto;

import java.time.Instant;
import java.util.List;

public class UserResponse {

    private final Long id;
    private final String firstname;
    private final String lastname;
    private final String email;
    private final String phone;
    private final List<String> roles;
    private final Instant createdAt;
    private final Instant updatedAt;

    public UserResponse(
            Long id,
            String firstname,
            String lastname,
            String email,
            String phone,
            List<String> roles,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phone = phone;
        this.roles = roles == null ? List.of() : List.copyOf(roles);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
