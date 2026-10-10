import React, { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import {
  ArrowLeft,
  Plane,
  CalendarDays,
  MapPin,
  Users,
  CreditCard,
  Ticket,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Clock,
} from "lucide-react";
import api from "../services/api";

const firstDefined = (...values) =>
  values.find((value) => value !== undefined && value !== null && value !== "");

const formatDate = (value) => {
  if (!value) return "Not available";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
};

const formatMoney = (amount, currency = "INR") => {
  if (amount === undefined || amount === null || amount === "")
    return "Not available";
  const numericAmount = Number(amount);
  if (!Number.isFinite(numericAmount)) return `${amount} ${currency}`;
  try {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: currency || "INR",
      maximumFractionDigits: 2,
    }).format(numericAmount);
  } catch {
    return `${numericAmount.toFixed(2)} ${currency || "INR"}`;
  }
};

const getStatusStyle = (status) => {
  const normalized = String(status || "").toUpperCase();
  if (["CONFIRMED", "SUCCESS", "PAID", "COMPLETED"].includes(normalized)) {
    return "bg-emerald-50 text-emerald-700 ring-emerald-200";
  }
  if (["PENDING", "PENDING_PAYMENT", "PROCESSING"].includes(normalized)) {
    return "bg-amber-50 text-amber-700 ring-amber-200";
  }
  if (["FAILED", "CANCELLED", "CANCELED", "EXPIRED"].includes(normalized)) {
    return "bg-red-50 text-red-700 ring-red-200";
  }
  return "bg-slate-100 text-slate-700 ring-slate-200";
};

function DetailRow({ label, value }) {
  return (
    <div className="flex flex-col gap-1 border-b border-slate-100 py-3 last:border-b-0 sm:flex-row sm:items-start sm:justify-between">
      <span className="text-sm text-slate-500">{label}</span>
      <span className="break-words text-sm font-semibold text-slate-900 sm:max-w-[65%] sm:text-right">
        {value || "Not available"}
      </span>
    </div>
  );
}

