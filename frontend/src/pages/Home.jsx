// import React from 'react'
// import { useState } from 'react'
// import { Plane, CalendarDays, Users, ArrowRightLeft,
//         Search, ShieldCheck, BadgeCheck, Headphones
//  } from 'lucide-react'
// import { searchFlights } from '../services/flightService'
// import { useNavigate } from 'react-router-dom'
// import LocationAutocomplete from "../components/LocationAutocomplete";

// export default function Home() {
//   const navigate = useNavigate();

//   // const [form, setForm] = useState({
//   //   origin: "",
//   //   destination: "",
//   //   departureDate: "",
//   //   passengers: "",
//   //   currency: "",
//   // });
//   const [form, setForm] = useState({
//   origin: null,
//   destination: null,
//   departureDate: "",
//   passengers: "",
//   currency: "",
// });

//   const [loading, setLoading] = useState(false);
//   const [error, setError] = useState("");

//   function updateLocation(field, location) {
//   setForm((previous) => ({
//     ...previous,
//     [field]: location,
//   }));
// }

//   function updateField(event) {
//     const { name, value } = event.target;
//     setForm((previous) => ({ ...previous, [name]: value }));
//   }

//   function swapAirports() {
//     setForm((previous) => ({
//       ...previous,
//       origin: previous.destination,
//       destination: previous.origin,
//     }));
//   }

//   async function handleSearch(event) {
//     event.preventDefault();
//     setError("");

//     if (
//       !form.origin.trim() ||
//       !form.destination.trim() ||
//       !form.departureDate ||
//       !form.passengers ||
//       !form.currency.trim()
//     ) {
//       setError("Please complete all flight search fields.");
//       return;
//     }

//     if (form.origin.trim().toUpperCase() ===
//         form.destination.trim().toUpperCase()) {
//       setError("Origin and destination must be different.");
//       return;
//     }

//     if (Number(form.passengers) < 1 ||
//         !Number.isInteger(Number(form.passengers))) {
//       setError("Enter a valid passenger count.");
//       return;
//     }

//     try {
//       setLoading(true);

//       const request = {
//         origin: form.origin.trim().toUpperCase(),
//         destination: form.destination.trim().toUpperCase(),
//         departureDate: form.departureDate,
//         passengers: Number(form.passengers),
//         currency: form.currency.trim().toUpperCase(),
//       };

//       const results = await searchFlights(request);

//       navigate("/flights", {
//         state: {
//           results,
//           searchRequest: request,
//         },
//       });
//     } catch (err) {
//       const message =
//         err.response?.data?.message ||
//         err.response?.data?.error ||
//         "Flight search failed. Please try again.";

//       setError(message);
//     } finally {
//       setLoading(false);
//     }
//   }

//   return (
//     <main>
//       <section className="hero">
//         <div className="hero-overlay" />
//         <div className="container-app hero-content">
//           <span className="hero-tag">
//             <Plane size={15} /> Your journey starts here
//           </span>

//           <h1>
//             Explore the world
//             <br />
//             with <span>SkyGo</span>
//           </h1>

//           <p>
//             Search flights, compare available fares and manage
//             your journey in one place.
//           </p>
//         </div>
//       </section>

//       <section className="search-panel">
//         <div className="trip-tabs">
//           <button type="button" className="trip-tab active">
//             One Way
//           </button>
//           <button type="button" className="trip-tab" disabled>
//             Round Trip
//           </button>
//           <button type="button" className="trip-tab" disabled>
//             Multi City
//           </button>
//         </div>

//         <form onSubmit={handleSearch}>
//           <div className="search-grid">
//             <Field
//               icon={<Plane size={18} />}
//               label="From"
//               name="origin"
//               value={form.origin}
//               onChange={updateField}
//               placeholder="Airport code"
//               autoComplete="off"
//             />

//             <button
//               type="button"
//               className="swap-button"
//               onClick={swapAirports}
//               aria-label="Swap origin and destination"
//             >
//               <ArrowRightLeft size={18} />
//             </button>

//             <Field
//               icon={<Plane size={18} />}
//               label="To"
//               name="destination"
//               value={form.destination}
//               onChange={updateField}
//               placeholder="Airport code"
//               autoComplete="off"
//             />

