-- src/main/resources/db/migration/V4__create_roles_table.sql
CREATE TABLE roles (
                       id   BIGSERIAL     PRIMARY KEY,
                       name VARCHAR(255)  NOT NULL UNIQUE
);