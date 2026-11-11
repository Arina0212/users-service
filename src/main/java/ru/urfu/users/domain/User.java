package ru.urfu.users.domain;

import java.time.Instant;
import java.util.List;

public class User {

    private final Long id;
    private final String firstname;
    private final String lastname;
    private final String email;
    private final String phone;
    private final String passwordHash;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<String> roles;

    public User(
            Long id,
            String firstname,
            String lastname,
            String email,
            String phone,
            String passwordHash,
            Instant createdAt,
            Instant updatedAt,
            List<String> roles
    ) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roles = roles == null ? List.of() : List.copyOf(roles);
    }

    public static User newUser(String firstname, String lastname, String email, String phone, String passwordHash) {
        return new User(null, firstname, lastname, email, phone, passwordHash, null, null, List.of());
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public User withRoles(List<String> roles) {
        return new User(id, firstname, lastname, email, phone, passwordHash, createdAt, updatedAt, roles);
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<String> getRoles() {
        return roles;
    }
}
