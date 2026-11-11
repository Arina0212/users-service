# User Service


## Запуск (Docker)

```bash
docker compose up --build -d
```

| Контейнер | Порт | Назначение |
|-----------|------|------------|
| `users-postgres` | `5433` | PostgreSQL (`users_db`) |
| `users-service` | `9000` | User Service |

Сервис: `http://localhost:9000`  
БД: `localhost:5433`, user/password `users_app`/`users_app`

При старте приложения Flyway применяет миграции (таблицы `users`, `roles`, `user_roles` + seed ролей).

Администратор: `admin@example.com` / `admin12345`, создаётся при первом старте.

Локальный запуск без Docker-образа сервиса (Postgres всё равно через compose):

```bash
docker compose up -d db
DB_R2DBC_URL=r2dbc:postgresql://localhost:5433/users_db \
DB_JDBC_URL=jdbc:postgresql://localhost:5433/users_db \
mvn spring-boot:run
```

Порт 5433 указан потому, что на 5432 обычно уже висит локальный PostgreSQL.

## API

| Method | Path | Auth | Описание |
|--------|------|------|----------|
| `POST` | `/auth/register` | нет | регистрация + роль `guest`, в ответе токен |
| `POST` | `/auth/login` | нет | вход, в ответе токен и профиль |
| `GET` | `/users/me` | Bearer JWT | свой профиль |
| `PATCH` | `/users/me` | Bearer JWT | обновить имя, телефон или пароль |
| `GET` | `/users/{id}` | нет | публичный профиль |
| `GET` | `/users?ids=1,2` | Bearer JWT | профили пачкой для ленты |
| `GET` | `/users?role=producer` | Bearer JWT | список авторов, `page` и `limit` |
| `GET` | `/users/{id}/roles` | Bearer JWT | роли пользователя |
| `GET` | `/roles` | JWT + `admin` | справочник ролей |
| `PUT` | `/users/{id}/roles/{role}` | JWT + `admin` | выдать роль |
| `DELETE` | `/users/{id}/roles/{role}` | JWT + `admin` | забрать роль |
| `DELETE` | `/users/{id}` | JWT + `admin` | удалить пользователя |

Публичный профиль открыт без токена для вызовов Messages Service.

### Регистрация

```http
POST /auth/register
Content-Type: application/json

{
  "firstname": "Изекиль",
  "lastname": "Моребыстропереплывающий",
  "email": "petr.fastSailing@example.com",
  "phone": "+79345633400",
  "password": "fastSailing123"
}
```

Ответ `201 Created`: токен и профиль с ролью `guest`.

### Вход

```http
POST /auth/login
Content-Type: application/json

{
  "email": "petr.fastSailing@example.com",
  "password": "fastSailing123"
}
```

JWT содержит `sub` (user id), claim `roles`, `iss` и живёт 1 час.

### Ошибки

```json
{ "code": "EMAIL_TAKEN", "message": "Этот email уже зарегистрирован" }
```

## Структура

```
users-service/
├── docker-compose.yml
├── Dockerfile
├── README.md
└── src/main/
    ├── java/ru/urfu/users/
    │   ├── config/       # Cors, Security, Database, AdminSeeder
    │   ├── controllers/  # Auth, User, Role
    │   ├── services/     # Auth, Token, User, Role
    │   ├── validators/   # RequestValidator
    │   ├── dao/          # UserRepository, R2dbcUserRepository, Constants
    │   ├── domain/       # User, Role, RoleCode
    │   ├── dto/          # запросы и ответы API
    │   ├── mapper/       # UserMapper
    │   ├── security/     # конвертер ролей, обработчики 401/403
    │   ├── exception/    # ApiException и наследники
    │   └── web/          # GlobalExceptionHandler
    └── resources/
        ├── application.yml
        └── db/migration/V1__init.sql
```
