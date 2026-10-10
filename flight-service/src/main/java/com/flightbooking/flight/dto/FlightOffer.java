package com.flightbooking.flight.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
public class FlightOffer {

	private String offerId;
	private String airline;
	private String airlineCode;
	private String flightNumber;
	private String origin;
	private String destination;
	private String departureTime;
	private String arrivalTime;
	private String duration;
	private String price;
	private String currency;
		private String displayPrice;
		private String displayCurrency;
		private String exchangeRate;
	private boolean refundable;
	private List<Segment> segments;

	public record Segment(String airline,
						String airlineCode,
						String flightNumber,
						String origin,
						String destination,
						String departureTime,
						String arrivalTime) {
	}


	public FlightOffer(String offerId,
					String airline,
					String airlineCode,
					String flightNumber,
					String origin,
					String destination,
					String departureTime,
					String arrivalTime,
					String duration,
					String price,
					String currency,
					String displayPrice,
			        String displayCurrency,
			        String exchangeRate,
					boolean refundable,
					List<Segment> segments) {
		this.offerId = offerId;
		this.airline = airline;
		this.airlineCode = airlineCode;
		this.flightNumber = flightNumber;
		this.origin = origin;
		this.destination = destination;
		this.departureTime = departureTime;
		this.arrivalTime = arrivalTime;
		this.duration = duration;
		this.price = price;
		this.currency = currency;
		this.displayPrice=displayPrice;
		this.displayCurrency=displayCurrency;
		this.exchangeRate = exchangeRate;
		this.refundable = refundable;
		this.segments = segments;

	}










}
