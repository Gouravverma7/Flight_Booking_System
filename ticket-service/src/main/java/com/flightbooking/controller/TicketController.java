package com.flightbooking.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.flightbooking.entity.Ticket;
import com.flightbooking.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@PostMapping("/{bookingReference}/generate")
	public ResponseEntity<Ticket> generateTicket(@PathVariable String bookingReference) throws Exception{
		return ResponseEntity.ok(ticketService.generateTicket(bookingReference));
	}

	@GetMapping("/{bookingReference}/download")
	public ResponseEntity<byte[]> downloadTicket(@PathVariable String bookingReference) throws Exception{
		Ticket ticket = ticketService.generateTicket(bookingReference);
		Path path = Paths.get(ticket.getPdfFilePath());

		byte[] pdf = Files.readAllBytes(path);

		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + ticket.getPdfFileName() + "\"")
									.header("Content-Type", "application/pdf")
									.body(pdf);
	}






























}
