package com.mobasserazaman.event_ticket_engine.DTO;

import com.mobasserazaman.event_ticket_engine.Entity.Booking;
import java.math.BigDecimal;
import java.util.List;

public record BookingResponseDto(
		String userId,
		Long eventId,
		List<Long> ticketIds,
		BigDecimal totalAmount,
		Booking.Status status) {

	public static BookingResponseDto fromEntity(Booking booking) {
		List<Long> ticketIds = booking.getTickets().stream()
				.map(ticket -> ticket.getId())
				.toList();

		return new BookingResponseDto(
				booking.getUserId(),
				booking.getEvent().getId(),
				ticketIds,
				booking.getTotalAmount(),
				booking.getStatus());
	}
}