//             <Field
//               icon={<CalendarDays size={18} />}
//               label="Departure"
//               name="departureDate"
//               type="date"
//               value={form.departureDate}
//               onChange={updateField}
//             />

//             <Field
//               icon={<Users size={18} />}
//               label="Passengers"
//               name="passengers"
//               type="number"
//               value={form.passengers}
//               onChange={updateField}
//               placeholder="Number"
//               min="1"
//               step="1"
//             />

//             <Field
//               label="Currency"
//               name="currency"
//               value={form.currency}
//               onChange={updateField}
//               placeholder="Currency code"
//               autoComplete="off"
//             />
//           </div>

//           {error && (
//             <p role="alert" className="error-message">
//               {error}
//             </p>
//           )}

//           <button
//             type="submit"
//             className="primary-button search-button"
//             disabled={loading}
//           >
//             <Search size={18} />
//             {loading ? "Searching flights..." : "Search Flights"}
//           </button>
//         </form>
//       </section>

//       <section className="container-app benefits">
//         <Benefit
//           icon={<ShieldCheck />}
//           title="Secure payments"
//           description="Payment handled by the connected payment service."
//         />
//         <Benefit
//           icon={<BadgeCheck />}
//           title="Clear booking flow"
//           description="Review your details before creating a booking."
//         />
//         <Benefit
//           icon={<Headphones />}
//           title="Travel support"
//           description="Find help for your booking and journey."
//         />
//       </section>
//     </main>
//   );
// }

// function Field({
//   icon,
//   label,
//   name,
//   value,
//   onChange,
//   type = "text",
//   placeholder,
//   min,
//   step,
//   autoComplete,
// }) {
//   return (
//     <label className="field">
//       <span className="field-label">
//         {icon}
//         {label}
//       </span>

//       <input
//         required
//         name={name}
//         type={type}
//         value={value}
//         onChange={onChange}
//         placeholder={placeholder || ""}
//         min={min}
//         step={step}
//         autoComplete={autoComplete}
//       />
//     </label>
//   );
// }

// function Benefit({ icon, title, description }) {
//   return (
//     <article className="benefit-card">
//       <div className="benefit-icon">{icon}</div>
//       <div>
//         <h2>{title}</h2>
//         <p>{description}</p>
//       </div>
//     </article>
//   );
// }



import { useState } from "react";
import {
  Plane,
  CalendarDays,
  Users,
  ArrowRightLeft,
  Search,
  ShieldCheck,
  BadgeCheck,
  Headphones,
} from "lucide-react";
import { useNavigate } from "react-router-dom";

import { searchFlights } from "../services/flightService";
import LocationAutocomplete from "../components/LocationAutocomplete";

