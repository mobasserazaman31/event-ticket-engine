package com.mobasserazaman.event_ticket_engine.Service;

import com.mobasserazaman.event_ticket_engine.Entity.Ticket;
import com.mobasserazaman.event_ticket_engine.Repository.TicketRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> getTicketsByBookingId(Long bookingId) {
        return ticketRepository.findByBookingId(bookingId);
    }

    public List<Ticket> getTicketsBySeatId(Long seatId) {
        return ticketRepository.findBySeatId(seatId);
    }

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }
}
