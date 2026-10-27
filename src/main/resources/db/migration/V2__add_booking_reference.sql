ALTER TABLE bookings ADD COLUMN booking_reference VARCHAR(36);

UPDATE bookings
SET booking_reference = 'BK-' || id
WHERE booking_reference IS NULL;

ALTER TABLE bookings
    ALTER COLUMN booking_reference SET NOT NULL;

ALTER TABLE bookings
    ADD CONSTRAINT uq_bookings_booking_reference UNIQUE (booking_reference);