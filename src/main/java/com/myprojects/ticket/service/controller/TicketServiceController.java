package com.myprojects.ticket.service.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myprojects.ticket.service.TicketService;
import com.myprojects.ticket.service.beans.SeatHold;

/**
 * REST Controller for ticket-service
 */
@Controller
@RequestMapping("/ticket-service")
public class TicketServiceController {
	
	private static final Logger LOGGER = Logger.getLogger(TicketServiceController.class);
	
	private final static ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	private TicketService service;
	
	@RequestMapping(value="/numSeatsAvailable/", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> numSeatsAvailable() {
		
		ResponseEntity<String> response = null;
		try {
			int numSeats = service.numSeatsAvailable();
			Map<String, Integer> responseMap = new HashMap<String, Integer>();
			responseMap.put("numSeatsAvailable", numSeats);
			response = new ResponseEntity<String>(mapper.writeValueAsString(responseMap), HttpStatus.ACCEPTED);
			LOGGER.info("Inside the controller: seatsAvailable = " + numSeats);
		} catch (Exception e) {
			response = new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(e);
		}
		return response;
	}
	
	@RequestMapping(value="/holdSeats/", method=RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> findAndHoldSeats(@RequestBody String jsonData) {
		
		ResponseEntity<String> response = null;
		try {
			TicketServiceRequest request = mapper.readValue(jsonData, TicketServiceRequest.class);
			SeatHold seatHold = service.findAndHoldSeats(request.getNumSeats(), request.getEmail());
			Map<String, Integer> responseMap = new HashMap<String, Integer>();
			responseMap.put("seatHoldId", seatHold.getSeatHoldId());
			response = new ResponseEntity<String>(mapper.writeValueAsString(responseMap), HttpStatus.ACCEPTED);
			LOGGER.info("Inside the controller: seatHoldId = " + seatHold.getSeatHoldId());
		} catch (Exception e) {
			response = new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(e);
		}
		return response;
	}
	
	@RequestMapping(value="/reserveSeats/", method=RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> reserveSeats(@RequestBody String jsonData) {
		
		ResponseEntity<String> response = null;
		try {
			TicketServiceRequest request = mapper.readValue(jsonData, TicketServiceRequest.class);
			String confirmationId = service.reserveSeats(request.getSeatHoldId(), request.getEmail());
			Map<String, String> responseMap = new HashMap<String, String>();
			responseMap.put("confirmationId", confirmationId);
			response = new ResponseEntity<String>(mapper.writeValueAsString(responseMap), HttpStatus.ACCEPTED);
			LOGGER.info("Inside the controller: confirmationId = " + confirmationId);
		} catch (Exception e) {
			response = new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.error(e);
		}
		return response;
	}
}

