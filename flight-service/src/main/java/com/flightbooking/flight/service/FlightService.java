package com.flightbooking.flight.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.flightbooking.flight.client.DuffelClient;
import com.flightbooking.flight.dto.CreateDuffelOrderRequest;
import com.flightbooking.flight.dto.DuffelOrderResponse;
import com.flightbooking.flight.dto.FlightOffer;
import com.flightbooking.flight.dto.FlightOfferDetails;
import com.flightbooking.flight.dto.FlightSearchRequest;
import com.flightbooking.flight.dto.FlightSearchResponse;
import com.flightbooking.flight.exception.FlightApiException;

@Service
public class FlightService {
	private final DuffelClient duffelClient;
	private final CurrencyConversionService currencyConversionService;

	public FlightService(
	        DuffelClient duffelClient,
	        CurrencyConversionService currencyConversionService) {

	    this.duffelClient = duffelClient;
	    this.currencyConversionService = currencyConversionService;
	}

    public FlightSearchResponse searchFlights(FlightSearchRequest request) {
        Map<String, Object> response = duffelClient.searchFlights(request);
        Object dataObject = response.get("data");
        if (!(dataObject instanceof Map<?, ?> data)) {
            return new FlightSearchResponse(null, new ArrayList<>());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) data;
        String searchId = dataMap.get("id") == null
                ? null
                : String.valueOf(dataMap.get("id"));

//        List<FlightOffer> offers = parseOffers(dataMap);
        String selectedCurrency = request.getCurrency();
        List<FlightOffer> offers = parseOffers(dataMap, selectedCurrency);
        return new FlightSearchResponse(searchId, offers);
    }


