package com.mobasserazaman.event_ticket_engine.Controller;

import com.mobasserazaman.event_ticket_engine.DTO.BookingRequestDto;
import com.mobasserazaman.event_ticket_engine.DTO.BookingResponseDto;
import com.mobasserazaman.event_ticket_engine.Service.BookingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponseDto> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/my-bookings")
    public List<BookingResponseDto> getMyBookings(@AuthenticationPrincipal Jwt jwt) {
        return bookingService.getBookingsByUserId(jwt.getSubject());
    }

    @GetMapping("/event/{eventId}")
    public List<BookingResponseDto> getBookingsByEventId(@PathVariable Long eventId) {
        return bookingService.getBookingsByEventId(eventId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestBody BookingRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            bookingService.createBooking(jwt.getSubject(), request);
            return ResponseEntity.ok("You have 5 minutes to confirm your booking. Proceed to checkout to complete your payment.");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<?> confirmBooking(
            @RequestBody BookingRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            return ResponseEntity.ok(bookingService.completePayment(jwt.getSubject(), request));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }
}
