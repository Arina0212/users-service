package ru.urfu.users.dao;

import io.r2dbc.spi.R2dbcException;
import java.util.List;
import java.util.Locale;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.urfu.users.domain.Role;
import ru.urfu.users.domain.RoleCode;
import ru.urfu.users.domain.User;
import ru.urfu.users.exception.ConflictException;


@Repository
public class R2dbcUserRepository implements UserRepository {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;
    private final UserRowMapper rowMapper;

    public R2dbcUserRepository(
            DatabaseClient databaseClient,
            TransactionalOperator transactionalOperator,
            UserRowMapper rowMapper
    ) {
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.rowMapper = rowMapper;
    }

    @Override
    public Mono<User> createUser(User user) {
        GenericExecuteSpec spec = databaseClient.sql(Constants.INSERT_USER)
                .bind("firstname", user.getFirstname())
                .bind("lastname", user.getLastname())
                .bind("email", user.getEmail())
                .bind("passwordHash", user.getPasswordHash());
        spec = bindNullable(spec, "phone", user.getPhone());

        Mono<User> chain = spec.map(rowMapper)
                .one()
                .flatMap(created -> addRole(created.getId(), RoleCode.GUEST.value())
                        .then(findRolesByUserId(created.getId()).map(Role::getName).collectList())
                        .map(created::withRoles));

        return chain.as(transactionalOperator::transactional)
                .onErrorMap(R2dbcUserRepository::translate);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return databaseClient.sql(Constants.FIND_BY_EMAIL)
                .bind("email", email)
                .map(rowMapper)
                .one();
    }

    @Override
    public Mono<User> findByPhone(String phone) {
        return databaseClient.sql(Constants.FIND_BY_PHONE)
                .bind("phone", phone)
                .map(rowMapper)
                .one();
    }

    @Override
    public Mono<User> findById(long id) {
        return databaseClient.sql(Constants.FIND_BY_ID)
                .bind("id", id)
                .map(rowMapper)
                .one();
    }

    @Override
    public Flux<User> findByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Flux.empty();
        }
        GenericExecuteSpec spec = databaseClient.sql(Constants.findByIdsSql(ids.size()));
        for (int index = 0; index < ids.size(); index++) {
            spec = spec.bind("id" + index, ids.get(index));
        }
        return spec.map(rowMapper).all();
    }

    @Override
    public Flux<User> findByRole(String role, int limit, int offset) {
        return databaseClient.sql(Constants.FIND_BY_ROLE)
                .bind("role", role)
                .bind("limit", limit)
                .bind("offset", offset)
                .map(rowMapper)
                .all();
    }

    @Override
    public Mono<Long> countByRole(String role) {
        return databaseClient.sql(Constants.COUNT_BY_ROLE)
                .bind("role", role)
                .map((row, metadata) -> row.get(0, Long.class))
                .one()
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<User> updateUser(long id, String firstname, String lastname, String phone, String passwordHash) {
        GenericExecuteSpec spec = databaseClient.sql(Constants.UPDATE_USER).bind("id", id);
        spec = bindNullable(spec, "firstname", firstname);
        spec = bindNullable(spec, "lastname", lastname);
        spec = bindNullable(spec, "passwordHash", passwordHash);
        spec = phone == null
                ? spec.bindNull("phone", String.class)
                : spec.bind("phone", phone);

        return spec.map(rowMapper)
                .one()
                .flatMap(updated -> findRolesByUserId(id).map(Role::getName).collectList().map(updated::withRoles))
                .onErrorMap(R2dbcUserRepository::translate);
    }

    @Override
    public Mono<Void> addRole(long userId, String role) {
        return databaseClient.sql(Constants.ADD_ROLE)
                .bind("userId", userId)
                .bind("role", role)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Mono<Void> removeRole(long userId, String role) {
        return databaseClient.sql(Constants.REMOVE_ROLE)
                .bind("userId", userId)
                .bind("role", role)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Mono<Boolean> deleteUser(long id) {
        return databaseClient.sql(Constants.SOFT_DELETE_USER)
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .map(updated -> updated > 0);
    }

    @Override
    public Flux<Role> findRoles() {
        return databaseClient.sql(Constants.FIND_ROLES)
                .map((row, metadata) -> new Role(row.get("id", Integer.class), row.get("name", String.class)))
                .all();
    }

    @Override
    public Flux<Role> findRolesByUserId(long userId) {
        return databaseClient.sql(Constants.FIND_ROLES_BY_USER)
                .bind("userId", userId)
                .map((row, metadata) -> new Role(row.get("id", Integer.class), row.get("name", String.class)))
                .all();
    }

    @Override
    public Mono<Boolean> existsById(long id) {
        return databaseClient.sql(Constants.EXISTS_USER)
                .bind("id", id)
                .map((row, metadata) -> Boolean.TRUE.equals(row.get("existed", Boolean.class)))
                .one()
                .defaultIfEmpty(false);
    }

    private static GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, String value) {
        return value == null ? spec.bindNull(name, String.class) : spec.bind(name, value);
    }

    private static Throwable translate(Throwable throwable) {
        String sqlState = sqlState(throwable);
        if (!"23505".equals(sqlState)) {
            return throwable;
        }
        String message = String.valueOf(throwable.getMessage()).toLowerCase(Locale.ROOT);
        if (message.contains("email")) {
            return new ConflictException("EMAIL_TAKEN", "Этот email уже зарегистрирован");
        }
        if (message.contains("phone")) {
            return new ConflictException("PHONE_TAKEN", "Этот телефон уже зарегистрирован");
        }
        return new ConflictException("CONFLICT", "Пользователь с такими данными уже существует");
    }

    private static String sqlState(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof R2dbcException r2dbcException) {
                return r2dbcException.getSqlState();
            }
        }
        return null;
    }
}
