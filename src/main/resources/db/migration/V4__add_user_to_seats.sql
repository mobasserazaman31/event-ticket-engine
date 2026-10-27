ALTER TABLE seats
    ADD COLUMN user_id BIGINT;

ALTER TABLE seats
    ADD CONSTRAINT fk_seats_user
        FOREIGN KEY (user_id) REFERENCES users(id);