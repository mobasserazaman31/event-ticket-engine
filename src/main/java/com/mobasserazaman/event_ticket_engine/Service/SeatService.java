package com.mobasserazaman.event_ticket_engine.Service;

import com.mobasserazaman.event_ticket_engine.Entity.Seat;
import com.mobasserazaman.event_ticket_engine.Repository.SeatRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    public Optional<Seat> getSeatById(Long id) {
        return seatRepository.findById(id);
    }

    public List<Seat> getSeatsByEventId(Long eventId) {
        return seatRepository.findByEventId(eventId);
    }

    public Seat saveSeat(Seat seat) {
        return seatRepository.save(seat);
    }

}
