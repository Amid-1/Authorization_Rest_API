-- src/main/resources/db/migration/V7__add_middle_name.sql
ALTER TABLE user_details
    ADD COLUMN middle_name VARCHAR(255);