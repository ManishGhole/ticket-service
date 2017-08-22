package com.myprojects.ticket.service;

import static com.myprojects.ticket.service.TicketServiceLocks.getConfirmReservationLock;
import static com.myprojects.ticket.service.TicketServiceLocks.getHoldReservationLock;
import static com.myprojects.ticket.service.dao.config.TicketServiceConfig.getConfirmReservationTimeout;
import static com.myprojects.ticket.service.dao.config.TicketServiceConfig.getHoldReservationTimeout;
import static com.myprojects.ticket.service.dao.config.TicketServiceConfig.getMaxSeatsRequest;
import static com.myprojects.ticket.service.utils.TicketServiceUtils.isValidEmail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.myprojects.ticket.service.beans.Seat;
import com.myprojects.ticket.service.beans.SeatHold;
import com.myprojects.ticket.service.dao.TicketServiceDAO;
import com.myprojects.ticket.service.email.TicketServiceEmailUtility;
import com.myprojects.ticket.service.enums.TicketServiceErrorType;
import com.myprojects.ticket.service.enums.TicketServiceEvent;
import com.myprojects.ticket.service.exception.InvalidSeatHoldException;
import com.myprojects.ticket.service.exception.ReservationTimeoutException;
import com.myprojects.ticket.service.exception.SeatHoldAlreadyConfirmedException;
import com.myprojects.ticket.service.exception.SeatHoldExpiredException;
import com.myprojects.ticket.service.exception.SeatHoldTimeoutException;
import com.myprojects.ticket.service.exception.SeatsUnavailableException;
import com.myprojects.ticket.service.exception.TicketServiceException;

@Component
final class TicketServiceImpl implements TicketService {

	private static final Logger LOGGER = Logger.getLogger(TicketServiceImpl.class);

	@Autowired
	private TicketServiceDAO ticketServiceDAO;

	public void setTicketServiceDAO(TicketServiceDAO ticketServiceDAO) {
		this.ticketServiceDAO = ticketServiceDAO;
	}

	public int numSeatsAvailable() throws TicketServiceException {
		List<Seat> availSeatsList = ticketServiceDAO.getAvailSeats();
		return availSeatsList.size();
	}

	public SeatHold findAndHoldSeats(final int numSeats, final String customerEmail)
			throws SeatsUnavailableException, SeatHoldTimeoutException, TicketServiceException {

		if (numSeats < 1 || numSeats > getMaxSeatsRequest()) {
			throw new TicketServiceException(
					String.format("Invalid number of seats requested, it should minimum 1 or maximum %d",
							getMaxSeatsRequest()),
					TicketServiceErrorType.INVALID_NUM_SEATS);
		}
		if (!isValidEmail(customerEmail)) {
			throw new TicketServiceException("Invalid email: " + customerEmail, TicketServiceErrorType.INVALID_EMAIL);
		}

		List<Seat> availSeats = ticketServiceDAO.getAvailSeats();

		List<Seat> holdSeats = null;
		int numAvailSeats = 0;
		boolean holdSuccess = false;

		try {
			Collections.sort(availSeats);
			if (getHoldReservationLock().tryLock(getHoldReservationTimeout(), TimeUnit.MILLISECONDS)) {
				numAvailSeats = availSeats.size();
				if (numAvailSeats == 0) {
					throw new SeatsUnavailableException("Show is housefull for the day",
							TicketServiceErrorType.SHOW_HOUSEFULL);
				}
				if (numAvailSeats < numSeats) {
					throw new SeatsUnavailableException(String.format("Only seats %d are remaining", numAvailSeats));
				}

				holdSeats = availSeats.subList(0, numSeats);
				holdSuccess = holdSeats(holdSeats);
			} else {
				throw new SeatHoldTimeoutException("Unable to hold seats");
			}
		} catch (InterruptedException e) {
			throw new SeatHoldTimeoutException(
					String.format("Seat hold timeout occurred: %d seconds", getHoldReservationTimeout()), e);
		} finally {
			try {
				getHoldReservationLock().unlock();
			} catch (IllegalMonitorStateException e) {
			} catch (Exception e) {
				LOGGER.error("Error occurred while release getHoldReservationLock: " + e.getMessage());
			}
		}

		if (!holdSuccess) {
			throw new TicketServiceException("Seat hold unsuccessful", TicketServiceErrorType.SEAT_HOLD_FAILED);
		}
		SeatHold seatHold = new SeatHold(customerEmail, holdSeats);
		ticketServiceDAO.putHoldReservation(seatHold);
		TicketServiceEmailUtility.sendEmail(seatHold, TicketServiceEvent.HOLD_RESERVATION);
		return seatHold;
	}