    @SuppressWarnings("unchecked")
    private List<FlightOffer> parseOffers(Map<String, Object> response, String selectedCurrency) {
        List<FlightOffer> result = new ArrayList<>();
        Object offersObject = response.get("offers");
        if (!(offersObject instanceof List<?> offersList)) {
            return result;
        }

        for (Object offerObject : offersList) {
            if (!(offerObject instanceof Map<?, ?> offerMap)) {
                continue;
            }

            Map<String, Object> offer = (Map<String, Object>) offerMap;
//            FlightOffer flightOffer = mapOffer(offer);
            FlightOffer flightOffer =
                    mapOffer(offer, selectedCurrency);
            result.add(flightOffer);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private FlightOffer mapOffer( Map<String, Object> offer, String selectedCurrency) {
        List<FlightOffer.Segment> segments = new ArrayList<>();
        Object slicesObject = offer.get("slices");
        if (slicesObject instanceof List<?> slices) {
            for (Object sliceObject : slices) {
                if (!(sliceObject instanceof Map<?, ?> slice)) {
                    continue;
                }

                Object segmentsObject = slice.get("segments");
                if (segmentsObject instanceof List<?> segmentList) {
                    for (Object segmentObject : segmentList) {
                        if (!(segmentObject instanceof Map<?, ?> segment)) {
                            continue;
                        }

                        Object originObject = segment.get("origin");
                        Object destinationObject = segment.get("destination");
                        Object carrierObject = segment.get("operating_carrier");

                        if (!(originObject instanceof Map<?, ?> origin)) {
                            continue;
                        }

                        if (!(destinationObject instanceof Map<?, ?> destination)) {
                            continue;
                        }

                        if (!(carrierObject instanceof Map<?, ?> operatingCarrier)) {
                            continue;
                        }

                        segments.add(
                            new FlightOffer.Segment(
                                String.valueOf(operatingCarrier.get("name")),
                                String.valueOf(operatingCarrier.get("iata_code")),
                                String.valueOf(segment.get("operating_carrier_flight_number")),
                                String.valueOf(origin.get("iata_code")),
                                String.valueOf(destination.get("iata_code")),
                                String.valueOf(segment.get("departing_at")),
                                String.valueOf(segment.get("arriving_at"))
                            )
                        );
                    }
                }
            }
        }

        FlightOffer.Segment first =
                segments.isEmpty()
                        ? null
                        : segments.get(0);

        FlightOffer.Segment last =
                segments.isEmpty()
                        ? null
                        : segments.get(segments.size() - 1);

        String price =
                offer.get("total_amount") == null
                        ? null
                        : String.valueOf(
                                offer.get("total_amount")
                          );

        String currency =
                offer.get("total_currency") == null
                        ? null
                        : String.valueOf(
                                offer.get("total_currency")
                          );
        BigDecimal originalAmount =
                price == null
                        ? BigDecimal.ZERO
                        : new BigDecimal(price);

        BigDecimal convertedAmount =
                currencyConversionService.convert(
                        originalAmount,
                        currency,
                        selectedCurrency);

        BigDecimal exchangeRate =
                currencyConversionService.getRate(
                        currency,
                        selectedCurrency);


        return new FlightOffer(
                String.valueOf(offer.get("id")),
                first == null
                        ? null
                        : first.airline(),

                first == null
                        ? null
                        : first.airlineCode(),

                first == null
                        ? null
                        : first.flightNumber(),

                first == null
                        ? null
                        : first.origin(),

                last == null
                        ? null
                        : last.destination(),

                first == null
                        ? null
                        : first.departureTime(),

                last == null
                        ? null
                        : last.arrivalTime(),

                null,
                price,
                currency,
                convertedAmount.toPlainString(),
                selectedCurrency,

                exchangeRate.toPlainString(),

                false,
                segments
        );
    }

    public FlightOfferDetails getOffer(String offerId) {
        Map<String, Object> response = duffelClient.getOffer(offerId);
        Object dataObject = response.get("data");
        if (!(dataObject instanceof Map<?, ?> dataMap)) {
            throw new FlightApiException(
                    "Invalid offer response from Duffel"
            );
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> offer = (Map<String, Object>) dataMap;
        return mapOfferDetails(offer);
    }


    @SuppressWarnings("unchecked")
    public FlightOfferDetails mapOfferDetails(Map<String, Object> offer) {
        List<FlightOffer.Segment> segments = new ArrayList<>();
        Object slicesObject = offer.get("slices");
        if (slicesObject instanceof List<?> slices) {
            for (Object sliceObject : slices) {
                if (!(sliceObject instanceof Map<?, ?> slice)) {
                    continue;
                }

                Object segmentsObject = slice.get("segments");
                if (!(segmentsObject instanceof List<?> segmentList)) {
                    continue;
                }

                for (Object segmentObject : segmentList) {
                    if (!(segmentObject instanceof Map<?, ?> segment)) {
                        continue;
                    }

                    Object originObject = segment.get("origin");
                    Object destinationObject = segment.get("destination");
                    Object carrierObject = segment.get("operating_carrier");
                    if (!(originObject instanceof Map<?, ?> origin)) {
                        continue;
                    }

                    if (!(destinationObject instanceof Map<?, ?> destination)) {
                        continue;
                    }

                    if (!(carrierObject instanceof Map<?, ?> carrier)) {
                        continue;
                    }

                    segments.add(new FlightOffer.Segment(
                                    String.valueOf(carrier.get("name")),
                                    String.valueOf(carrier.get("iata_code")),
                                    String.valueOf(segment.get("operating_carrier_flight_number")),
                                    String.valueOf(origin.get("iata_code")),
                                    String.valueOf(destination.get("iata_code")),
//                                    String.valueOf(segment.get("departing_at")),
//                                    String.valueOf(segment.get("arriving_at"))
                                    getRequiredString(segment, "departing_at"),
                                    getRequiredString(segment, "arriving_at")
                            )
                    );
                }
            }
        }

        String price = offer.get("total_amount") == null
                        ? null
                        : String.valueOf(offer.get("total_amount"));

        String currency = offer.get("total_currency") == null
                        ? null
                        : String.valueOf(offer.get("total_currency"));

        FlightOffer.Segment first = segments.isEmpty()
                        ? null
                        : segments.get(0);

        return new FlightOfferDetails(
                String.valueOf(offer.get("id")),
                first == null
                        ? null
                        : first.airline(),

                first == null
                        ? null
                        : first.airlineCode(),

                price,
                currency,
                segments
        );
    }


    private String getRequiredString(
            Map<?, ?> map,
            String key
    ) {
        Object value = map.get(key);

        if (value == null) {
            throw new FlightApiException(
                "Duffel segment is missing required field: " + key
            );
        }

        String result = String.valueOf(value);

        if (result.isBlank() || "null".equalsIgnoreCase(result)) {
            throw new FlightApiException(
                "Duffel segment has empty required field: " + key
            );
        }

        return result;
    }

    public FlightOfferDetails repriceOffer(String offerId) {
        Map<String, Object> response = duffelClient.repriceOffer(offerId);
        Object dataObject = response.get("data");
        if (!(dataObject instanceof Map<?, ?> dataMap)) {
            throw new FlightApiException(
                    "Invalid price response from Duffel"
            );
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> offer = (Map<String, Object>) dataMap;
        return mapOfferDetails(offer);
    }


//    public DuffelOrderResponse createOrder(CreateDuffelOrderRequest request) {
//    	Map<String, Object> response = duffelClient.createOrder(request);
//    	Map<String, Object> data = (Map<String, Object>) response.get("data");
//    	String orderId = String.valueOf(data.get("id"));
//    	String bookingReference = String.valueOf(data.get("booking_reference"));
//    	String totalAmount = String.valueOf(data.get("total_amount"));
//    	String currency = String.valueOf(data.get("total_currency"));
//    	return new DuffelOrderResponse(
//    			orderId,
//    			bookingReference,
//    			totalAmount,
//    			currency);
//    }

//    public DuffelOrderResponse createOrder(
//            CreateDuffelOrderRequest request
//    ) {
//
//        Map<String, Object> response =
//                duffelClient.createOrder(request);
//
//        if (response == null) {
//            throw new FlightApiException(
//                    "Empty response received from Duffel"
//            );
//        }
//
//        Object dataObject =
//                response.get("data");
//
//        if (!(dataObject instanceof Map<?, ?> dataMap)) {
//            throw new FlightApiException(
//                    "Invalid order response received from Duffel"
//            );
//        }
//
//        String orderId =
//                dataMap.get("id") == null
//                        ? null
//                        : String.valueOf(
//                                dataMap.get("id")
//                        );
//
//        String bookingReference =
//                dataMap.get("booking_reference") == null
//                        ? null
//                        : String.valueOf(
//                                dataMap.get("booking_reference")
//                        );
//
//        String totalAmount =
//                dataMap.get("total_amount") == null
//                        ? null
//                        : String.valueOf(
//                                dataMap.get("total_amount")
//                        );
//
//        String currency =
//                dataMap.get("total_currency") == null
//                        ? null
//                        : String.valueOf(
//                                dataMap.get("total_currency")
//                        );
//
//        if (orderId == null) {
//            throw new FlightApiException(
//                    "Duffel order ID missing in response"
//            );
//        }
//
//        return new DuffelOrderResponse(
//                orderId,
//                bookingReference,
//                totalAmount,
//                currency
//        );
//    }

    public DuffelOrderResponse createOrder(
            CreateDuffelOrderRequest request) {

        if (request == null
                || request.offerId() == null
                || request.offerId().isBlank()) {
            throw new FlightApiException("Flight offer ID is required");
        }

        if (request.amount() == null
                || request.currency() == null
                || request.currency().isBlank()) {
            throw new FlightApiException(
                    "Expected amount and currency are required"
            );
        }

        // Get the latest price from Duffel.
        FlightOfferDetails latestOffer =
                repriceOffer(request.offerId());


if (latestOffer.getPrice() == null
        || latestOffer.getCurrency() == null) {
    throw new FlightApiException(
            "Unable to verify the latest flight price"
    );
}

BigDecimal expectedAmount =
        new BigDecimal(request.amount());

BigDecimal latestAmount =
        new BigDecimal(latestOffer.getPrice());

boolean currencyChanged =
        !request.currency().equalsIgnoreCase(
                latestOffer.getCurrency()
        );

boolean priceChanged =
        expectedAmount.compareTo(latestAmount) != 0;

if (currencyChanged || priceChanged) {
    throw new FlightApiException(
            "FLIGHT_PRICE_CHANGED: The flight price has changed. "
            + "Please review the latest price before proceeding."
    );
}


        // Create the order only after the price check passes.
        Map<String, Object> response =
                duffelClient.createOrder(request);

        if (response == null) {
            throw new FlightApiException(
                    "Empty response received from Duffel"
            );
        }

        Object dataObject = response.get("data");

        if (!(dataObject instanceof Map<?, ?> dataMap)) {
            throw new FlightApiException(
                    "Invalid order response received from Duffel"
            );
        }

        String orderId = dataMap.get("id") == null
                ? null
                : String.valueOf(dataMap.get("id"));

        String bookingReference = dataMap.get("booking_reference") == null
                ? null
                : String.valueOf(dataMap.get("booking_reference"));

        String totalAmount = dataMap.get("total_amount") == null
                ? null
                : String.valueOf(dataMap.get("total_amount"));

        String currency = dataMap.get("total_currency") == null
                ? null
                : String.valueOf(dataMap.get("total_currency"));

        if (orderId == null || orderId.isBlank()) {
            throw new FlightApiException(
                    "Duffel order ID missing in response"
            );
        }

        return new DuffelOrderResponse(
                orderId,
                bookingReference,
                totalAmount,
                currency
        );
    }


@SuppressWarnings("unchecked")
public Map<String, Object> searchLocations(String query) {

    if (query == null || query.trim().length() < 2) {
        throw new IllegalArgumentException(
                "Enter at least 2 characters to search locations"
        );
    }

    Map<String, Object> response =
            duffelClient.searchLocations(query);

    List<Map<String, Object>> locations = new ArrayList<>();

    Object dataObject = response == null
            ? null
            : response.get("data");

    if (dataObject instanceof List<?> data) {
        for (Object item : data) {
            if (!(item instanceof Map<?, ?> place)) {
                continue;
            }

            String type = String.valueOf(place.get("type"));

            Object codeObject = place.get("iata_code");

            if (codeObject == null) {
                codeObject = place.get("iata_city_code");
            }

            if (codeObject == null) {
                continue;
            }

            String code = String.valueOf(codeObject);

            if (code.isBlank() || "null".equals(code)) {
                continue;
            }

            String city = place.get("city_name") == null
                    ? ""
                    : String.valueOf(place.get("city_name"));

            String name = place.get("name") == null
                    ? city
                    : String.valueOf(place.get("name"));

            Map<String, Object> location = new HashMap<>();

            location.put("id", place.get("id"));
            location.put("name", name);
            location.put("city", city);
            location.put("country",
                    place.get("iata_country_code"));
            location.put("iataCode", code);
            location.put("type", type);

            locations.add(location);
        }
    }

    return Map.of("locations", locations);
}
}
