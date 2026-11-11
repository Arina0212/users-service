package ru.urfu.users.dto;

public class RoleResponse {

    private final Integer id;
    private final String name;

    public RoleResponse(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
