package com.myprojects.ticket.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
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
import com.myprojects.ticket.service.beans.comparators.SeatRankComparator;
import com.myprojects.ticket.service.dao.DataGenerator;
import com.myprojects.ticket.service.dao.TicketServiceDAOImpl;
import com.myprojects.ticket.service.enums.SeatType;
import com.myprojects.ticket.service.enums.TicketServiceErrorType;
import com.myprojects.ticket.service.exception.TicketServiceException;
import com.myprojects.ticket.service.utils.TicketServiceUtils;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceTest {

	@Mock
	DataGenerator dataGenerator;

	@InjectMocks
	TicketServiceDAOImpl dao = new TicketServiceDAOImpl();

	TicketServiceImpl service = new TicketServiceImpl();

	String email = "test@test.com";

	@Before
	public void setUp() throws Exception {
		dao.setDataGenerator(dataGenerator);
		service.setTicketServiceDAO(dao);
	}

	@Test
	public void testNumSeatsAvailable_Excp_AvailMapNull() throws TicketServiceException {
		try {
			Map<String, Seat> availSeatMap = null;
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Assert.assertEquals(service.numSeatsAvailable(), 0);
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.DATA_BASE_ERROR, e.getErrorType());
		}
	}

	@Test
	public void testNumSeatsAvailable() throws TicketServiceException {
		try {
			Map<String, Seat> availSeatsMap = new ConcurrentHashMap<String, Seat>();
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatsMap);
			Assert.assertEquals(service.numSeatsAvailable(), 0);

			int rowId = 1;
			int colId = 1;
			int maxRows = 3;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			float rate = 40;
			SeatType seatType = TicketServiceUtils.identifySeatType(rowId, maxGoldRows, maxSilverRows);
			int rank = TicketServiceUtils.calculateSeatRank(rowId, colId, maxRows, maxCols);

			availSeatsMap.put("R1-C1", new Seat(rowId, colId, seatType, rank, rate));
			Assert.assertEquals(service.numSeatsAvailable(), 1);

			availSeatsMap.put("R1-C1", new Seat(rowId, colId, seatType, rank, rate));
			availSeatsMap.put("R1-C1", new Seat(rowId, colId, seatType, rank, rate));
			availSeatsMap.put("R1-C1", new Seat(rowId, colId, seatType, rank, rate));
			Assert.assertEquals(service.numSeatsAvailable(), 1);

			availSeatsMap.put("R1-C2", new Seat(rowId, colId, seatType, rank, rate));
			availSeatsMap.put("R1-C3", new Seat(rowId, colId, seatType, rank, rate));
			availSeatsMap.put("R1-C4", new Seat(rowId, colId, seatType, rank, rate));

			Assert.assertEquals(service.numSeatsAvailable(), 4);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testFindAndHoldSeats_Excp_InvalidSeatsReq() {
		try {
			Map<String, Seat> availSeatMap = null;
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Assert.assertNotNull(service.findAndHoldSeats(0, email));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.INVALID_NUM_SEATS, e.getErrorType());
		}

	}

	@Test
	public void testFindAndHoldSeats_Excp_InvalidEmail() {
		try {
			Map<String, Seat> availSeatMap = null;
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Assert.assertNotNull(service.findAndHoldSeats(1, "invalidEmail"));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.INVALID_EMAIL, e.getErrorType());
		}
	}

	@Test
	public void testFindAndHoldSeats_Excp_AvailMapNull() {
		try {
			Map<String, Seat> availSeatMap = null;
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Assert.assertNotNull(service.findAndHoldSeats(3, email));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.DATA_BASE_ERROR, e.getErrorType());
		}
	}

	@Test
	public void testFindAndHoldSeats_Excp_HoldReservationsMapNull() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Map<Integer, SeatHold> holdSeatsMap = null;
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(holdSeatsMap);
			Assert.assertEquals(service.findAndHoldSeats(8, email).getSeats().size(), 8);
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.DATA_BASE_ERROR, e.getErrorType());
		}
	}

	@Test
	public void testFindAndHoldSeats_Excp_Housefull() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Assert.assertEquals(service.findAndHoldSeats(8, email).getSeats().size(), 8);
			Assert.assertEquals(service.findAndHoldSeats(4, email).getSeats().size(), 4);
			Assert.assertNull(service.findAndHoldSeats(2, email));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.SHOW_HOUSEFULL, e.getErrorType());
		}
	}

	@Test
	public void testFindAndHoldSeats_Excp_LessSeatsAvail() {
		try {
			int maxRows = 3;
			int maxCols = 4;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Assert.assertEquals(service.findAndHoldSeats(8, email).getSeats().size(), 8);
			Assert.assertEquals(service.findAndHoldSeats(2, email).getSeats().size(), 2);
			Assert.assertNotNull(service.findAndHoldSeats(4, email));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.NOT_ENOUGH_SEATS, e.getErrorType());
		}
	}

	@Test
	public void testFindAndHoldSeats() {
		try {

			int maxRows = 4;
			int maxCols = 3;
			int maxSeats = maxRows * maxCols;
			int maxGoldRows = 1;
			int maxSilverRows = 1;

			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);

			Map<Integer, SeatHold> holdSeatsMap = new ConcurrentHashMap<Integer, SeatHold>();
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(holdSeatsMap);

			SeatHold seatHold1 = service.findAndHoldSeats(4, email);
			Assert.assertNotNull(seatHold1);
			Assert.assertEquals(seatHold1.getSeats().size(), 4);
			for (Seat seat : seatHold1.getSeats()) {
				Assert.assertFalse(availSeatMap.containsKey(seat.getSeatId()));
			}
			Assert.assertEquals(maxSeats - 4, service.numSeatsAvailable());

			SeatHold seatHold2 = service.findAndHoldSeats(3, email);
			Assert.assertNotNull(seatHold2);
			Assert.assertEquals(seatHold2.getSeats().size(), 3);
			for (Seat seat : seatHold2.getSeats()) {
				Assert.assertFalse(availSeatMap.containsKey(seat.getSeatId()));
			}
			Assert.assertEquals(maxSeats - (4 + 3), service.numSeatsAvailable());

			SeatHold seatHold3 = service.findAndHoldSeats(5, email);
			Assert.assertNotNull(seatHold3);
			Assert.assertEquals(seatHold3.getSeats().size(), 5);
			for (Seat seat : seatHold3.getSeats()) {
				Assert.assertFalse(availSeatMap.containsKey(seat.getSeatId()));
			}
			Assert.assertEquals(0, service.numSeatsAvailable());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testReserveSeats_Excp_InvalidEmail() {
		try {
			Map<String, Seat> availSeatMap = null;
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Assert.assertNotNull(service.reserveSeats(1, "InvalidEmail"));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.INVALID_EMAIL, e.getErrorType());
		}
	}

	@Test
	public void testReserveSeats_Excp_AvailMapNull() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);

			Map<Integer, SeatHold> holdSeatsMap = new ConcurrentHashMap<Integer, SeatHold>();
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(holdSeatsMap);

			SeatHold seatHold = service.findAndHoldSeats(4, email);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(null);
			Assert.assertNotNull(service.reserveSeats(seatHold.getSeatHoldId(), email));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.DATA_BASE_ERROR, e.getErrorType());
		}
	}

	@Test
	public void testReserveSeats_Excp_HoldReservationsMapNull() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(null);
			Assert.assertNotNull(service.reserveSeats(1, email));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.DATA_BASE_ERROR, e.getErrorType());
		}
	}

	@Test
	public void testReserveSeats_Excp_InvalidSeatHoldId() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(new ConcurrentHashMap<Integer, SeatHold>());
			SeatHold seatHold = service.findAndHoldSeats(4, email);
			Assert.assertEquals(4, seatHold.getSeats().size());
			Assert.assertNotNull(service.reserveSeats(seatHold.getSeatHoldId() + 1, email));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.SEAT_HOLD_ID_NOT_FOUND, e.getErrorType());
		}
	}

	@Test
	public void testReserveSeats_Excp_EmailNotMatched() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(new ConcurrentHashMap<Integer, SeatHold>());
			SeatHold seatHold = service.findAndHoldSeats(4, email);
			Assert.assertEquals(4, seatHold.getSeats().size());
			Assert.assertNotNull(service.reserveSeats(seatHold.getSeatHoldId(), "NotMatching" + email));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.SEAT_HOLD_EMAIL_NOT_MATCHING, e.getErrorType());
		}
	}

	@Test
	public void testReserveSeats_Excp_SeatHoldExpired() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Map<Integer, SeatHold> holdReservationsMap = new ConcurrentHashMap<Integer, SeatHold>();
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(holdReservationsMap);
			Map<String, SeatHold> confirmedReservationsMap = new ConcurrentHashMap<String, SeatHold>();
			Mockito.when(dataGenerator.getConfirmedReservations()).thenReturn(confirmedReservationsMap);

			SeatHold seatHold = service.findAndHoldSeats(4, email);
			Assert.assertTrue(holdReservationsMap.containsKey(seatHold.getSeatHoldId()));
			Assert.assertEquals(4, seatHold.getSeats().size());
			Assert.assertEquals(maxRows * maxCols - 4, availSeatMap.size());
			seatHold.markAsExpired();
			Assert.assertNotNull(service.reserveSeats(seatHold.getSeatHoldId(), email));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.SEAT_HOLD_EXPIRED, e.getErrorType());
		}
	}

	@Test
	public void testReserveSeats_Excp_AlreadyReserved() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Map<Integer, SeatHold> holdReservationsMap = new ConcurrentHashMap<Integer, SeatHold>();
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(holdReservationsMap);
			Map<String, SeatHold> confirmedReservationsMap = new ConcurrentHashMap<String, SeatHold>();
			Mockito.when(dataGenerator.getConfirmedReservations()).thenReturn(confirmedReservationsMap);

			SeatHold seatHold = service.findAndHoldSeats(4, email);
			Assert.assertTrue(holdReservationsMap.containsKey(seatHold.getSeatHoldId()));
			Assert.assertEquals(4, seatHold.getSeats().size());
			Assert.assertEquals(maxRows * maxCols - 4, availSeatMap.size());

			String confirmationId = service.reserveSeats(seatHold.getSeatHoldId(), email);
			Assert.assertNotNull(confirmationId);
			Assert.assertTrue(confirmedReservationsMap.containsKey(confirmationId));
			Assert.assertNotNull(service.reserveSeats(seatHold.getSeatHoldId(), email));
		} catch (TicketServiceException e) {
			Assert.assertEquals(TicketServiceErrorType.SEAT_HOLD_ALREADY_CONFIRMED, e.getErrorType());
		}
	}

	@Test
	public void testReserveSeats() {
		try {
			int maxRows = 4;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatMap);
			Map<Integer, SeatHold> holdReservationsMap = new ConcurrentHashMap<Integer, SeatHold>();
			Mockito.when(dataGenerator.getHoldReservations()).thenReturn(holdReservationsMap);
			Map<String, SeatHold> confirmedReservationsMap = new ConcurrentHashMap<String, SeatHold>();
			Mockito.when(dataGenerator.getConfirmedReservations()).thenReturn(confirmedReservationsMap);

			SeatHold seatHold = service.findAndHoldSeats(4, email);
			Assert.assertTrue(holdReservationsMap.containsKey(seatHold.getSeatHoldId()));
			Assert.assertEquals(4, seatHold.getSeats().size());
			Assert.assertEquals(maxRows * maxCols - 4, availSeatMap.size());

			String confirmationId = service.reserveSeats(seatHold.getSeatHoldId(), email);
			Assert.assertNotNull(confirmationId);
			Assert.assertTrue(confirmedReservationsMap.containsKey(confirmationId));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testHoldSeats_Success() {
		try {
			int maxRows = 3;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatsMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatsMap);
			List<Seat> seatsList = new ArrayList<Seat>(availSeatsMap.values());
			Collections.sort(seatsList);
			List<Seat> holdSeats = seatsList.subList(0, maxRows);
			Assert.assertTrue(service.holdSeats(holdSeats));
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	@Test
	public void testHoldSeats_Failure() {
		try {
			int maxRows = 3;
			int maxCols = 3;
			int maxGoldRows = 1;
			int maxSilverRows = 1;
			Map<String, Seat> availSeatsMap = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
			Mockito.when(dataGenerator.getAvailSeats()).thenReturn(availSeatsMap);
			List<Seat> seatsList = new ArrayList<Seat>(availSeatsMap.values());
			Collections.sort(seatsList);
			List<Seat> holdSeats = seatsList.subList(0, maxRows);
			availSeatsMap.remove(holdSeats.get(0).getSeatId());
			Assert.assertFalse(service.holdSeats(holdSeats));
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void treeSetVsMap() {
		int maxRows = 10;
		int maxCols = 10;
		int maxGoldRows = 1;
		int maxSilverRows = 1;
		Map<String, Seat> map = generateAvailSeatMap(maxRows, maxCols, maxGoldRows, maxSilverRows);
		System.out.println("********** HashMap Size *********** : " + map.size());
		System.out.println("********** HashMap Elements ******* : " + map);
		SortedSet<Seat> set = new TreeSet<Seat>(new SeatRankComparator());
		set.addAll(map.values());
		System.out.println("********** TreeSet Size *********** : " + set.size());
		System.out.println("********** TreeSet Elements ******* : " + set);
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
