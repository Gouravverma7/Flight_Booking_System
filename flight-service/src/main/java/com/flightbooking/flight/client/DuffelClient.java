package com.flightbooking.flight.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.flightbooking.flight.dto.CreateDuffelOrderRequest;
import com.flightbooking.flight.dto.FlightSearchRequest;
import com.flightbooking.flight.exception.FlightApiException;

@Component
public class DuffelClient {
	private final RestClient restClient;

	public DuffelClient(@Value("${duffel.base-url}") String baseUrl,
			@Value("${duffel.access-token}") String accessToken) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).defaultHeader("Authorization", "Bearer " + accessToken)
				.defaultHeader("Duffel-Version", "v2").defaultHeader("Accept", "application/json").build();
	}

	public Map<String, Object> searchFlights(FlightSearchRequest request) {
		Map<String, Object> passenger = Map.of("type", "adult");
		List<Map<String, Object>> passengers = new ArrayList<>();

		for (int i = 0; i < request.getAdults(); i++) {
			passengers.add(passenger);
		}

		Map<String, Object> slice = Map.of("origin", request.getOrigin().toUpperCase(), "destination",
				request.getDestination().toUpperCase(), "departure_date", request.getDepartureDate().toString());

		Map<String, Object> data = Map.of("slices", List.of(slice), "passengers", passengers, "cabin_class",
				request.getCabin().toLowerCase());

		Map<String, Object> body = Map.of("data", data);

		try {
			Map<String, Object> response = restClient.post().uri("/air/offer_requests")
					.contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
			System.out.println("DUFFEL RESPONSE " + response);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FlightApiException("Unable to retrieve flights from provider");
		}
	}

	public Map<String, Object> getOffer(String offerId) {
		try {
			Map<String, Object> response = restClient.get().uri("/air/offers/{offerId}", offerId).retrieve()
					.body(Map.class);
			System.out.println("DUFFEL OFFER RESPONSE: " + response);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FlightApiException("Unable to retrieve flight offer");
		}
	}

	public Map<String, Object> repriceOffer(String offerId) {
		Map<String, Object> body = Map.of("data", Map.of("intended_services", List.of()));

		try {
			Map<String, Object> response = restClient.post().uri("/air/offers/{offerId}/actions/price", offerId)
					.contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);

			System.out.println("DUFFEL PRICE RESPONSE: " + response);
			return response;

		} catch (org.springframework.web.client.RestClientResponseException e) {
			String responseBody = e.getResponseBodyAsString();

			System.err.println("Duffel repricing failed. Status: " + e.getStatusCode());
			System.err.println("Duffel response: " + responseBody);

			if (responseBody != null && responseBody.contains("offer_no_longer_available")) {
				throw new FlightApiException(
						"FLIGHT_OFFER_UNAVAILABLE: This offer is no longer available. Search again.");
			}

			throw new FlightApiException("Flight repricing failed. Please select another flight or try again later.");
		} catch (Exception e) {
			System.err.println("Unexpected repricing error: " + e.getMessage());
			e.printStackTrace();

			throw new FlightApiException("Unable to reprice flight offer");
		}
	}

	public Map<String, Object> createOrder(CreateDuffelOrderRequest request) {

		try {
			Map<String, Object> offerResponse = restClient.get().uri("/air/offers/{offerId}", request.offerId())
					.retrieve().body(Map.class);

			if (offerResponse == null) {
				throw new FlightApiException("Duffel returned empty offer response");
			}

			Object offerDataObject = offerResponse.get("data");
			if (!(offerDataObject instanceof Map<?, ?> offerData)) {
				throw new FlightApiException("Invalid offer response from Duffel");
			}

			Object offerPassengersObject = offerData.get("passengers");

			if (!(offerPassengersObject instanceof List<?> offerPassengers)) {
				throw new FlightApiException("Duffel offer does not contain passengers");
			}

			if (offerPassengers.size() != request.passengers().size()) {
				throw new FlightApiException("Passenger count mismatch. Duffel offer has " + offerPassengers.size()
						+ " passengers but request contains " + request.passengers().size());
			}

			List<Map<String, Object>> passengers = new ArrayList<>();

			for (int i = 0; i < request.passengers().size(); i++) {
				var passenger = request.passengers().get(i);
				Object offerPassengerObject = offerPassengers.get(i);
				if (!(offerPassengerObject instanceof Map<?, ?> offerPassenger)) {
					throw new FlightApiException("Invalid passenger data in Duffel offer");
				}

				Object duffelPassengerIdObject = offerPassenger.get("id");
				if (duffelPassengerIdObject == null) {
					throw new FlightApiException("Duffel passenger ID is missing");
				}

				String duffelPassengerId = String.valueOf(duffelPassengerIdObject);

				Map<String, Object> map = new HashMap<>();

				/*
				 * THIS IS THE IMPORTANT FIX
				 */
				map.put("id", duffelPassengerId);

				map.put("given_name", passenger.given_name());

				map.put("family_name", passenger.family_name());

				map.put("born_on", passenger.born_on());

				map.put("gender", passenger.gender());

				map.put("title", passenger.title());

				map.put("email", passenger.email());

				map.put("phone_number", normalizePhoneNumber(passenger.phone_number()));

				map.put("type", "adult");

				passengers.add(map);
			}

			/*
			 * Duffel payment.
			 */
			Map<String, Object> payment = Map.of("type", "balance",

					"currency", request.currency(),

					"amount", request.amount());

			/*
			 * Duffel order data.
			 */
			List<Map<String, Object>> seatServices = selectAutomaticFreeSeats(request.offerId(), passengers);

			Map<String, Object> data = new HashMap<>();

			data.put("type", "instant");

			data.put("selected_offers", List.of(request.offerId()));

			data.put("payments", List.of(payment));

			data.put("passengers", passengers);

			if (!seatServices.isEmpty()) {
				data.put("services", seatServices);
				System.out.println("Automatically selected free seats: " + seatServices);
			} else {
				System.out.println("No free seat services available. Creating order without seat selection.");
			}

			Map<String, Object> body = Map.of("data", data);

			/*
			 * DEBUG LOG
			 */
			System.out.println("========== DUFFEL ORDER REQUEST ==========");

			System.out.println("Offer ID: " + request.offerId());

			System.out.println("Currency: " + request.currency());

			System.out.println("Amount: " + request.amount());

			System.out.println("Passengers: " + passengers);

			System.out.println("==========================================");
			var orderRequest = restClient.post().uri("/air/orders").contentType(MediaType.APPLICATION_JSON);

			if (request.bookingReference() != null && !request.bookingReference().isBlank()) {

				orderRequest.header("Idempotency-Key", "flight-booking-" + request.bookingReference());
			}

			Map<String, Object> response = orderRequest.body(body).retrieve().body(Map.class);

			System.out.println("========== DUFFEL ORDER RESPONSE ==========");

			System.out.println(response);

			System.out.println("===========================================");

			return response;

		} catch (org.springframework.web.client.RestClientResponseException e) {

			String responseBody = e.getResponseBodyAsString();

			System.err.println("Duffel order creation failed");
			System.err.println("HTTP Status: " + e.getStatusCode());
			System.err.println("Response: " + responseBody);

			if (e.getStatusCode().value() == 422 && responseBody.contains("offer_no_longer_available")) {

				throw new FlightApiException("FLIGHT_OFFER_UNAVAILABLE: This flight is no longer available. "
						+ "Search again and select a fresh offer.");
			}

			throw new FlightApiException("Duffel order creation failed: " + responseBody);
		} catch (Exception e) {

			System.err.println("========== DUFFEL ORDER ERROR ==========");

			e.printStackTrace();

			System.err.println("========================================");

			throw new FlightApiException("Unable to create Duffel order: " + e.getMessage());
		}
	}

	private List<Map<String, Object>> selectAutomaticFreeSeats(String offerId, List<Map<String, Object>> passengers) {

		List<Map<String, Object>> selectedServices = new ArrayList<>();

		try {
			Map<String, Object> response = restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/air/seat_maps").queryParam("offer_id", offerId).build())
					.retrieve().body(Map.class);

			System.out.println("========== DUFFEL SEAT MAP ==========");
			System.out.println(response);
			System.out.println("====================================");

			if (response == null)
				return selectedServices;

			Object dataObject = response.get("data");
			if (!(dataObject instanceof List<?> seatMaps))
				return selectedServices;

			for (Object seatMapObject : seatMaps) {
				if (!(seatMapObject instanceof Map<?, ?> seatMap))
					continue;

				Set<String> usedDesignators = new HashSet<>();
				Set<String> selectedPassengerIds = new HashSet<>();

				Object cabinsObject = seatMap.get("cabins");
				if (!(cabinsObject instanceof List<?> cabins))
					continue;

				for (Object cabinObject : cabins) {
					if (!(cabinObject instanceof Map<?, ?> cabin))
						continue;
					Object rowsObject = cabin.get("rows");
					if (!(rowsObject instanceof List<?> rows))
						continue;

					for (Object rowObject : rows) {
						if (!(rowObject instanceof Map<?, ?> row))
							continue;
						Object sectionsObject = row.get("sections");
						if (!(sectionsObject instanceof List<?> sections))
							continue;

						for (Object sectionObject : sections) {
							if (!(sectionObject instanceof Map<?, ?> section))
								continue;
							Object elementsObject = section.get("elements");
							if (!(elementsObject instanceof List<?> elements))
								continue;

							for (Object elementObject : elements) {
								if (!(elementObject instanceof Map<?, ?> element))
									continue;
								if (!"seat".equalsIgnoreCase(String.valueOf(element.get("type"))))
									continue;

								String designator = element.get("designator") == null ? null
										: String.valueOf(element.get("designator"));
								if (designator == null || designator.isBlank() || usedDesignators.contains(designator))
									continue;

								Object servicesObject = element.get("available_services");
								if (!(servicesObject instanceof List<?> services))
									continue;

								for (Object serviceObject : services) {
									if (!(serviceObject instanceof Map<?, ?> service))
										continue;

									Object serviceIdObject = service.get("id");
									Object passengerIdObject = service.get("passenger_id");
									Object amountObject = service.get("total_amount");
									if (serviceIdObject == null || passengerIdObject == null || amountObject == null)
										continue;

									String serviceId = String.valueOf(serviceIdObject);
									String passengerId = String.valueOf(passengerIdObject);

									if (!isFreeService(amountObject) || !isRequestedPassenger(passengers, passengerId)
											|| selectedPassengerIds.contains(passengerId))
										continue;

									Map<String, Object> selectedService = new HashMap<>();
									selectedService.put("quantity", 1);
									selectedService.put("id", serviceId);
									selectedServices.add(selectedService);
									selectedPassengerIds.add(passengerId);
									usedDesignators.add(designator);
									break;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			System.err
					.println("Seat-map lookup failed; continuing without automatic seat selection: " + e.getMessage());
		}

		return selectedServices;
	}

	private boolean isRequestedPassenger(List<Map<String, Object>> passengers, String passengerId) {
		for (Map<String, Object> passenger : passengers) {
			if (passengerId.equals(String.valueOf(passenger.get("id"))))
				return true;
		}
		return false;
	}

	private boolean isFreeService(Object amountObject) {
		try {
			return new java.math.BigDecimal(String.valueOf(amountObject)).compareTo(java.math.BigDecimal.ZERO) == 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private String normalizePhoneNumber(String phone) {

		if (phone == null || phone.isBlank()) {
			throw new FlightApiException("Passenger phone number is required");
		}

		String normalized = phone.trim();
		normalized = normalized.replaceAll("[\\s()\\-]", "");

		if (normalized.startsWith("0") && normalized.length() == 11) {
			normalized = "+91" + normalized.substring(1);
		}

		if (normalized.matches("\\d{10}")) {
			normalized = "+91" + normalized;
		}

		if (!normalized.startsWith("+")) {
			throw new FlightApiException("Invalid phone number. Use international format, e.g. +919876543210");
		}

		if (!normalized.matches("^\\+[1-9]\\d{7,14}$")) {
			throw new FlightApiException("Invalid phone number. Use international format, e.g. +919876543210");
		}

		return normalized;
	}

	public Map<String, Object> searchLocations(String query) {
		try {
			Map<String, Object> response = restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/places/suggestions").queryParam("query", query.trim()).build())
					.retrieve().body(Map.class);

			return response;

		} catch (org.springframework.web.client.RestClientResponseException e) {
			System.err.println("Duffel location search failed: " + e.getStatusCode());
			System.err.println(e.getResponseBodyAsString());

			throw new FlightApiException("Unable to search locations from provider");
		} catch (Exception e) {
			throw new FlightApiException("Unable to search locations: " + e.getMessage());
		}
	}

}