export default function Home() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    origin: null,
    destination: null,
    departureDate: "",
    passengers: "",
    currency: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function updateLocation(field, location) {
    setForm((previous) => ({
      ...previous,
      [field]: location,
    }));

    setError("");
  }

  function updateField(event) {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    setError("");
  }

  function swapAirports() {
    setForm((previous) => ({
      ...previous,
      origin: previous.destination,
      destination: previous.origin,
    }));

    setError("");
  }

  async function handleSearch(event) {
    event.preventDefault();
    setError("");

    // User must select locations from backend suggestions.
    if (!form.origin?.code || !form.destination?.code) {
      setError(
        "Please select your origin and destination from the location suggestions."
      );
      return;
    }

    if (form.origin.code === form.destination.code) {
      setError("Origin and destination must be different.");
      return;
    }

    if (!form.departureDate) {
      setError("Please select your departure date.");
      return;
    }

    const selectedDate = new Date(
      `${form.departureDate}T00:00:00`
    );

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (
      Number.isNaN(selectedDate.getTime()) ||
      selectedDate < today
    ) {
      setError("Departure date cannot be in the past.");
      return;
    }

    const passengerCount = Number(form.passengers);

    if (
      !Number.isInteger(passengerCount) ||
      passengerCount < 1
    ) {
      setError("Please enter a valid passenger count.");
      return;
    }

    if (!form.currency.trim()) {
      setError("Please enter your preferred currency.");
      return;
    }

    const request = {
      origin: form.origin.code,
      destination: form.destination.code,
      departureDate: form.departureDate,
      passengers: passengerCount,
      currency: form.currency.trim().toUpperCase(),
    };

    try {
      setLoading(true);

      const results = await searchFlights(request);

      if (!Array.isArray(results?.offers)) {
        throw new Error(
          "The flight service returned an unexpected response."
        );
      }

      navigate("/flights", {
        state: {
          results,
          searchRequest: request,
        },
      });
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        "Flight search failed. Please try again.";

      setError(message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main>
      <section className="hero">
        <div className="hero-overlay" />

        <div className="container-app hero-content">
          <span className="hero-tag">
            <Plane size={15} />
            Your journey starts here
          </span>

          <h1>
            Explore the world
            <br />
            with <span>SkyGo</span>
          </h1>

          <p>
            Search flights, compare available fares and manage
            your journey in one place.
          </p>
        </div>
      </section>

      <section className="search-panel">
        <div className="trip-tabs">
          <button
            type="button"
            className="trip-tab active"
            aria-current="page"
          >
            One Way
          </button>

          <button
            type="button"
            className="trip-tab"
            disabled
            title="Round trip search is not implemented yet"
          >
            Round Trip
          </button>

          <button
            type="button"
            className="trip-tab"
            disabled
            title="Multi-city search is not implemented yet"
          >
            Multi City
          </button>
        </div>

        <form onSubmit={handleSearch}>
          <div className="search-grid">
            <LocationAutocomplete
              label="From"
              value={form.origin}
              onSelect={(location) =>
                updateLocation("origin", location)
              }
              placeholder="Search city or airport"
            />

            <button
              type="button"
              className="swap-button"
              onClick={swapAirports}
              aria-label="Swap origin and destination"
              disabled={loading}
            >
              <ArrowRightLeft size={18} />
            </button>

            <LocationAutocomplete
              label="To"
              value={form.destination}
              onSelect={(location) =>
                updateLocation("destination", location)
              }
              placeholder="Search city or airport"
            />

            <Field
              icon={<CalendarDays size={18} />}
              label="Departure"
              name="departureDate"
              type="date"
              value={form.departureDate}
              onChange={updateField}
              min={new Date().toLocaleDateString("en-CA")}
            />

            <Field
              icon={<Users size={18} />}
              label="Passengers"
              name="passengers"
              type="number"
              value={form.passengers}
              onChange={updateField}
              placeholder="Number"
              min="1"
              step="1"
            />

            <Field
              label="Currency"
              name="currency"
              value={form.currency}
              onChange={updateField}
              placeholder="e.g. INR"
              autoComplete="off"
            />
          </div>

          {error && (
            <p role="alert" className="error-message">
              {error}
            </p>
          )}

          <button
            type="submit"
            className="primary-button search-button"
            disabled={loading}
          >
            <Search size={18} />

            {loading ? "Searching flights..." : "Search Flights"}
          </button>
        </form>
      </section>

      <section className="container-app benefits">
        <Benefit
          icon={<ShieldCheck />}
          title="Secure payments"
          description="Payment handled by the connected payment service."
        />

        <Benefit
          icon={<BadgeCheck />}
          title="Clear booking flow"
          description="Review your details before creating a booking."
        />

        <Benefit
          icon={<Headphones />}
          title="Travel support"
          description="Find help for your booking and journey."
        />
      </section>
    </main>
  );
}

function Field({
  icon,
  label,
  name,
  value,
  onChange,
  type = "text",
  placeholder,
  min,
  step,
  autoComplete,
}) {
  return (
    <label className="field">
      <span className="field-label">
        {icon}
        {label}
      </span>

      <input
        required
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder || ""}
        min={min}
        step={step}
        autoComplete={autoComplete}
      />
    </label>
  );
}

function Benefit({ icon, title, description }) {
  return (
    <article className="benefit-card">
      <div className="benefit-icon">{icon}</div>

      <div>
        <h2>{title}</h2>
        <p>{description}</p>
      </div>
    </article>
  );
}
