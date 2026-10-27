package com.mobasserazaman.event_ticket_engine.Repository;

import com.mobasserazaman.event_ticket_engine.Entity.Seat;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventId(Long eventId);
    List<Seat> findByEventIdAndStatus(Long eventId, Seat.Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select seat from Seat seat where seat.id in :seatIds order by seat.id")
    List<Seat> findAllByIdInForUpdate(@Param("seatIds") List<Long> seatIds);
}
