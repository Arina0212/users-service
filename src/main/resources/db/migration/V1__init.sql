-- Схема базы users_db: пользователи, справочник ролей и выданные роли.

CREATE TABLE users (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    firstname      VARCHAR(100) NOT NULL,
    lastname       VARCHAR(100) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    phone_number   VARCHAR(16),
    password_hash  VARCHAR(100) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ,

    CONSTRAINT chk_users_firstname CHECK (btrim(firstname) <> ''),
    CONSTRAINT chk_users_lastname  CHECK (btrim(lastname) <> ''),
    CONSTRAINT chk_users_email     CHECK (email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
    CONSTRAINT chk_users_phone     CHECK (phone_number ~ '^\+?[0-9]{10,15}$')
);

-- Email сравнивается без учёта регистра и освобождается после мягкого удаления.
CREATE UNIQUE INDEX uq_users_email ON users (lower(email)) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_users_phone ON users (phone_number) WHERE phone_number IS NOT NULL AND deleted_at IS NULL;

CREATE TABLE roles (
    id    SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(32) NOT NULL,

    CONSTRAINT uq_roles_name  UNIQUE (name),
    CONSTRAINT chk_roles_name CHECK (btrim(name) <> '')
);

CREATE TABLE user_roles (
    user_id     BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id     SMALLINT    NOT NULL REFERENCES roles (id) ON DELETE RESTRICT,
    granted_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_role ON user_roles (role_id);

INSERT INTO roles (name) VALUES
    ('guest'),
    ('subscriber'),
    ('producer'),
    ('admin');
