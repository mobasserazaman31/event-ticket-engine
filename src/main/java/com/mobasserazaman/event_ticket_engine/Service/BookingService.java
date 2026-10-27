package com.mobasserazaman.event_ticket_engine.Service;

import com.mobasserazaman.event_ticket_engine.DTO.BookingRequestDto;
import com.mobasserazaman.event_ticket_engine.DTO.BookingResponseDto;
import com.mobasserazaman.event_ticket_engine.Entity.Event;
import com.mobasserazaman.event_ticket_engine.Entity.Seat;
import com.mobasserazaman.event_ticket_engine.Repository.BookingRepository;
import com.mobasserazaman.event_ticket_engine.Repository.EventRepository;
import com.mobasserazaman.event_ticket_engine.Repository.SeatRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final StringRedisTemplate redisTemplate;
    private final BookingPaymentTransactionService paymentTransactionService;
    private static final DefaultRedisScript<Long> ACQUIRE_BULK_HOLDS_SCRIPT = new DefaultRedisScript<>(
            "for i, key in ipairs(KEYS) do " +
                    "   if redis.call('EXISTS', key) == 1 then return 0 end " +
                    "end " +
                    "for i, key in ipairs(KEYS) do " +
                    "   redis.call('SET', key, ARGV[1], 'EX', ARGV[2]) " +
                    "end " +
                    "return 1",
            Long.class);
    private static final DefaultRedisScript<Long> EXTEND_BULK_HOLDS_SCRIPT = new DefaultRedisScript<>(
            "for i, key in ipairs(KEYS) do " +
                    "   if redis.call('GET', key) ~= ARGV[1] then return 0 end " +
                    "end " +
                    "for i, key in ipairs(KEYS) do " +
                    "   redis.call('EXPIRE', key, ARGV[2]) " +
                    "end " +
                    "return 1",
            Long.class);
    private static final DefaultRedisScript<Long> DELETE_OWNED_HOLDS_SCRIPT = new DefaultRedisScript<>(
            "for i, key in ipairs(KEYS) do " +
                    "   if redis.call('GET', key) == ARGV[1] then redis.call('DEL', key) end " +
                    "end " +
                    "return 1",
            Long.class);

        public BookingService(SeatRepository seatRepository, BookingRepository bookingRepository,
            EventRepository eventRepository, StringRedisTemplate redisTemplate,
            BookingPaymentTransactionService paymentTransactionService) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.redisTemplate = redisTemplate;
        this.paymentTransactionService = paymentTransactionService;
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(BookingResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<BookingResponseDto> getBookingById(Long id) {
        return bookingRepository.findById(id).map(BookingResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByUserId(String userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByEventId(Long eventId) {
        return bookingRepository.findByEventId(eventId).stream()
                .map(BookingResponseDto::fromEntity)
                .toList();
    }

    @Transactional
    public Boolean createBooking(String keycloakSubject, BookingRequestDto request) {
        if (keycloakSubject == null || keycloakSubject.isBlank()) {
            throw new IllegalArgumentException("Authenticated Keycloak subject is required.");
        }
        Event event = eventRepository.findById(request.event_id())
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + request.event_id()));

        if (request.seatIds() == null || request.seatIds().isEmpty()
                || request.seatIds().stream().distinct().count() != request.seatIds().size()) {
            throw new IllegalArgumentException("Select one or more unique seats.");
        }

        List<Seat> seats = seatRepository.findAllById(request.seatIds());
        if (seats.size() != request.seatIds().size()) {
            throw new IllegalArgumentException("One or more seats do not exist.");
        }

        List<String> holdKeys = new ArrayList<>();
        for (Seat seat : seats) {
            if (!event.getId().equals(seat.getEvent().getId())) {
                throw new IllegalArgumentException(
                        "Seat " + seat.getId() + " does not belong to event " + event.getId());
            }
            holdKeys.add("event:" + event.getId() + ":seat:" + seat.getId() + ":hold");
        }

        long redisHoldTtlSeconds = 300; // 5 mins for Redis

        // 1. Attempt atomic bulk lock in Redis first
        Long acquired = redisTemplate.execute(
                ACQUIRE_BULK_HOLDS_SCRIPT,
                holdKeys,
                keycloakSubject,
                String.valueOf(redisHoldTtlSeconds));

        if (acquired == null || acquired == 0) {
            throw new IllegalStateException("One or more selected seats are currently on hold by another user.");
        }

        // 2. Now check DB status safely because nobody else can touch them
        try {
            for (Seat seat : seats) {
                if (seat.getStatus() != Seat.Status.AVAILABLE) {
                    throw new IllegalStateException(
                            "Seat number " + seat.getSeatNumber() + " is no longer available.");
                }
            }
        } catch (Exception ex) {
            // If DB check fails, release the locks we just acquired.
            deleteOwnedHolds(holdKeys, keycloakSubject);
            throw ex;
        }

        return true;

    }

    private void deleteOwnedHolds(List<String> holdKeys, String ownerId) {
        if (!holdKeys.isEmpty()) {
            redisTemplate.execute(DELETE_OWNED_HOLDS_SCRIPT, holdKeys, ownerId);
        }
    }

    public BookingResponseDto completePayment(String keycloakSubject, BookingRequestDto request) {
        if (keycloakSubject == null || keycloakSubject.isBlank()) {
            throw new IllegalArgumentException("Authenticated Keycloak subject is required.");
        }
        Event event = eventRepository.findById(request.event_id())
                .orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + request.event_id()));

        if (request.seatIds() == null || request.seatIds().isEmpty()
                || request.seatIds().stream().distinct().count() != request.seatIds().size()) {
            throw new IllegalArgumentException("Select one or more unique seats.");
        }

        // 2. Fetch seats and validate relational integrity
        List<Seat> seats = seatRepository.findAllById(request.seatIds());
        if (seats.size() != request.seatIds().size()) {
            throw new IllegalArgumentException("One or more seats do not exist.");
        }

        List<String> holdKeys = new ArrayList<>();
        List<Long> sortedSeatIds = request.seatIds().stream()
                .sorted()
                .toList();

        for (Seat seat : seats) {
            if (!event.getId().equals(seat.getEvent().getId())) {
                throw new IllegalArgumentException(
                        "Seat " + seat.getId() + " does not belong to event " + event.getId());
            }
            if (seat.getStatus() != Seat.Status.AVAILABLE || seat.getUserId() != null) {
                throw new IllegalStateException(
                        "Seat number " + seat.getSeatNumber() + " is no longer available.");
            }

        }

        for (Long seatId : sortedSeatIds) {
            holdKeys.add("event:" + event.getId() + ":seat:" + seatId + ":hold");
        }

        Long holdsExtended = redisTemplate.execute(
                EXTEND_BULK_HOLDS_SCRIPT,
                holdKeys,
                keycloakSubject,
                "180");
        if (holdsExtended == null || holdsExtended != 1L) {
            throw new IllegalStateException("One or more seat holds do not exist or have expired.");
        }

        BigDecimal totalAmount = seats.stream()
                .map(Seat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paymentStartedAt = System.nanoTime();
        boolean paymentAccepted;
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(10_000, 40_001));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            deleteOwnedHolds(holdKeys, keycloakSubject);
            throw new IllegalStateException("Payment processing was interrupted; seat holds were released.", e);
        }

        if (System.nanoTime() - paymentStartedAt > TimeUnit.SECONDS.toNanos(30)) {
            deleteOwnedHolds(holdKeys, keycloakSubject);
            throw new IllegalStateException(
                    "Payment processing exceeded the 30-second limit; seat holds were released.");
        }
        paymentAccepted = ThreadLocalRandom.current().nextDouble() < 0.70;

        if (!paymentAccepted) {
            deleteOwnedHolds(holdKeys, keycloakSubject);
            throw new RuntimeException("Payment processing failed. Your card was declined.");
        }

        String chargeReference = UUID.randomUUID().toString();
        try {
            BookingResponseDto booking = paymentTransactionService.persistPaidBooking(
                    keycloakSubject, event.getId(), request.seatIds(), totalAmount);
                deleteOwnedHolds(holdKeys, keycloakSubject);
            return booking;
        } catch (RuntimeException ex) {
            mockRefund(chargeReference, totalAmount);
                deleteOwnedHolds(holdKeys, keycloakSubject);
            throw new IllegalStateException(
                    "Payment was accepted, but one or more seats are no longer available. The mock payment was refunded.",
                    ex);
        }

    }

    private void mockRefund(String chargeReference, BigDecimal amount) {
        System.getLogger(BookingService.class.getName()).log(
                System.Logger.Level.INFO,
                "Mock refund issued for charge {0}, amount {1}",
                chargeReference,
                amount);
    }

}
