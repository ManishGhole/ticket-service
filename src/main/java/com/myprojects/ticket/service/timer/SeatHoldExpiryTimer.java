package com.myprojects.ticket.service.timer;

import static com.myprojects.ticket.service.TicketServiceLocks.getAvailSeatsLock;
import static com.myprojects.ticket.service.TicketServiceLocks.getHoldReservationLock;
import static com.myprojects.ticket.service.dao.config.TicketServiceConfig.getAvailableSeatsLockTimeout;
import static com.myprojects.ticket.service.dao.config.TicketServiceConfig.getSeatHoldExpiry;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.myprojects.ticket.service.beans.Seat;
import com.myprojects.ticket.service.beans.SeatHold;
import com.myprojects.ticket.service.dao.TicketServiceDAO;
import com.myprojects.ticket.service.email.TicketServiceEmailUtility;
import com.myprojects.ticket.service.enums.TicketServiceEvent;
import com.myprojects.ticket.service.exception.TicketServiceException;

/**
 * This class is Timer task which will run at desired schedule
 * to check expired hold reservations and release the seats 
 *
 */
public class SeatHoldExpiryTimer extends TimerTask {
	private static final Logger LOGGER = Logger.getLogger(SeatHoldExpiryTimer.class);

	private TicketServiceDAO ticketServiceDAO;

	public SeatHoldExpiryTimer(TicketServiceDAO ticketServiceDAO) {
		this.ticketServiceDAO = ticketServiceDAO;
	}

	@Override
	public void run() {
		int count = 0;
		try {
			List<SeatHold> holdReservations = ticketServiceDAO.getAllHoldSeatHolds();
			if (holdReservations != null && !holdReservations.isEmpty()) {
				long expiryTimeout = getSeatHoldExpiry() * 1000;
				for (SeatHold seatHold : holdReservations) {
					long timeDiff = System.currentTimeMillis() - seatHold.getTimestamp().getTime();
					if (seatHold != null && !seatHold.isReserved() && !seatHold.isExpired()
							&& timeDiff >= expiryTimeout) {
						seatHold.markAsExpired();
						if (seatHold.getSeats() != null) {
							for (Seat seat : seatHold.getSeats()) {
								ticketServiceDAO.makeSeatAvailable(seat);
								count++;
							}
						}
						ticketServiceDAO.updateHoldReservation(seatHold);
						TicketServiceEmailUtility.sendEmail(seatHold, TicketServiceEvent.HOLD_RESERVATION_EXPIRED);
					}
				}
			}
		} catch (TicketServiceException e) {
			LOGGER.error("SeatHold expiration cycle failed", e);
		} catch (Exception e) {
			LOGGER.error("Error occurred while SeatHold expiration cycle failed", e);
		} finally {
			if (count > 0) {
				LOGGER.info(String.format("**** SeatHold EXPIRY TIMER **** Seats Released : %d", count));
			}
		}
	}

	/**
	 * This method is not being used as it is able to acquire the lock but not releasing it
	 * But still I feel we don't need lock for where we are only adding the seats back to avail map
	 */
	public void run_old() {
		int count = 0;
		try {
			List<SeatHold> holdReservations = ticketServiceDAO.getAllHoldSeatHolds();
			if (holdReservations != null && !holdReservations.isEmpty()) {
				long expiryTimeout = getSeatHoldExpiry() * 1000;
				for (SeatHold seatHold : holdReservations) {
					long timeDiff = System.currentTimeMillis() - seatHold.getTimestamp().getTime();
					if (seatHold != null && !seatHold.isReserved() && !seatHold.isExpired()
							&& timeDiff >= expiryTimeout) {
						try {
							seatHold.markAsExpired();
							if (getAvailSeatsLock().tryLock(getAvailableSeatsLockTimeout(), TimeUnit.MILLISECONDS)) {
								if (seatHold.getSeats() != null) {
									for (Seat seat : seatHold.getSeats()) {
										ticketServiceDAO.makeSeatAvailable(seat);
										count++;
									}
								}
							} else {
								LOGGER.error(String.format("Could not get lock on seatHold id '%d' for expiration",
										seatHold.getSeatHoldId()));
							}
						} catch (InterruptedException e) {
							LOGGER.error(String.format("Could not get lock on seatHold id '%d' for expiration",
									seatHold.getSeatHoldId()));
						} finally {
							try {
								getHoldReservationLock().unlock();
							} catch (IllegalMonitorStateException e) {
								LOGGER.error("Error occurred while release getHoldReservationLock: " + e.getMessage());
							} catch (Exception e) {
								LOGGER.error("Error occurred while release getHoldReservationLock: " + e.getMessage());
							}

						}
					}
				}
			}
		} catch (TicketServiceException e) {
			LOGGER.error("SeatHold expiration cycle failed", e);
		} catch (Exception e) {
			LOGGER.error("Error occurred while SeatHold expiration cycle failed", e);
		} finally {
			if (count > 0) {
				LOGGER.info(String.format("Seat hold expity timer ****** Seats Released : %d", count));
			}
		}
	}

}