package com.flightbooking.flight.controller;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.flightbooking.flight.dto.CreateDuffelOrderRequest;
import com.flightbooking.flight.dto.DuffelOrderResponse;
import com.flightbooking.flight.dto.FlightOfferDetails;
import com.flightbooking.flight.dto.FlightSearchRequest;
import com.flightbooking.flight.dto.FlightSearchResponse;
import com.flightbooking.flight.service.CurrencyConversionService;
import com.flightbooking.flight.service.FlightService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;
    private final CurrencyConversionService currencyConversionService;

    public FlightController(
            FlightService flightService,
            CurrencyConversionService currencyConversionService) {

        this.flightService = flightService;
        this.currencyConversionService = currencyConversionService;
    }

    // Search flights
    @PostMapping("/search")
    public ResponseEntity<FlightSearchResponse> searchFlights(
            @Valid @RequestBody FlightSearchRequest request) {

        return ResponseEntity.ok(
                flightService.searchFlights(request));
    }

    // Get a specific offer
    @GetMapping("/offers/{offerId}")
    public ResponseEntity<FlightOfferDetails> getOffer(
            @PathVariable String offerId) {

        return ResponseEntity.ok(
                flightService.getOffer(offerId));
    }

    // Reprice an offer before payment
    @PostMapping("/offers/{offerId}/price")
    public ResponseEntity<FlightOfferDetails> repriceOffer(
            @PathVariable String offerId) {

        return ResponseEntity.ok(
                flightService.repriceOffer(offerId));
    }

    // Currency conversion
    @GetMapping("/currency/convert")
    public ResponseEntity<Map<String, Object>> convertCurrency(
            @RequestParam BigDecimal amount,
            @RequestParam String from,
            @RequestParam String to) {

        BigDecimal convertedAmount =
                currencyConversionService.convert(
                        amount, from, to);

        BigDecimal exchangeRate =
                currencyConversionService.getRate(
                        from, to);

        Map<String, Object> response = new HashMap<>();

        response.put("amount", amount);
        response.put("from", from.toUpperCase());
        response.put("to", to.toUpperCase());
        response.put("rate", exchangeRate);
        response.put("convertedAmount", convertedAmount);

        return ResponseEntity.ok(response);
    }

    // Create Duffel order
    @PostMapping("/orders")
    public ResponseEntity<DuffelOrderResponse> createOrder(
            @RequestBody CreateDuffelOrderRequest request) {

        return ResponseEntity.ok(
                flightService.createOrder(request));
    }

    // Search airport/location suggestions
    @GetMapping("/locations")
    public ResponseEntity<Map<String, Object>> searchLocations(
            @RequestParam String query) {

        return ResponseEntity.ok(
                flightService.searchLocations(query));
    }
}

