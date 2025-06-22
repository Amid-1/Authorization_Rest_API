-- src/main/resources/db/migration/V5__create_users_roles_table.sql
CREATE TABLE users_roles (
                             user_id BIGINT NOT NULL,
                             role_id BIGINT NOT NULL,
                             CONSTRAINT pk_users_roles PRIMARY KEY (user_id, role_id),
                             CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
                             CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);