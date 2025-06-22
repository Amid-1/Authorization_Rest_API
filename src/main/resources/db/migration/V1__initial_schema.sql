-- src/main/resources/db/migration/V1__initial_schema.sql
CREATE TABLE users (
                       id              SERIAL PRIMARY KEY,
                       username        VARCHAR(50) NOT NULL UNIQUE,
                       password        VARCHAR(255) NOT NULL,
                       created_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE user_details (
                              user_id         INTEGER       PRIMARY KEY
                                  REFERENCES users(id)
                                      ON DELETE CASCADE,
                              first_name      VARCHAR(100)  NOT NULL,
                              last_name       VARCHAR(100),
                              email           VARCHAR(150)  NOT NULL UNIQUE,
                              phone           VARCHAR(20),
                              created_at      TIMESTAMP     NOT NULL DEFAULT now()
);

-- Если есть ещё таблицы, виды, индексы, триггеры — все они тоже здесь:
CREATE INDEX idx_users_username ON users(username);