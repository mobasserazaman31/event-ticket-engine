package com.mobasserazaman.event_ticket_engine.Controller;

import com.mobasserazaman.event_ticket_engine.DTO.TicketResponseDto;
import com.mobasserazaman.event_ticket_engine.Entity.Ticket;
import com.mobasserazaman.event_ticket_engine.Service.TicketService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponseDto> getAllTickets() {
        return ticketService.getAllTickets().stream().map(TicketResponseDto::fromEntity).toList();
    }

    @GetMapping("/booking/{bookingId}")
    public List<Ticket> getTicketsByBookingId(@PathVariable Long bookingId) {
        return ticketService.getTicketsByBookingId(bookingId);
    }

    @GetMapping("/seat/{seatId}")
    public List<Ticket> getTicketsBySeatId(@PathVariable Long seatId) {
        return ticketService.getTicketsBySeatId(seatId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket) {
        return ticketService.saveTicket(ticket);
    }
}
