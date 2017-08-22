package com.myprojects.ticket.service.email;

import static com.myprojects.ticket.service.dao.config.TicketServiceConfig.getEmailThreadPoolSize;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.myprojects.ticket.service.beans.SeatHold;
import com.myprojects.ticket.service.enums.TicketServiceEvent;

/**
 * Asynchronous email sending utility for ticket-service
 */
public final class TicketServiceEmailUtility {
	private static final Logger LOGGER = Logger.getLogger(TicketServiceEmailUtility.class);
	private static ExecutorService EMAIL_THREADPOOL = Executors.newFixedThreadPool(getEmailThreadPoolSize());
	
	private TicketServiceEmailUtility() {
	}
	
	public static void sendEmail(SeatHold seatHold, TicketServiceEvent event) {
		try {
			EMAIL_THREADPOOL.submit(new TicketServiceEmailThread(seatHold, event));
		} catch (Exception e) {
			LOGGER.error("Error occurred while submitting job for sending email", e);
		}
	}
}
