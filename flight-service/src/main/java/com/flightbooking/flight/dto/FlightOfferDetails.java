package com.flightbooking.flight.dto;

import java.util.List;


public class FlightOfferDetails {

	private String offerId;
	private String airline;
	private String airlineCode;
	private String price;
	private String currency;
	private List<FlightOffer.Segment> segments;

	public FlightOfferDetails(
            String offerId,
            String airline,
            String airlineCode,
            String price,
            String currency,
            List<FlightOffer.Segment> segments
    ) {
        this.offerId = offerId;
        this.airline = airline;
        this.airlineCode = airlineCode;
        this.price = price;
        this.currency = currency;
        this.segments = segments;
    }


	public String getOfferId() {
        return offerId;
    }

    public String getAirline() {
        return airline;
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public String getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public List<FlightOffer.Segment> getSegments() {
        return segments;
    }

}
