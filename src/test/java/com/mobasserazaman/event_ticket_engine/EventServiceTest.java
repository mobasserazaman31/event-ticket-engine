package com.mobasserazaman.event_ticket_engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobasserazaman.event_ticket_engine.Entity.Event;
import com.mobasserazaman.event_ticket_engine.Entity.Seat;
import com.mobasserazaman.event_ticket_engine.Repository.EventRepository;
import com.mobasserazaman.event_ticket_engine.Repository.SeatRepository;
import com.mobasserazaman.event_ticket_engine.Service.EventService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EventServiceTest {

    @Test
    void saveEvent_shouldGenerateSeatsForEachSeatInTotalSeats() {
        EventRepository eventRepository = mock(EventRepository.class);
        SeatRepository seatRepository = mock(SeatRepository.class);
        EventService eventService = new EventService(eventRepository, seatRepository);

        Event event = new Event();
        event.setTitle("Summer Festival");
        event.setVenue("City Arena");
        event.setEventDate(LocalDateTime.now().plusDays(2));
        event.setPrice(new BigDecimal("50.00"));
        event.setTotalSeats(3);

        Event savedEvent = new Event();
        savedEvent.setId(10L);
        savedEvent.setTitle(event.getTitle());
        savedEvent.setVenue(event.getVenue());
        savedEvent.setEventDate(event.getEventDate());
        savedEvent.setPrice(event.getPrice());
        savedEvent.setTotalSeats(event.getTotalSeats());

        when(eventRepository.save(event)).thenReturn(savedEvent);
        when(seatRepository.save(any(Seat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.saveEvent(event);

        assertEquals(savedEvent.getId(), result.getId());

        ArgumentCaptor<Seat> seatCaptor = ArgumentCaptor.forClass(Seat.class);
        verify(seatRepository, times(3)).save(seatCaptor.capture());

        assertEquals("1", seatCaptor.getAllValues().get(0).getSeatNumber());
        assertEquals("2", seatCaptor.getAllValues().get(1).getSeatNumber());
        assertEquals("3", seatCaptor.getAllValues().get(2).getSeatNumber());
        assertEquals(new BigDecimal("50.00"), seatCaptor.getAllValues().get(0).getPrice());
        assertEquals(Seat.Status.AVAILABLE, seatCaptor.getAllValues().get(0).getStatus());
    }
}
