package com.mobasserazaman.event_ticket_engine.DTO;

import com.mobasserazaman.event_ticket_engine.Entity.Seat;
import java.math.BigDecimal;

public record SeatResponse(
        Long id,
        Long eventId,
        String seatNumber,
        Seat.Status status,
        BigDecimal price
) {
    public static SeatResponse fromEntity(Seat seat) {
        if (seat == null) {
            return null;
        }

        Long eventId = seat.getEvent() != null ? seat.getEvent().getId() : null;

        return new SeatResponse(
                seat.getId(),
                eventId,
                seat.getSeatNumber(),
                seat.getStatus(),
                seat.getPrice()
        );
    }
}
