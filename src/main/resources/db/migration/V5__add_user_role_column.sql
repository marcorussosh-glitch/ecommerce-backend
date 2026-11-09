ALTER TABLE users
    ADD COLUMN user_role VARCHAR(30);

UPDATE users
SET user_role = 'USER';

ALTER TABLE users
    ALTER COLUMN user_role SET NOT NULL;
