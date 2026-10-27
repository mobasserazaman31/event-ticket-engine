package com.mobasserazaman.event_ticket_engine.DTO;

import java.math.BigDecimal;

import com.mobasserazaman.event_ticket_engine.Entity.Ticket;

public record TicketResponseDto(Long id, Long booking_id, Long seat_id, BigDecimal priceAtPurchase,
        Ticket.Status status) {

    public static TicketResponseDto fromEntity(Ticket ticket) {

        return new TicketResponseDto(ticket.getId(), ticket.getBooking().getId(), ticket.getSeat().getId(),
                ticket.getPriceAtPurchase(), ticket.getStatus());

    }

}
