package com.myprojects.ticket.service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class contains the locks for multi-threaded application
 * to preserve the data integrity
 */
public final class TicketServiceLocks {
	private static Lock availSeatsLock;
	private static Lock holdReservationLock;
	private static Lock confirmReservationLock;

	private TicketServiceLocks() {
	}

	static {
		availSeatsLock = new ReentrantLock(true);
		holdReservationLock = new ReentrantLock(true);
		confirmReservationLock = new ReentrantLock(true);
	}

	public static Lock getAvailSeatsLock() {
		return availSeatsLock;
	}

	public static Lock getHoldReservationLock() {
		return holdReservationLock;
	}

	public static Lock getConfirmReservationLock() {
		return confirmReservationLock;
	}
	
}
