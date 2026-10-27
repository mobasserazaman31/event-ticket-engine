package com.mobasserazaman.event_ticket_engine.Controller;

import com.mobasserazaman.event_ticket_engine.DTO.SeatResponse;
import com.mobasserazaman.event_ticket_engine.Entity.Seat;
import com.mobasserazaman.event_ticket_engine.Service.SeatService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public List<SeatResponse> getAllSeats() {
        return seatService.getAllSeats().stream()
                .map(SeatResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/event/{eventId}")
    public List<SeatResponse> getSeatsByEventId(@PathVariable Long eventId) {
        return seatService.getSeatsByEventId(eventId).stream()
                .map(SeatResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> getSeatById(@PathVariable Long id) {
        return seatService.getSeatById(id)
                .map(SeatResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SeatResponse createSeat(@RequestBody Seat seat) {
        Seat savedSeat = seatService.saveSeat(seat);
        return SeatResponse.fromEntity(savedSeat);
    }
}
