package com.mobasserazaman.event_ticket_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventTicketEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventTicketEngineApplication.class, args);
	}

}
