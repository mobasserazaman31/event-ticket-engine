package com.mobasserazaman.event_ticket_engine.Entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true, length = 36)
    private String bookingReference;

    @Column(name = "keycloak_subject", nullable = false, length = 255)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * What it means: "Don't create a third, extra database table just to link these two. Look at the booking field inside the Ticket class—that field already owns and tracks this relationship."
     * Why it matters: It tells Hibernate to use the existing booking_id foreign key column inside the tickets table instead of generating an unnecessary booking_tickets join table.
     */

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    public enum Status {
        HELD,
        CONFIRMED,
        CANCELLED,
        EXPIRED
    }
}


/**
 *  BOOKING FLOW
Step 1: Hold Request (Create Booking)
Action: The user selects one or more seats on the frontend and clicks "Proceed to Checkout".

Backend Logic:

Validate that the requested seats (seatIds) are currently AVAILABLE.

Create a new Booking record with status HELD, setting an expiration time (expiresAt = now + 10 minutes).

Create associated Ticket entries linked to the booking and update the requested Seat statuses from AVAILABLE to HELD.

Calculate the totalAmount.

Step 2: Temporary Hold Window (10-Minute Lock)
Action: The user enters their payment details.

Backend/Database Logic: The seats are temporarily locked so no one else can book them. If the user completes payment within 10 minutes, move to Step 3. If they abandon checkout, a background job or scheduled task automatically releases the seats back to AVAILABLE and marks the booking as EXPIRED.

Step 3: Payment & Confirmation
Action: The user submits payment.

Backend Logic:

Process the payment gateway call.

If successful, update the Booking status from HELD to CONFIRMED.

Update all related Seat statuses from HELD to SOLD.

Update Ticket statuses to CONFIRMED.
*/


/**
 * 
Why use Redis?

1. Lightning-Fast Seat Holds & Concurrency (Distributed Locking)
Earlier, we talked about preventing double-booking using database pessimistic locks. While that works, if 50,000 people try to book tickets for a major concert at the exact same second, your SQL database will choke on connection threads.

The Redis Way: You can use Redis's atomic commands (like SETNX or Redlock) to temporarily lock a seat in memory before touching your main SQL database. Because Redis is single-threaded per core for command execution, it naturally serializes concurrent requests instantly without database overhead.

2. Automatic 10-Minute Hold Expirations (Using TTL)
Remember your 10-minute booking timer where uncompleted holds expire?

The Traditional Way: You have to write a heavy scheduled background job (@Scheduled in Spring Boot) that constantly queries your SQL database every minute: SELECT * FROM bookings WHERE status = 'HELD' AND expires_at < NOW(), and then loops through them to release seats. This wastes database CPU cycles.

The Redis Way: When a user holds a seat, you save a key in Redis like seat:hold:12 and set a TTL (Time-To-Live) of 600 seconds (10 minutes). Redis automatically deletes that key the second the timer hits zero. You can even listen to Redis expiration events to instantly trigger a backend event that frees up the seat in your SQL database!

3. High-Traffic Event Catalog Caching
Thousands of users might browse the Event list page at the same time. Instead of hitting your SQL database every single time someone loads the homepage, you store the event list in Redis cache.
 */

