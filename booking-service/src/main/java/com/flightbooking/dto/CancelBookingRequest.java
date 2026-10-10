package com.flightbooking.dto;

import jakarta.validation.constraints.Size;

public class CancelBookingRequest {
	  @Size(max = 500)
	  private String reason;

	  public CancelBookingRequest() {
	  }

	  public String getReason() {
	        return reason;
	  }

	  public void setReason(String reason) {
	        this.reason = reason;
	  }
}