	/**
	 * Holds seats for requested SeatHold reservation
	 * 
	 * @param holdSeats
	 * @return boolean
	 * @throws TicketServiceException
	 */
	protected boolean holdSeats(List<Seat> holdSeats) throws TicketServiceException {
		boolean holdSuccess = true;
		List<Seat> tempSeats = new ArrayList<Seat>(holdSeats);
		for (Seat availSeat : holdSeats) {
			if (ticketServiceDAO.makeSeatUnavailable(availSeat.getSeatId()) == null) {
				// Targeted seat could not be found in available seat table
				// So reverting the transactions by adding all previously held
				// seats back to availSeats table
				holdSuccess = false;
				for (Seat tempSeat : tempSeats) {
					ticketServiceDAO.makeSeatAvailable(tempSeat);
				}
				break;
			}
			tempSeats.add(availSeat);
		}
		return holdSuccess;
	}

	public String reserveSeats(final int seatHoldId, final String customerEmail)
			throws InvalidSeatHoldException, SeatHoldExpiredException, SeatHoldAlreadyConfirmedException,
			ReservationTimeoutException, TicketServiceException {

		if (!isValidEmail(customerEmail)) {
			throw new TicketServiceException("Invalid email: " + customerEmail, TicketServiceErrorType.INVALID_EMAIL);
		}
		String confirmationId = null;

		SeatHold seatHold = ticketServiceDAO.getHoldReservations(seatHoldId);
		if (seatHold == null) {
			throw new InvalidSeatHoldException(String.format("Seat hold id '%d' not found", seatHoldId));
		}
		if (!StringUtils.equals(customerEmail, seatHold.getCustomerEmail())) {
			throw new InvalidSeatHoldException(
					String.format("Seat hold id '%d' has different email on file than provided '%s'", seatHoldId,
							seatHold.getCustomerEmail()),
					TicketServiceErrorType.SEAT_HOLD_EMAIL_NOT_MATCHING);
		}
		if (seatHold.isExpired()) {
			throw new SeatHoldExpiredException(String.format("Seat hold id '%d' is expired", seatHoldId));
		}
		try {
			if (getConfirmReservationLock().tryLock(getConfirmReservationTimeout(), TimeUnit.MILLISECONDS)) {
				if (seatHold.isReserved()) {
					confirmationId = seatHold.getConfirmationId();
					throw new SeatHoldAlreadyConfirmedException(
							String.format("Seat hold id '%d' is already confirmed with confirmation id '%s'",
									seatHoldId, confirmationId),
							confirmationId);
				}
				seatHold.markAsReserved();
				confirmationId = seatHold.getConfirmationId();
				ticketServiceDAO.confirmReservation(seatHold);
			} else {
				throw new ReservationTimeoutException(
						String.format("Unable to confirm seats for seat hold id '%d'", seatHold.getSeatHoldId()));
			}
		} catch (InterruptedException e) {
			throw new ReservationTimeoutException(
					String.format("Reservation timeout occurred: %d seconds", getHoldReservationTimeout()), e);
		} finally {
			try {
				getConfirmReservationLock().unlock();
			} catch (IllegalMonitorStateException e) {
			} catch (Exception e) {
				LOGGER.error("Error occurred while release getConfirmReservationLock: " + e.getMessage());
			}
		}
		TicketServiceEmailUtility.sendEmail(seatHold, TicketServiceEvent.CONFIRM_RESERVATION);
		return confirmationId;
	}
}
