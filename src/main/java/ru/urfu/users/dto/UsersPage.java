package ru.urfu.users.dto;

import java.util.List;

public class UsersPage {

    private final List<UserSummary> items;
    private final int page;
    private final int limit;

    public UsersPage(List<UserSummary> items, int page, int limit) {
        this.items = items == null ? List.of() : List.copyOf(items);
        this.page = page;
        this.limit = limit;
    }

    public List<UserSummary> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }
}
