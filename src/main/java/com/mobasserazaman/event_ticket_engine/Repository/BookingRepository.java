package com.mobasserazaman.event_ticket_engine.Repository;

import com.mobasserazaman.event_ticket_engine.Entity.Booking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(String userId);
    List<Booking> findByEventId(Long eventId);
}
