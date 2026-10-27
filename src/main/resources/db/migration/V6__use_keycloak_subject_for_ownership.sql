DELETE FROM tickets;
DELETE FROM bookings;

UPDATE seats
SET status = 'AVAILABLE', user_id = NULL;

ALTER TABLE bookings
    DROP CONSTRAINT fk_bookings_user;

ALTER TABLE seats
    DROP CONSTRAINT fk_seats_user;

DROP INDEX IF EXISTS idx_bookings_user_id;

ALTER TABLE bookings
    RENAME COLUMN user_id TO keycloak_subject;

ALTER TABLE bookings
    ALTER COLUMN keycloak_subject TYPE VARCHAR(255)
    USING keycloak_subject::text;

ALTER TABLE seats
    RENAME COLUMN user_id TO keycloak_subject;

ALTER TABLE seats
    ALTER COLUMN keycloak_subject TYPE VARCHAR(255)
    USING keycloak_subject::text;

CREATE INDEX idx_bookings_keycloak_subject ON bookings(keycloak_subject);

DROP TABLE users;