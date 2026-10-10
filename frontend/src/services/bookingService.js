
import api from "./api";

export async function createBooking(bookingRequest) {
  const response = await api.post("/api/bookings", bookingRequest);
  return response.data;
}
