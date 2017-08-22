package com.myprojects.ticket.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.myprojects.ticket.service.beans.Seat;
import com.myprojects.ticket.service.beans.SeatHold;
import com.myprojects.ticket.service.dao.DataGenerator;
import com.myprojects.ticket.service.dao.TicketServiceDAOImpl;
import com.myprojects.ticket.service.dao.config.TicketServiceConfig;
import com.myprojects.ticket.service.enums.SeatType;
import com.myprojects.ticket.service.timer.SeatHoldExpiryTimer;
import com.myprojects.ticket.service.utils.TicketServiceUtils;

/**
 * Testing SeatHoldExpiryTimer class
 */
@RunWith(MockitoJUnitRunner.class)
public class SeatHoldExpiryTimerTest {
	@Mock
	DataGenerator dataGenerator;

	@InjectMocks
	TicketServiceDAOImpl dao = new TicketServiceDAOImpl();

	TicketServiceImpl service = new TicketServiceImpl();
	
	SeatHoldExpiryTimer timer = new SeatHoldExpiryTimer(dao);

	String email = "test@test.com";

	@Before
	public void setUp() throws Exception {
		dao.setDataGenerator(dataGenerator);
		service.setTicketServiceDAO(dao);
	}
	
	@Test
	public void testRun_NoReservedSeatHold() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxSeats = maxRows*maxCols;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Map<Integer, SeatHold> holdReservationsMap = new ConcurrentHashMap<Integer, SeatHold>();
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(holdReservationsMap);
			
			SeatHold seatHold1 = service.findAndHoldSeats(2, email);
			Assert.assertNotNull(seatHold1);
			SeatHold seatHold2 = service.findAndHoldSeats(2, email);
			Assert.assertNotNull(seatHold2);
			Assert.assertEquals(maxSeats-4, service.numSeatsAvailable());
			
			timer.run();
			Assert.assertEquals(8, service.numSeatsAvailable());
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testRun_NoExpiredSeatHold() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxSeats = maxRows*maxCols;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Map<Integer, SeatHold> holdReservationsMap = new ConcurrentHashMap<Integer, SeatHold>();
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(holdReservationsMap);
			SeatHold seatHold1 = service.findAndHoldSeats(2, email);
			Assert.assertNotNull(seatHold1);
			SeatHold seatHold2 = service.findAndHoldSeats(2, email);
			Assert.assertNotNull(seatHold2);
			Assert.assertNotNull(service.reserveSeats(seatHold2.getSeatHoldId(), email));
			Assert.assertEquals(maxSeats-4, service.numSeatsAvailable());
			
			timer.run();
			Assert.assertEquals(maxSeats-4, service.numSeatsAvailable());
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	@Test
	public void testRun_WithExpiredSeatHold() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxSeats = maxRows*maxCols;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Map<Integer, SeatHold> holdReservationsMap = new ConcurrentHashMap<Integer, SeatHold>();
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(holdReservationsMap);
			
			SeatHold seatHold1 = service.findAndHoldSeats(2, email);
			Assert.assertNotNull(seatHold1);
			SeatHold seatHold2 = service.findAndHoldSeats(2, email);
			Assert.assertNotNull(seatHold2);
			Assert.assertEquals(maxSeats-4, service.numSeatsAvailable());
			
			long expiryTs = seatHold2.getTimestamp().getTime()-(TicketServiceConfig.getSeatHoldExpiry()*1000);
			seatHold2.getTimestamp().setTime(expiryTs);
			
			timer.run();
			Assert.assertEquals(maxSeats-2, service.numSeatsAvailable());
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	private Map<String, Seat> generateAvailSeatMap(int maxRows, int maxCols, int maxGoldRows, int maxSilverRows) {
		Map<String, Seat> availSeatsMap = new ConcurrentHashMap<String, Seat>();
		for (int rowId = 1; rowId <= maxRows; rowId++) {
			for (int colId = 1; colId <= maxCols; colId++) {
				SeatType seatType = TicketServiceUtils.identifySeatType(rowId, maxGoldRows, maxSilverRows);
				int rank = TicketServiceUtils.calculateSeatRank(rowId, colId, maxRows, maxCols);
				Seat seat = new Seat(rowId, colId, seatType, rank, 40);
				availSeatsMap.put(seat.getSeatId(), seat);
			}
		}
		return availSeatsMap;
	}

}
