
import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { ArrowLeft, Plane, LoaderCircle } from "lucide-react";
import { createBooking } from "../services/bookingService";

const emptyPassenger = () => ({
  title: "",
  firstName: "",
  lastName: "",
  dateOfBirth: "",
  gender: "",
  nationality: "",
  email: "",
  phone: "",
  passportNumber: "",
});

function formatPrice(offer) {
  const amount = Number(offer?.displayPrice ?? offer?.price);
  const currency = offer?.displayCurrency ?? offer?.currency;

  if (!Number.isFinite(amount) || !currency) return "Fare shown after repricing";

  try {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency,
      maximumFractionDigits: 2,
    }).format(amount);
  } catch {
    return `${currency} ${amount.toFixed(2)}`;
  }
}

export default function Booking() {
  const location = useLocation();
  const navigate = useNavigate();

  const offer = location.state?.offer;
  const searchRequest = location.state?.searchRequest;

  const passengerCount = Math.min(
    9,
    Math.max(1, Number(searchRequest?.passengers) || 1)
  );

  const [passengers, setPassengers] = useState(
    () => Array.from({ length: passengerCount }, emptyPassenger)
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function updatePassenger(index, field, value) {
    setPassengers((current) =>
      current.map((passenger, i) =>
        i === index ? { ...passenger, [field]: value } : passenger
      )
    );
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (!offer?.offerId) {
      setError("Selected flight offer is missing. Please search again.");
      return;
    }

    if (passengers.length !== passengerCount) {
      setError("Passenger count does not match your flight search.");
      return;
    }

    setLoading(true);

    try {
      const booking = await createBooking({
  offerId: offer.offerId,
  currency: offer.displayCurrency || offer.currency,

  passengers: passengers.map((passenger) => ({
    title: passenger.title,
    passengerType: "adult",
    firstName: passenger.firstName.trim(),
    lastName: passenger.lastName.trim(),
    dateOfBirth: passenger.dateOfBirth,
    gender: passenger.gender,
    nationality: passenger.nationality.trim(),
    email: passenger.email.trim(),
    phone: passenger.phone.trim(),
    passportNumber: passenger.passportNumber.trim() || null,
  })),
});

navigate("/payment", {
  state: {
    booking,
    offer,
    bookingReference: booking.bookingReference,
  },
});
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          "Booking could not be created. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }

  if (!offer || !searchRequest) {
    return (
      <main className="mx-auto max-w-3xl px-4 py-16">
        <h1 className="text-2xl font-bold text-slate-900">
          No flight selected
        </h1>
        <p className="mt-2 text-slate-600">
          Select a flight before entering passenger details.
        </p>
        <button
          onClick={() => navigate("/")}
          className="mt-5 rounded-xl bg-blue-600 px-5 py-3 font-semibold text-white"
        >
          Search flights
        </button>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-slate-50 px-4 py-8">
      <div className="mx-auto max-w-4xl">
        <button
          type="button"
        onClick={() => navigate(-1)}
          className="mb-5 flex items-center gap-2 text-sm font-semibold text-blue-700"
        >
          <ArrowLeft size={17} />
          Back to flight results
        </button>

        <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl">
          Passenger details
        </h1>
        <p className="mt-2 text-slate-600">
          Enter the details exactly as shown on each passenger’s travel
          document.
        </p>

        <section className="mt-6 rounded-2xl bg-slate-900 p-5 text-white">
          <div className="flex items-start gap-3">
            <Plane size={23} className="mt-1 shrink-0" />
            <div className="min-w-0 flex-1">
              <p className="font-semibold">
                {offer.origin} → {offer.destination}
              </p>
              <p className="mt-1 text-sm text-slate-300">
                {offer.airline || "Airline details"}
              </p>
              <p className="mt-3 text-xl font-bold">
                {formatPrice(offer)}
              </p>
              <p className="mt-1 text-xs text-slate-300">
                Displayed offer price; final fare is subject to backend
                repricing.
              </p>
            </div>
          </div>
        </section>

        <form onSubmit={handleSubmit} className="mt-6 space-y-6">
          {passengers.map((passenger, index) => (
            <section
              key={index}
              className="rounded-2xl border border-slate-200 bg-white p-5 sm:p-6"
            >
              <h2 className="mb-5 text-lg font-bold text-slate-900">
                Passenger {index + 1}
              </h2>

              <div className="grid gap-4 sm:grid-cols-2">
                <label className="text-sm font-medium text-slate-700">
  Title *
  <select
    required
    value={passenger.title}
    onChange={(e) =>
      updatePassenger(index, "title", e.target.value)
    }
    className="mt-2 w-full rounded-xl border border-slate-300 p-3"
  >
    <option value="">Select title</option>
    <option value="MR">Mr</option>
    <option value="MS">Ms</option>
    <option value="MRS">Mrs</option>
  </select>
</label>
                <label className="text-sm font-medium text-slate-700">
                  First name *
                  <input
                    required
                    autoComplete="given-name"
                    value={passenger.firstName}
                    onChange={(e) =>
                      updatePassenger(index, "firstName", e.target.value)
                    }
                    className="mt-2 w-full rounded-xl border border-slate-300 p-3 outline-none focus:border-blue-500"
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  Last name *
                  <input
                    required
                    autoComplete="family-name"
                    value={passenger.lastName}
                    onChange={(e) =>
                      updatePassenger(index, "lastName", e.target.value)
                    }
                    className="mt-2 w-full rounded-xl border border-slate-300 p-3 outline-none focus:border-blue-500"
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  Date of birth *
                  <input
                    required
                    type="date"
                    max={new Date().toISOString().slice(0, 10)}
                    value={passenger.dateOfBirth}
                    onChange={(e) =>
                      updatePassenger(index, "dateOfBirth", e.target.value)
                    }
                    className="mt-2 w-full rounded-xl border border-slate-300 p-3 outline-none focus:border-blue-500"
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  Gender *
                  <select
                    required
                    value={passenger.gender}
                    onChange={(e) =>
                      updatePassenger(index, "gender", e.target.value)
                    }
                    className="mt-2 w-full rounded-xl border border-slate-300 p-3 outline-none focus:border-blue-500"
                  >
                    <option value="">Select gender</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </label>

                <label className="text-sm font-medium text-slate-700">
                  Nationality *
                  <input
                    required
                    autoComplete="country-name"
                    value={passenger.nationality}
                    onChange={(e) =>
                      updatePassenger(index, "nationality", e.target.value)
                    }
                    placeholder="Enter nationality"
                    className="mt-2 w-full rounded-xl border border-slate-300 p-3 outline-none focus:border-blue-500"
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  Email *
                  <input
                    required
                    type="email"
                    autoComplete="email"
                    value={passenger.email}
                    onChange={(e) =>
                      updatePassenger(index, "email", e.target.value)
                    }
                    className="mt-2 w-full rounded-xl border border-slate-300 p-3 outline-none focus:border-blue-500"
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  Phone *
                  <input
                    required
                    type="tel"
                    autoComplete="tel"
                    value={passenger.phone}
                    onChange={(e) =>
                      updatePassenger(index, "phone", e.target.value)
                    }
                    className="mt-2 w-full rounded-xl border border-slate-300 p-3 outline-none focus:border-blue-500"
                  />
                </label>

                <label className="text-sm font-medium text-slate-700">
                  Passport number
                  <input
                    value={passenger.passportNumber}
                    onChange={(e) =>
                      updatePassenger(index, "passportNumber", e.target.value)
                    }
                    autoComplete="off"
                    className="mt-2 w-full rounded-xl border border-slate-300 p-3 outline-none focus:border-blue-500"
                  />
                </label>
              </div>
            </section>
          ))}

          {error && (
            <div
              role="alert"
              className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
            >
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 px-5 py-4 font-bold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loading && <LoaderCircle className="animate-spin" size={18} />}
            {loading ? "Creating booking..." : "Continue to payment"}
          </button>
        </form>
      </div>
    </main>
  );
}
