import { useEffect, useState } from "react";
import { loadStripe } from "@stripe/stripe-js";
import {
  Elements,
  PaymentElement,
  useElements,
  useStripe,
} from "@stripe/react-stripe-js";
import { useLocation, useNavigate } from "react-router-dom";
import { ShieldCheck, CreditCard, Plane } from "lucide-react";

import "./Payment.css";

const stripePromise = loadStripe(
  import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY
);

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";

function CheckoutForm({ clientSecret, payment }) {
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handlePayment = async (event) => {
    event.preventDefault();

    if (!stripe || !elements) return;

    setLoading(true);
    setError("");

    const { error: submitError } = await elements.submit();

    if (submitError) {
      setError(submitError.message);
      setLoading(false);
      return;
    }

    const { error: paymentError } = await stripe.confirmPayment({
      elements,
      clientSecret,
      confirmParams: {
  return_url: `${window.location.origin}/payment-success?bookingReference=${encodeURIComponent(
    payment.bookingReference
  )}`,
},
      redirect: "if_required",
    });

    if (paymentError) {
      setError(paymentError.message || "Payment could not be completed.");
      setLoading(false);
      return;
    }

    // Stripe confirmation alone does not confirm the booking.
    // The backend webhook remains the source of truth.
    navigate("/payment-success", {
      state: {
        bookingReference: payment.bookingReference,
        paymentIntentId: payment.stripePaymentIntentId,
      },
    });
  };

  return (
    <form onSubmit={handlePayment} className="stripe-form">
      <PaymentElement />

      {error && <p className="payment-error">{error}</p>}

      <button
        type="submit"
        className="pay-button"
        disabled={!stripe || !elements || loading}
      >
        {loading ? "Processing..." : "Pay securely"}
      </button>

      <p className="secure-note">
        <ShieldCheck size={17} />
        Secure payment powered by Stripe
      </p>
    </form>
  );
}

export default function Payment() {
  const location = useLocation();

  const bookingReference =
    location.state?.bookingReference ||
    new URLSearchParams(location.search).get("bookingReference");

  const [payment, setPayment] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function createPayment() {
      if (!bookingReference) {
        setError("Booking reference missing. Please start from your booking.");
        setLoading(false);
        return;
      }

      try {
const token = sessionStorage.getItem("accessToken");

if (!token) {
  throw new Error("Session expired. Please log in again.");
}

const response = await fetch(`${API_BASE_URL}/api/payments`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  },
  body: JSON.stringify({ bookingReference }),
});

const data = await response.json();

if (!response.ok) {
  throw new Error(
    data.message || data.error || `Payment request failed (${response.status})`
  );
}

if (!data.clientSecret) {
  throw new Error("Payment Service did not return a Stripe client secret.");
}

setPayment(data);

        if (!cancelled) setPayment(data);
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "Something went wrong.");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    createPayment();

    return () => {
      cancelled = true;
    };
  }, [bookingReference]);

  const options = payment?.clientSecret
    ? {
        clientSecret: payment.clientSecret,
        appearance: {
          theme: "stripe",
          variables: {
            colorPrimary: "#2563eb",
            borderRadius: "10px",
          },
        },
      }
    : undefined;

  return (
    <main className="payment-page">
      <div className="payment-container">
        <div className="payment-heading">
          <span className="payment-icon">
            <CreditCard size={24} />
          </span>
          <div>
            <h1>Complete your payment</h1>
            <p>Secure your flight booking in a few simple steps.</p>
          </div>
        </div>

        {loading && (
          <div className="payment-message">Preparing secure checkout...</div>
        )}

        {error && (
          <div className="payment-message payment-error">
            {error}
          </div>
        )}

        {!loading && payment && options && (
          <div className="payment-layout">
            <section className="payment-card">
              <div className="section-heading">
                <h2>Payment details</h2>
                <span className="test-badge">Stripe checkout</span>
              </div>

              <Elements stripe={stripePromise} options={options}>
                <CheckoutForm
                  clientSecret={payment.clientSecret}
                  payment={payment}
                />
              </Elements>
            </section>

            <aside className="booking-summary">
              <div className="summary-heading">
                <Plane size={21} />
                <h2>Booking summary</h2>
              </div>

              <p className="summary-label">Booking reference</p>
              <p className="booking-reference">{payment.bookingReference}</p>

              <div className="summary-divider" />

              <p className="summary-label">Amount to pay</p>
              <h2 className="payment-amount">
                {payment.currency}{" "}
                {Number(payment.amount).toLocaleString("en-IN", {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2,
                })}
              </h2>

              <p className="summary-footnote">
                The final amount is determined by the Booking Service.
              </p>
            </aside>
          </div>
        )}
      </div>
    </main>
  );
}
