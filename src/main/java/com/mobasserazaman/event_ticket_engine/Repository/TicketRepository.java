package com.mobasserazaman.event_ticket_engine.Repository;

import com.mobasserazaman.event_ticket_engine.Entity.Ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByBookingId(Long bookingId);
    List<Ticket> findBySeatId(Long seatId);
    List<Ticket> findByStatus(Ticket.Status status);
}
