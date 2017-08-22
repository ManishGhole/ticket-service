package com.myprojects.ticket.service;

import com.myprojects.ticket.service.beans.SeatHold;
import com.myprojects.ticket.service.exception.TicketServiceException;
import com.myprojects.ticket.service.exception.InvalidSeatHoldException;
import com.myprojects.ticket.service.exception.ReservationTimeoutException;
import com.myprojects.ticket.service.exception.SeatHoldAlreadyConfirmedException;
import com.myprojects.ticket.service.exception.SeatHoldExpiredException;
import com.myprojects.ticket.service.exception.SeatHoldTimeoutException;
import com.myprojects.ticket.service.exception.SeatsUnavailableException;

public interface TicketService {
	/**
	 * The number of seats in the venue that are neither held nor reserved
	 *
	 * @return the number of tickets available in the venue
	 * @throws TicketServiceException
	 */
	int numSeatsAvailable() throws TicketServiceException;

	/**
	 * Find and hold the best available seats for a customer
	 *
	 * @param numSeats the number of seats to find and hold
	 * @param customerEmail unique identifier for the customer
	 * @return a SeatHold object identifying the specific seats and related information
	 * @throws SeatsUnavailableException
	 * @throws SeatHoldTimeoutException
	 * @throws TicketServiceException
	 */
	SeatHold findAndHoldSeats(int numSeats, String customerEmail)
			throws SeatsUnavailableException, SeatHoldTimeoutException, TicketServiceException;

	/**
	 * Commit seats held for a specific customer
	 * @param seatHoldId the seat hold identifier
	 * @param customerEmail the email address of the customer to which the seat hold is assigned
	 * @return
	 * @throws InvalidSeatHoldException
	 * @throws SeatHoldExpiredException
	 * @throws SeatHoldAlreadyConfirmedException
	 * @throws ReservationTimeoutException
	 * @throws TicketServiceException
	 */
	String reserveSeats(int seatHoldId, String customerEmail)
			throws InvalidSeatHoldException, SeatHoldExpiredException, SeatHoldAlreadyConfirmedException,
			ReservationTimeoutException, TicketServiceException;
}