export default function BookingDetails() {
  const { bookingReference } = useParams();
  const navigate = useNavigate();
  const [booking, setBooking] = useState(null);
  const [passengers, setPassengers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");
  const [payment, setPayment] = useState(null);
  const [paymentError, setPaymentError] = useState("");

  const loadDetails = useCallback(
    async (isRefresh = false) => {
      if (isRefresh) setRefreshing(true);
      else setLoading(true);
      setError("");

      try {
        // Booking and passenger data are essential; payment status is loaded
        // independently so a payment-service/network error won't hide the booking.
        const [bookingResponse, passengerResponse] = await Promise.all([
          api.get(`/api/bookings/${encodeURIComponent(bookingReference)}`),
          api.get(
            `/api/bookings/${encodeURIComponent(bookingReference)}/passengers`,
          ),
        ]);

        const bookingData =
          bookingResponse?.data?.data ?? bookingResponse?.data;
        const passengerData =
          passengerResponse?.data?.data ?? passengerResponse?.data;

        setBooking(bookingData);
        setPassengers(Array.isArray(passengerData) ? passengerData : []);

        try {
          const paymentResponse = await api.get(
            `/api/payments/${encodeURIComponent(bookingReference)}/status`,
          );
          const paymentData =
            paymentResponse?.data?.data ?? paymentResponse?.data;
          setPayment(paymentData);
          setPaymentError("");
        } catch (paymentRequestError) {
          setPayment(null);
          setPaymentError(
            paymentRequestError?.response?.status === 404
              ? "No payment record found"
              : "Payment status unavailable",
          );
        }
      } catch (requestError) {
        const status = requestError?.response?.status;
        if (status === 401 || status === 403) {
          navigate("/login", {
            replace: true,
            state: { from: `/bookings/${bookingReference}` },
          });
          return;
        }
        if (status === 404) {
          setError(
            "Booking not found, or you don't have access to this booking.",
          );
        } else {
          setError(
            requestError?.response?.data?.message ||
              "We couldn't load this booking right now. Please try again.",
          );
        }
        setBooking(null);
        setPassengers([]);
        setPayment(null);
        setPaymentError("Payment status unavailable");
      } finally {
        setLoading(false);
        setRefreshing(false);
      }
    },
    [bookingReference, navigate],
  );

  useEffect(() => {
    if (!bookingReference) {
      setError("Booking reference is missing.");
      setLoading(false);
      return;
    }
    loadDetails();
  }, [bookingReference, loadDetails]);

  const reference = firstDefined(
    booking?.bookingReference,
    booking?.reference,
    bookingReference,
  );
  const origin = firstDefined(
    booking?.origin,
    booking?.originCity,
    booking?.source,
    booking?.departureAirport,
  );
  const destination = firstDefined(
    booking?.destination,
    booking?.destinationCity,
    booking?.arrivalAirport,
  );
  const status = firstDefined(
    booking?.status,
    booking?.bookingStatus,
    "UNKNOWN",
  );
  const currency = firstDefined(booking?.currency, "INR");
  const amount = firstDefined(
    booking?.amount,
    booking?.totalAmount,
    booking?.totalPrice,
    booking?.price,
  );
  const departure = firstDefined(
    booking?.departureTime,
    booking?.departAt,
    booking?.departureDateTime,
  );
  const arrival = firstDefined(
    booking?.arrivalTime,
    booking?.arriveAt,
    booking?.arrivalDateTime,
  );
  const airline = firstDefined(
    booking?.airlineName,
    booking?.airline,
    booking?.carrierName,
  );
  const flightNumber = firstDefined(booking?.flightNumber, booking?.flightNo);
  const bookedAt = firstDefined(
    booking?.createdAt,
    booking?.bookingDate,
    booking?.createdDate,
  );
  const passengerCount = firstDefined(
    booking?.passengerCount,
    booking?.numberOfPassengers,
    passengers.length,
  );

  if (loading) {
    return (
      <main className="min-h-[70vh] bg-slate-50 px-4 py-10">
        <div className="mx-auto max-w-4xl animate-pulse space-y-5">
          <div className="h-5 w-36 rounded bg-slate-200" />
          <div className="h-10 w-64 rounded bg-slate-200" />
          <div className="h-48 rounded-2xl bg-white shadow-sm" />
          <div className="h-56 rounded-2xl bg-white shadow-sm" />
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-slate-50 px-4 py-8 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-4xl">
        <Link
          to="/bookings"
          className="mb-6 inline-flex items-center gap-2 text-sm font-medium text-slate-600 transition hover:text-blue-700"
        >
          <ArrowLeft size={17} /> Back to My Bookings
        </Link>

        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-[0.18em] text-blue-700">
              Booking details
            </p>
            <h1 className="text-3xl font-extrabold tracking-tight text-slate-950 sm:text-4xl">
              Your trip
            </h1>
            <p className="mt-2 break-all text-sm text-slate-500">
              Reference:{" "}
              <span className="font-semibold text-slate-700">{reference}</span>
            </p>
          </div>
          <button
            type="button"
            onClick={() => loadDetails(true)}
            disabled={refreshing}
            className="inline-flex items-center justify-center gap-2 self-start rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60 sm:self-auto"
          >
            <RefreshCw size={16} className={refreshing ? "animate-spin" : ""} />
            Refresh
          </button>
        </div>

        {error ? (
          <section className="rounded-2xl border border-red-200 bg-red-50 p-5 text-red-800">
            <div className="flex items-start gap-3">
              <AlertCircle className="mt-0.5 shrink-0" size={20} />
              <div>
                <p className="font-semibold">{error}</p>
                <button
                  type="button"
                  onClick={() => loadDetails()}
                  className="mt-3 text-sm font-semibold underline underline-offset-4"
                >
                  Try again
                </button>
              </div>
            </div>
          </section>
        ) : booking ? (
          <div className="space-y-5">
            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-7">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div className="flex items-start gap-3">
                  <div className="rounded-xl bg-blue-50 p-3 text-blue-700">
                    <Plane size={23} />
                  </div>
                  <div>
                    <h2 className="text-lg font-bold text-slate-950">
                      {origin && destination
                        ? `${origin} to ${destination}`
                        : "Flight reservation"}
                    </h2>
                    <p className="mt-1 text-sm text-slate-500">
                      {[airline, flightNumber].filter(Boolean).join(" · ") ||
                        "Flight information"}
                    </p>
                  </div>
                </div>
                <span
                  className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-bold ring-1 ring-inset ${getStatusStyle(status)}`}
                >
                  {String(status).toUpperCase() === "CONFIRMED" ? (
                    <CheckCircle2 size={14} />
                  ) : (
                    <Clock size={14} />
                  )}
                  {String(status).replaceAll("_", " ")}
                </span>
              </div>

              <div className="my-6 grid gap-4 rounded-xl bg-slate-50 p-4 sm:grid-cols-[1fr_auto_1fr] sm:items-center sm:p-5">
                <div>
                  <p className="mb-1 text-xs text-slate-500">From</p>
                  <p className="break-words text-lg font-bold text-slate-950">
                    {origin || "Not available"}
                  </p>
                  <p className="mt-1 text-sm text-slate-500">
                    {formatDate(departure)}
                  </p>
                </div>
                <div className="hidden items-center justify-center text-blue-600 sm:flex">
                  <div className="h-px w-12 bg-slate-300" />
                  <Plane size={18} className="mx-2" />
                  <div className="h-px w-12 bg-slate-300" />
                </div>
                <div className="sm:text-right">
                  <p className="mb-1 text-xs text-slate-500">To</p>
                  <p className="break-words text-lg font-bold text-slate-950">
                    {destination || "Not available"}
                  </p>
                  <p className="mt-1 text-sm text-slate-500">
                    {formatDate(arrival)}
                  </p>
                </div>
              </div>

              <div className="grid gap-4 sm:grid-cols-3">
                <div className="flex items-center gap-3">
                  <CalendarDays className="shrink-0 text-blue-700" size={19} />
                  <div>
                    <p className="text-xs text-slate-500">Booked on</p>
                    <p className="mt-0.5 text-sm font-semibold text-slate-900">
                      {bookedAt ? formatDate(bookedAt) : "Not provided by API"}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Users className="shrink-0 text-blue-700" size={19} />
                  <div>
                    <p className="text-xs text-slate-500">Passengers</p>
                    <p className="mt-0.5 text-sm font-semibold text-slate-900">
                      {passengerCount}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <CreditCard className="shrink-0 text-blue-700" size={19} />
                  <div>
                    <p className="text-xs text-slate-500">Total amount</p>
                    <p className="mt-0.5 text-sm font-semibold text-slate-900">
                      {formatMoney(amount, currency)}
                    </p>
                  </div>
                </div>
              </div>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-7">
              <div className="mb-3 flex items-center gap-2">
                <Ticket size={19} className="text-blue-700" />
                <h2 className="text-lg font-bold text-slate-950">
                  Reservation information
                </h2>
              </div>
              <DetailRow label="Booking reference" value={reference} />
              <DetailRow
                label="Booking status"
                value={String(status).replaceAll("_", " ")}
              />
              <DetailRow label="Airline" value={airline} />
              <DetailRow
                label="Flight number"
                value={booking?.flightNumber || "Not available"}
              />
              <DetailRow label="Flight offer ID" value={booking?.offerId} />
              <DetailRow
                label="Airline booking reference"
                value={firstDefined(
                  booking?.airlineBookingReference,
                  booking?.airlineReference,
                )}
              />
              <DetailRow
                label="Flight fare"
                value={formatMoney(
                  booking?.totalAmount,
                  booking?.currency || "INR",
                )}
              />
              <DetailRow
                label="Payment amount"
                value={
                  payment
                    ? formatMoney(payment.amount, payment.currency)
                    : formatMoney(
                        booking?.paymentAmount,
                        booking?.paymentCurrency || "INR",
                      )
                }
              />
              <DetailRow
                label="Payment status"
                value={
                  payment?.status
                    ? String(payment.status).replaceAll("_", " ")
                    : paymentError || "Not available"
                }
              />
              <DetailRow
                label="Booked on"
                value={
                  bookedAt
                    ? formatDate(bookedAt)
                    : "Booking date not provided by booking API"
                }
              />
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-7">
              <div className="mb-4 flex items-center gap-2">
                <Users size={19} className="text-blue-700" />
                <h2 className="text-lg font-bold text-slate-950">Passengers</h2>
              </div>
              {passengers.length ? (
                <div className="divide-y divide-slate-100">
                  {passengers.map((passenger, index) => {
                    const name = firstDefined(
                      passenger?.fullName,
                      passenger?.name,
                      [passenger?.firstName, passenger?.lastName]
                        .filter(Boolean)
                        .join(" "),
                    );
                    return (
                      <div
                        key={
                          passenger?.id ??
                          passenger?.passengerId ??
                          `${name || "passenger"}-${index}`
                        }
                        className="flex items-start gap-3 py-4 first:pt-0 last:pb-0"
                      >
                        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-blue-50 text-sm font-bold text-blue-700">
                          {index + 1}
                        </div>
                        <div className="min-w-0 flex-1">
                          <p className="break-words font-semibold text-slate-900">
                            {name || `Passenger ${index + 1}`}
                          </p>
                          <p className="mt-1 text-sm text-slate-500">
                            {[
                              passenger?.type,
                              passenger?.passengerType,
                              passenger?.gender,
                            ]
                              .filter(Boolean)
                              .join(" · ") || "Passenger"}
                          </p>
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <p className="rounded-xl bg-slate-50 px-4 py-5 text-sm text-slate-500">
                  Passenger details are not available for this booking.
                </p>
              )}
            </section>

            <p className="flex items-start gap-2 text-xs leading-5 text-slate-500">
              <MapPin size={15} className="mt-0.5 shrink-0" />
              Details shown here come from your booking APIs. Fields not
              returned by the backend are displayed as unavailable.
            </p>
          </div>
        ) : null}
      </div>
    </main>
  );
}
