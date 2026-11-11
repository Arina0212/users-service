package ru.urfu.users.dao;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Constants {

    private Constants() {
    }

    private static final String USER_FIELDS = """
            u.id, u.firstname, u.lastname, u.email, u.phone_number, u.password_hash,
            u.created_at, u.updated_at,
            COALESCE((SELECT string_agg(r.name, ',' ORDER BY r.name)
                      FROM user_roles ur
                      JOIN roles r ON r.id = ur.role_id
                      WHERE ur.user_id = u.id), '') AS roles""";

    private static final String INSERTED_USER_FIELDS = """
            id, firstname, lastname, email, phone_number, password_hash, created_at, updated_at""";

    public static final String INSERT_USER = """
            INSERT INTO users (firstname, lastname, email, phone_number, password_hash)
            VALUES (:firstname, :lastname, :email, CAST(:phone AS VARCHAR), :passwordHash)
            RETURNING %s""".formatted(INSERTED_USER_FIELDS);

    public static final String FIND_BY_EMAIL = """
            SELECT %s
            FROM users u
            WHERE lower(u.email) = lower(:email) AND u.deleted_at IS NULL""".formatted(USER_FIELDS);

    public static final String FIND_BY_ID = """
            SELECT %s
            FROM users u
            WHERE u.id = :id AND u.deleted_at IS NULL""".formatted(USER_FIELDS);

    public static final String FIND_BY_PHONE = """
            SELECT %s
            FROM users u
            WHERE u.phone_number = :phone AND u.deleted_at IS NULL""".formatted(USER_FIELDS);

    private static final String FIND_BY_IDS_TEMPLATE = """
            SELECT %s
            FROM users u
            WHERE u.id IN (%s) AND u.deleted_at IS NULL
            ORDER BY u.id""";

    public static String findByIdsSql(int count) {
        String placeholders = IntStream.range(0, count)
                .mapToObj(index -> ":id" + index)
                .collect(Collectors.joining(", "));
        return FIND_BY_IDS_TEMPLATE.formatted(USER_FIELDS, placeholders);
    }

    public static final String FIND_BY_ROLE = """
            SELECT %s
            FROM users u
            WHERE u.deleted_at IS NULL
              AND EXISTS (SELECT 1
                          FROM user_roles ur
                          JOIN roles r ON r.id = ur.role_id
                          WHERE ur.user_id = u.id AND r.name = :role)
            ORDER BY u.id
            LIMIT :limit OFFSET :offset""".formatted(USER_FIELDS);

    public static final String COUNT_BY_ROLE = """
            SELECT count(*)
            FROM users u
            WHERE u.deleted_at IS NULL
              AND EXISTS (SELECT 1
                          FROM user_roles ur
                          JOIN roles r ON r.id = ur.role_id
                          WHERE ur.user_id = u.id AND r.name = :role)""";


    public static final String UPDATE_USER = """
            UPDATE users
            SET firstname   = COALESCE(CAST(:firstname AS VARCHAR), firstname),
                lastname    = COALESCE(CAST(:lastname AS VARCHAR), lastname),
                phone_number = CASE
                                   WHEN CAST(:phone AS VARCHAR) IS NULL THEN phone_number
                                   WHEN CAST(:phone AS VARCHAR) = '' THEN NULL
                                   ELSE CAST(:phone AS VARCHAR)
                               END,
                password_hash = COALESCE(CAST(:passwordHash AS VARCHAR), password_hash),
                updated_at  = now()
            WHERE id = :id AND deleted_at IS NULL
            RETURNING %s""".formatted(INSERTED_USER_FIELDS);

    public static final String SOFT_DELETE_USER = """
            UPDATE users
            SET deleted_at = now(), updated_at = now()
            WHERE id = :id AND deleted_at IS NULL""";

    public static final String EXISTS_USER = """
            SELECT EXISTS (SELECT 1 FROM users WHERE id = :id AND deleted_at IS NULL) AS existed""";

    public static final String FIND_ROLES = """
            SELECT id::int AS id, name FROM roles ORDER BY id""";

    public static final String FIND_ROLES_BY_USER = """
            SELECT r.id::int AS id, r.name
            FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = :userId
            ORDER BY r.name""";

    public static final String ADD_ROLE = """
            INSERT INTO user_roles (user_id, role_id)
            SELECT CAST(:userId AS BIGINT), r.id
            FROM roles r
            WHERE r.name = :role
            ON CONFLICT (user_id, role_id) DO NOTHING""";

    public static final String REMOVE_ROLE = """
            DELETE FROM user_roles ur
            USING roles r
            WHERE ur.role_id = r.id
              AND ur.user_id = CAST(:userId AS BIGINT)
              AND r.name = :role""";
}
