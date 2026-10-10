package com.flightbooking.flight.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.flightbooking.flight.client.CurrencyExchangeClient;
import com.flightbooking.flight.dto.CurrencyConversionResponse;

@Service
public class CurrencyConversionService {
	 private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
	            "INR",
	            "USD",
	            "EUR",
	            "GBP",
	            "AUD"
	    );

	    private final CurrencyExchangeClient currencyExchangeClient;

	    public CurrencyConversionService(
	            CurrencyExchangeClient currencyExchangeClient) {
	        this.currencyExchangeClient = currencyExchangeClient;
	    }

	    public BigDecimal convert(
	            BigDecimal amount,
	            String fromCurrency,
	            String toCurrency) {

	        String from = normalize(fromCurrency);
	        String to = normalize(toCurrency);

	        validateCurrency(from);
	        validateCurrency(to);

	        if (from.equals(to)) {
	            return amount.setScale(2, RoundingMode.HALF_UP);
	        }

	        CurrencyConversionResponse response =
	                currencyExchangeClient.getRate(from, to);

	        if (response == null) {
	            throw new IllegalStateException(
	                    "Unable to get exchange rate for "
	                            + from + " -> " + to);
	        }

	        BigDecimal rate = BigDecimal.valueOf(response.rate());

	        return amount
	                .multiply(rate)
	                .setScale(2, RoundingMode.HALF_UP);
	    }

	    public BigDecimal getRate(
	            String fromCurrency,
	            String toCurrency) {

	        String from = normalize(fromCurrency);
	        String to = normalize(toCurrency);

	        validateCurrency(from);
	        validateCurrency(to);

	        if (from.equals(to)) {
	            return BigDecimal.ONE;
	        }

	        CurrencyConversionResponse response =
	                currencyExchangeClient.getRate(from, to);

	        if (response == null) {
	            throw new IllegalStateException(
	                    "Unable to get exchange rate for "
	                            + from + " -> " + to);
	        }

	        return BigDecimal.valueOf(response.rate());
	    }

	    private String normalize(String currency) {
	        if (currency == null || currency.isBlank()) {
	            return "INR";
	        }

	        return currency.trim().toUpperCase(Locale.ROOT);
	    }

	    private void validateCurrency(String currency) {
	        if (!SUPPORTED_CURRENCIES.contains(currency)) {
	            throw new IllegalArgumentException(
	                    "Unsupported currency: " + currency
	                            + ". Supported currencies: "
	                            + SUPPORTED_CURRENCIES);
	        }
	    }
}
