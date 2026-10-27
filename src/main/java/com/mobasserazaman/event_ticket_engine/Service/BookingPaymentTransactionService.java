package com.mobasserazaman.event_ticket_engine.Service;

import com.mobasserazaman.event_ticket_engine.DTO.BookingResponseDto;
import com.mobasserazaman.event_ticket_engine.Entity.Booking;
import com.mobasserazaman.event_ticket_engine.Entity.Event;
import com.mobasserazaman.event_ticket_engine.Entity.Seat;
import com.mobasserazaman.event_ticket_engine.Entity.Ticket;
import com.mobasserazaman.event_ticket_engine.Repository.BookingRepository;
import com.mobasserazaman.event_ticket_engine.Repository.EventRepository;
import com.mobasserazaman.event_ticket_engine.Repository.SeatRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingPaymentTransactionService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public BookingPaymentTransactionService(
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            SeatRepository seatRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
        public BookingResponseDto persistPaidBooking(
            String keycloakSubject, Long eventId, List<Long> seatIds, BigDecimal totalAmount) {
        if (keycloakSubject == null || keycloakSubject.isBlank()) {
            throw new IllegalArgumentException("Authenticated Keycloak subject is required.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + eventId));

        List<Seat> seats = seatRepository.findAllByIdInForUpdate(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new IllegalArgumentException("One or more seats do not exist.");
        }

        for (Seat seat : seats) {
            if (!eventId.equals(seat.getEvent().getId())
                    || seat.getStatus() != Seat.Status.AVAILABLE
                    || seat.getUserId() != null) {
                throw new IllegalStateException("Seat number " + seat.getSeatNumber() + " is no longer available.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking();
        booking.setBookingReference(UUID.randomUUID().toString());
        booking.setUserId(keycloakSubject);
        booking.setEvent(event);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(Booking.Status.CONFIRMED);
        booking.setCreatedAt(now);

        List<Ticket> tickets = new ArrayList<>();
        for (Seat seat : seats) {
            seat.setStatus(Seat.Status.SOLD);
            seat.setUserId(keycloakSubject);

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeat(seat);
            ticket.setPriceAtPurchase(seat.getPrice());
            ticket.setStatus(Ticket.Status.CONFIRMED);
            tickets.add(ticket);
        }

        booking.getTickets().addAll(tickets);
        return BookingResponseDto.fromEntity(bookingRepository.saveAndFlush(booking));
    }
}

/**
 * Separation of Concerns: You kept the slow external payment simulation outside of the database transaction, preventing connection pool exhaustion.  
 *  Distributed Locking with Redis: You used atomic Lua scripts (ACQUIRE_BULK_HOLDS_SCRIPT, EXTEND_BULK_HOLDS_SCRIPT, DELETE_OWNED_HOLDS_SCRIPT) to prevent race conditions during seat selection and checkout extension.   
 * Database as the Ultimate Source of Truth: Your final persistence step uses pessimistic locking (findAllByIdInForUpdate) to guarantee absolute safety when writing to PostgreSQL.   
 */