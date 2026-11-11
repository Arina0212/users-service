package ru.urfu.users.dto;

import java.time.Instant;

public class PublicUserResponse {

    private final Long id;
    private final String firstname;
    private final String lastname;
    private final Instant createdAt;

    public PublicUserResponse(Long id, String firstname, String lastname, Instant createdAt) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.createdAt = createdAt;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
