import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { CheckCircle2, Clock, Plane, Ticket } from "lucide-react";
import "./PaymentSuccess.css";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";

export default function PaymentSuccess() {
  const location = useLocation();

  const bookingReference =
    location.state?.bookingReference ||
    new URLSearchParams(location.search).get("bookingReference");

  const [status, setStatus] = useState("checking");
  const [message, setMessage] = useState(
    "Verifying payment and booking confirmation..."
  );

  useEffect(() => {
    let cancelled = false;
    let timer;

    async function verifyPayment() {
      if (!bookingReference) {
        setStatus("pending");
        setMessage(
          "Booking reference is missing. Open My Bookings to check your booking."
        );
        return;
      }

      const token = sessionStorage.getItem("accessToken");

      if (!token) {
        setStatus("pending");
        setMessage("Please log in to check your payment status.");
        return;
      }

      try {
        const response = await fetch(
          `${API_BASE_URL}/api/payments/${encodeURIComponent(
            bookingReference
          )}/status`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!response.ok) {
          throw new Error("Could not retrieve payment status.");
        }

        const data = await response.json();

        if (cancelled) return;

        if (data.status === "SUCCEEDED") {
          setStatus("success");
          setMessage(
            "Payment received. Your booking confirmation is being checked."
          );
          return;
        }

        if (
          data.status === "FAILED" ||
          data.status === "CANCELLED"
        ) {
          setStatus("failed");
          setMessage(
            "Payment was not completed. Check your booking before trying again."
          );
          return;
        }

        setStatus("checking");
        setMessage(
          "Payment is processing. Waiting for confirmation..."
        );

        // Webhook may take a short time to update the database.
        timer = setTimeout(verifyPayment, 3000);
      } catch {
        if (cancelled) return;

        setStatus("pending");
        setMessage(
          "Payment status could not be verified. Please check My Bookings."
        );
      }
    }

    verifyPayment();

    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [bookingReference]);

  const isSuccess = status === "success";
  const isFailed = status === "failed";

  return (
    <main className="payment-success-page">
      <section className="payment-success-card">
        <div className={`success-icon ${isSuccess ? "confirmed" : ""}`}>
          {isSuccess ? (
            <CheckCircle2 size={42} />
          ) : (
            <Clock size={42} />
          )}
        </div>

        <h1>
          {isSuccess
            ? "Payment successful!"
            : isFailed
              ? "Payment failed"
              : "Payment confirmation pending"}
        </h1>

        <p className="success-message">{message}</p>

        {bookingReference && (
          <div className="success-reference">
            <span>Booking reference</span>
            <strong>{bookingReference}</strong>
          </div>
        )}

        <div className="success-note">
          <Ticket size={20} />
          <p>
            Payment success does not by itself confirm the airline booking.
            Check My Bookings for the latest booking status and ticket.
          </p>
        </div>

        <div className="success-actions">
          <Link to="/bookings" className="success-primary">
            <Plane size={18} />
            View my bookings
          </Link>

          <Link to="/flights" className="success-secondary">
            Explore flights
          </Link>
        </div>
      </section>
    </main>
  );
}