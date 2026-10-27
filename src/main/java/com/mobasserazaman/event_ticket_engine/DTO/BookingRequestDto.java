package com.mobasserazaman.event_ticket_engine.DTO;

import java.util.List;

public record BookingRequestDto(
        Long event_id,
        List<Long> seatIds
) {
}
