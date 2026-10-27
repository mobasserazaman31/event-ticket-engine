package com.mobasserazaman.event_ticket_engine.Service;

import com.mobasserazaman.event_ticket_engine.Entity.Event;
import com.mobasserazaman.event_ticket_engine.Entity.Seat;
import com.mobasserazaman.event_ticket_engine.Repository.EventRepository;
import com.mobasserazaman.event_ticket_engine.Repository.SeatRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public EventService(EventRepository eventRepository, SeatRepository seatRepository) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public Event saveEvent(Event event) {
        Event savedEvent = eventRepository.save(event);

        for (int i = 1; i <= savedEvent.getTotalSeats(); i++) {
            Seat seat = new Seat();
            seat.setEvent(savedEvent);
            seat.setSeatNumber(String.valueOf(i));
            seat.setPrice(savedEvent.getPrice());
            seat.setStatus(Seat.Status.AVAILABLE);
            seatRepository.save(seat);
        }

        return savedEvent;
    }
}
