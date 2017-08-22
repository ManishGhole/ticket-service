package com.myprojects.ticket.service;

import static com.myprojects.ticket.service.dao.config.TicketServiceConfig.getSeatHoldExpiryTimerFrequency;

import java.util.Timer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.myprojects.ticket.service.dao.TicketServiceDAO;
import com.myprojects.ticket.service.timer.SeatHoldExpiryTimer;

/**
 * Main application class
 */
@SpringBootApplication
public class TicketSeviceApplication implements CommandLineRunner {
	private static final Logger LOGGER = Logger.getLogger(TicketSeviceApplication.class);
	
	@Autowired
	private TicketService service;
	
	@Autowired
	private TicketServiceDAO ticketServiceDAO;
	
	private SeatHoldExpiryTimer seatHoldExpiryTimer;

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(TicketSeviceApplication.class);
		app.run(args);
	}

	public void run(String... args) throws Exception {
		try {
			seatHoldExpiryTimer = new SeatHoldExpiryTimer(ticketServiceDAO);
			Timer timer = new Timer("SeatHoldExpiryTimer", true);
			timer.scheduleAtFixedRate(seatHoldExpiryTimer, 0, getSeatHoldExpiryTimerFrequency());
			LOGGER.info("********* TICKETS SERVICE STARTED *********");
			LOGGER.info("********* TICKETS AVAILABLE *************** : " + service.numSeatsAvailable());
		} catch (Exception e) {
			LOGGER.error(e);
			System.exit(1);
		}
	}
}
