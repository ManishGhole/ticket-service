package com.myprojects.ticket.service;

import com.myprojects.ticket.service.beans.SeatHold;
import com.myprojects.ticket.service.exception.TicketServiceException;
import com.myprojects.ticket.service.exception.SeatsUnavailableException;

public interface TicketService {
	/**
	 * The number of seats in the venue that are neither held nor reserved
	 *
	 * @return the number of tickets available in the venue
	 */
	int numSeatsAvailable() throws TicketServiceException;

	/**
	 * Find and hold the best available seats for a customer
	 *
	 * @param numSeats the number of seats to find and hold
	 * @param customerEmail unique identifier for the customer
	 * @return a SeatHold object identifying the specific seats and related information
	 * @throws SeatsUnavailableException 
	 * @throws TicketsServiceException
	 */
	SeatHold findAndHoldSeats(int numSeats, String customerEmail) throws TicketServiceException, SeatsUnavailableException;

	/**
	 * Commit seats held for a specific customer
	 *
	 * @param seatHoldId the seat hold identifier
	 * @param customerEmail the email address of the customer to which the seat hold is assigned
	 * @return a service confirmation code
	 */
	String reserveSeats(int seatHoldId, String customerEmail) throws TicketServiceException, SeatsUnavailableException;
}