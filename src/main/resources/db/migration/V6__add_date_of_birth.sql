-- src/main/resources/db/migration/V6__add_date_of_birth.sql
ALTER TABLE user_details
    ADD COLUMN date_of_birth DATE;
