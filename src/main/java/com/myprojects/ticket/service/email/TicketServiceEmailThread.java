package com.myprojects.ticket.service.email;

import org.apache.log4j.Logger;

import com.myprojects.ticket.service.beans.SeatHold;
import com.myprojects.ticket.service.enums.TicketServiceEvent;

/**
 * Thread class to form email body and send email<br>
 * OR Call some other API<br>
 * This task will be submitted to TicketServiceEmailUtility for non-blocking operation
 */
class TicketServiceEmailThread implements Runnable {
	
	private static final Logger LOGGER = Logger.getLogger(TicketServiceEmailThread.class);
	
	private SeatHold seatHold;
	private TicketServiceEvent event;
	
	public TicketServiceEmailThread(SeatHold seatHold, TicketServiceEvent event) {
		this.seatHold = seatHold;
		this.event = event;
	}

	public void run() {
		LOGGER.info(String.format("%s Notofication Email is been sent for seatHoldId '%d' to '%s'",
				event, seatHold.getSeatHoldId(), seatHold.getCustomerEmail()));
		// Based on TicketServiceEvent form a message body with desired details and send an email.
	}

}
