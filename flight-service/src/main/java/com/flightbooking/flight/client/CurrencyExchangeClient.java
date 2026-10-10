package com.flightbooking.flight.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.flightbooking.flight.dto.CurrencyConversionResponse;

@Component
public class CurrencyExchangeClient {
    private final RestClient restClient;

    public CurrencyExchangeClient(
            @Value("${currency.base-url}") String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public CurrencyConversionResponse getRate(
            String from,
            String to) {

        return restClient.get()
                .uri("/v2/rate/{from}/{to}", from, to)
                .retrieve()
                .body(CurrencyConversionResponse.class);
    }
}
