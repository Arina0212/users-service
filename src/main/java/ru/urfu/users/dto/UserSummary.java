package ru.urfu.users.dto;


public class UserSummary {

    private final Long id;
    private final String firstname;
    private final String lastname;

    public UserSummary(Long id, String firstname, String lastname) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
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
}
