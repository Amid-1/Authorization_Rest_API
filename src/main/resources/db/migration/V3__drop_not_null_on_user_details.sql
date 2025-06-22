-- src/main/resources/db/migration/V3__drop_not_null_on_user_details.sql
ALTER TABLE user_details
    ALTER COLUMN first_name DROP NOT NULL,
ALTER COLUMN email      DROP NOT NULL;