-- src/main/resources/db/migration/V2__make_user_details_nullable.sql
ALTER TABLE user_details
    ALTER COLUMN first_name DROP NOT NULL,
ALTER COLUMN email      DROP NOT NULL;