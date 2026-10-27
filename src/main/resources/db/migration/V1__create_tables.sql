CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER'
        CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    venue VARCHAR(255) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    total_seats INTEGER NOT NULL
);

CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    seat_number VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE', 'HELD', 'SOLD')),
    price NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_seats_event
        FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT uq_seat_per_event UNIQUE (event_id, seat_number)
);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('HELD', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_event
        FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    price_at_purchase NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('CONFIRMED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT fk_tickets_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_tickets_seat
        FOREIGN KEY (seat_id) REFERENCES seats(id),
    CONSTRAINT uq_ticket_seat UNIQUE (seat_id)
);

CREATE INDEX idx_seats_event_id ON seats(event_id);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_event_id ON bookings(event_id);
CREATE INDEX idx_tickets_booking_id ON tickets(booking_id);
