package com.mobasserazaman.event_ticket_engine.Repository;

import com.mobasserazaman.event_ticket_engine.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
