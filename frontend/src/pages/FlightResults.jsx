import { useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  Clock3,
  Plane,
  SlidersHorizontal,
} from "lucide-react";

function formatTime(value) {
  if (!value) return "--:--";

  return new Date(value).toLocaleTimeString("en-IN", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
}

function formatDate(value) {
  if (!value) return "";

  return new Date(value).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function getDuration(start, end) {
  if (!start || !end) return null;

  const minutes = Math.round(
    (new Date(end).getTime() - new Date(start).getTime()) / 60000,
  );

  if (minutes < 0) return null;

  const days = Math.floor(minutes / 1440);
  const hours = Math.floor((minutes % 1440) / 60);
  const remainingMinutes = minutes % 60;

  return [
    days ? `${days}d` : "",
    hours ? `${hours}h` : "",
    remainingMinutes ? `${remainingMinutes}m` : "",
  ]
    .filter(Boolean)
    .join(" ");
}

function getStops(offer) {
  if (Array.isArray(offer.segments)) {
    return Math.max(0, offer.segments.length - 1);
  }

  return 0;
}

function formatPrice(offer) {
  const amount = Number(offer.displayPrice ?? offer.price);

  if (!Number.isFinite(amount)) return "Price unavailable";

  const currency = offer.displayCurrency || offer.currency;

  try {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency,
      maximumFractionDigits: 2,
    }).format(amount);
  } catch {
    return `${currency || ""} ${amount.toFixed(2)}`;
  }
}

export default function FlightResults() {
  const location = useLocation();
  const navigate = useNavigate();

  const results = location.state?.results;
  const searchRequest = location.state?.searchRequest;

  const [sortBy, setSortBy] = useState("price");
  const [airlineFilter, setAirlineFilter] = useState("all");
  const [stopsFilter, setStopsFilter] = useState("all");
  const [selectedOffer, setSelectedOffer] = useState(null);

  const offers = Array.isArray(results?.offers) ? results.offers : [];

  const airlines = useMemo(
    () => [...new Set(offers.map((offer) => offer.airline).filter(Boolean))],
    [offers],
  );

  const filteredOffers = useMemo(() => {
    const filtered = offers.filter((offer) => {
      const airlineMatches =
        airlineFilter === "all" || offer.airline === airlineFilter;

      const stops = getStops(offer);

      const stopsMatch =
        stopsFilter === "all" ||
        (stopsFilter === "direct" && stops === 0) ||
        (stopsFilter === "one" && stops === 1) ||
        (stopsFilter === "twoPlus" && stops >= 2);

      return airlineMatches && stopsMatch;
    });

    return [...filtered].sort((a, b) => {
      if (sortBy === "departure") {
        return new Date(a.departureTime) - new Date(b.departureTime);
      }

      if (sortBy === "arrival") {
        return new Date(a.arrivalTime) - new Date(b.arrivalTime);
      }

      if (sortBy === "duration") {
        return (
          new Date(a.arrivalTime) -
          new Date(a.departureTime) -
          (new Date(b.arrivalTime) - new Date(b.departureTime))
        );
      }

      return (
        Number(a.displayPrice ?? a.price) - Number(b.displayPrice ?? b.price)
      );
    });
  }, [offers, airlineFilter, stopsFilter, sortBy]);

  if (!results || !searchRequest) {
    return (
      <main className="mx-auto max-w-6xl px-4 py-16">
        <h1 className="text-2xl font-bold">Flight results</h1>
        <p className="mt-2 text-slate-600">
          Start a flight search to view available offers.
        </p>
        <button
          className="mt-5 rounded-xl bg-blue-600 px-5 py-3 font-semibold text-white"
          onClick={() => navigate("/")}
        >
          <ArrowLeft className="mr-2 inline" size={17} />
          Search flights
        </button>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-slate-50">
      <div className="mx-auto max-w-6xl px-4 py-8">
        {/* <button
          onClick={() => navigate("/")}
          className="mb-5 flex items-center gap-2 text-sm font-semibold text-blue-700"
        >
          <ArrowLeft size={17} />
          Edit search
        </button> */}
        {/* <button
          onClick={() =>
            navigate("/booking", {
              state: {
                offer,
                searchRequest,
              },
            })
          }
          className="rounded-xl bg-blue-600 px-5 py-3 text-sm font-bold text-white transition hover:bg-blue-700"
        >
          Enter passenger details
          <ArrowRight className="ml-2 inline" size={16} />
        </button> */}
        <section className="mb-7 rounded-2xl bg-slate-900 p-6 text-white">
          <p className="text-sm text-slate-300">Your journey</p>
          <h1 className="mt-2 text-2xl font-bold sm:text-3xl">
            {searchRequest.origin} → {searchRequest.destination}
          </h1>
          <p className="mt-2 text-sm text-slate-300">
            {formatDate(searchRequest.departureDate)}
            {" · "}
            {searchRequest.passengers} passenger(s)
          </p>
        </section>
        <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-xl font-bold text-slate-900">
              Available flights
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              {filteredOffers.length} offers found
            </p>
          </div>

          <label className="flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm">
            <SlidersHorizontal size={17} />
            Sort by
            <select
              value={sortBy}
              onChange={(event) => setSortBy(event.target.value)}
              className="max-w-40 bg-transparent font-semibold outline-none"
            >
              <option value="price">Lowest price</option>
              <option value="duration">Shortest journey</option>
              <option value="departure">Earliest departure</option>
              <option value="arrival">Earliest arrival</option>
            </select>
          </label>
        </div>
        <section className="mb-5 grid gap-3 rounded-2xl border border-slate-200 bg-white p-4 sm:grid-cols-2">
          <label className="text-sm font-semibold text-slate-700">
            Airline
            <select
              value={airlineFilter}
              onChange={(event) => setAirlineFilter(event.target.value)}
              className="mt-2 w-full rounded-xl border border-slate-200 p-3 font-normal"
            >
              <option value="all">All airlines</option>
              {airlines.map((airline) => (
                <option key={airline} value={airline}>
                  {airline}
                </option>
              ))}
            </select>
          </label>

          <label className="text-sm font-semibold text-slate-700">
            Stops
            <select
              value={stopsFilter}
              onChange={(event) => setStopsFilter(event.target.value)}
              className="mt-2 w-full rounded-xl border border-slate-200 p-3 font-normal"
            >
              <option value="all">Any number of stops</option>
              <option value="direct">Non-stop</option>
              <option value="one">1 stop</option>
              <option value="twoPlus">2+ stops</option>
            </select>
          </label>
        </section>
        <section className="space-y-4">
          {filteredOffers.map((offer) => {
            const stops = getStops(offer);
            const duration = getDuration(
              offer.departureTime,
              offer.arrivalTime,
            );

            return (
              <article
                key={offer.offerId}
                className={`rounded-2xl border bg-white p-5 shadow-sm transition ${
                  selectedOffer?.offerId === offer.offerId
                    ? "border-blue-500 ring-2 ring-blue-100"
                    : "border-slate-200 hover:border-blue-200"
                }`}
              >
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div className="flex min-w-0 items-center gap-3">
                    <div className="grid h-12 w-12 shrink-0 place-items-center rounded-xl bg-blue-50 text-blue-700">
                      <Plane size={22} />
                    </div>

                    <div className="min-w-0">
                      <h3 className="font-bold text-slate-900">
                        {offer.airline || "Airline unavailable"}
                      </h3>
                      <p className="mt-1 text-sm text-slate-500">
                        {offer.airlineCode || ""}
                        {offer.flightNumber && offer.flightNumber !== "null"
                          ? ` ${offer.flightNumber}`
                          : ""}
                      </p>
                    </div>
                  </div>

                  <div className="sm:text-right">
                    <p className="text-xl font-extrabold text-slate-900">
                      {formatPrice(offer)}
                    </p>
                    <p className="mt-1 text-xs text-slate-500">
                      {offer.displayCurrency || offer.currency}
                      {" fare"}
                    </p>
                  </div>
                </div>

                <div className="my-5 grid grid-cols-[1fr_auto_1fr] items-center gap-3">
                  <div>
                    <p className="text-2xl font-bold text-slate-900">
                      {formatTime(offer.departureTime)}
                    </p>
                    <p className="mt-1 text-sm font-semibold text-slate-600">
                      {offer.origin}
                    </p>
                    <p className="text-xs text-slate-400">
                      {formatDate(offer.departureTime)}
                    </p>
                  </div>

                  <div className="min-w-20 text-center">
                    <p className="text-xs text-slate-500">
                      {duration || "Duration unavailable"}
                    </p>
                    <div className="my-2 flex items-center gap-1 text-blue-500">
                      <div className="h-px flex-1 bg-slate-300" />
                      <Plane size={17} />
                      <div className="h-px flex-1 bg-slate-300" />
                    </div>
                    <p className="text-xs text-slate-500">
                      {stops === 0
                        ? "Non-stop"
                        : `${stops} stop${stops > 1 ? "s" : ""}`}
                    </p>
                  </div>

                  <div className="text-right">
                    <p className="text-2xl font-bold text-slate-900">
                      {formatTime(offer.arrivalTime)}
                    </p>
                    <p className="mt-1 text-sm font-semibold text-slate-600">
                      {offer.destination}
                    </p>
                    <p className="text-xs text-slate-400">
                      {formatDate(offer.arrivalTime)}
                    </p>
                  </div>
                </div>

                {offer.segments?.length > 1 && (
                  <details className="mb-4 rounded-xl bg-slate-50 p-3">
                    <summary className="cursor-pointer text-sm font-semibold text-blue-700">
                      View connecting flights
                    </summary>
                    <div className="mt-3 space-y-3">
                      {offer.segments.map((segment, index) => (
                        <div
                          key={`${segment.flightNumber}-${index}`}
                          className="border-l-2 border-blue-200 pl-3 text-sm"
                        >
                          <p className="font-semibold text-slate-800">
                            {segment.airline}
                          </p>
                          <p className="mt-1 text-slate-600">
                            {segment.origin} → {segment.destination}
                          </p>
                          <p className="text-slate-500">
                            {formatTime(segment.departureTime)}
                            {" – "}
                            {formatTime(segment.arrivalTime)}
                          </p>
                        </div>
                      ))}
                    </div>
                  </details>
                )}

                <div className="flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-4">
                  <span className="text-sm text-slate-500">
                    {offer.refundable === true
                      ? "Refundable"
                      : offer.refundable === false
                        ? "Non-refundable"
                        : "Refund policy unavailable"}
                  </span>

                  {/* <button
                    onClick={() => setSelectedOffer(offer)}
                    className="rounded-xl bg-blue-600 px-5 py-3 text-sm font-bold text-white transition hover:bg-blue-700"
                  >
                    {selectedOffer?.offerId === offer.offerId
                      ? "Flight selected"
                      : "Select flight"}
                    <ArrowRight className="ml-2 inline" size={16} />
                  </button> */}

                  <button
                    onClick={() => {
                      const token = sessionStorage.getItem("accessToken");

                      if (!token) {
                        navigate("/login", {
                          state: {
                            from: "/booking",
                            offer,
                            searchRequest,
                          },
                        });
                        return;
                      }

                      navigate("/booking", {
                        state: {
                          offer,
                          searchRequest,
                        },
                      });
                    }}
                    className="rounded-xl bg-blue-600 px-5 py-3 text-sm font-bold text-white transition hover:bg-blue-700"
                  >
                    Enter passenger details
                    <ArrowRight className="ml-2 inline" size={16} />
                  </button>
                </div>
              </article>
            );
          })}

          {filteredOffers.length === 0 && (
            <div className="rounded-2xl border border-slate-200 bg-white p-10 text-center">
              <Clock3 className="mx-auto text-slate-400" size={30} />
              <h3 className="mt-3 font-bold text-slate-900">
                No matching flights
              </h3>
              <p className="mt-1 text-sm text-slate-500">
                Try changing the airline or stops filters.
              </p>
            </div>
          )}
        </section>
        {selectedOffer && (
          <section className="mt-6 rounded-2xl border border-blue-200 bg-blue-50 p-5">
            <h3 className="font-bold text-slate-900">Selected flight</h3>
            <p className="mt-2 text-sm text-slate-700">
              {selectedOffer.airline}
              {" · "}
              {selectedOffer.origin} → {selectedOffer.destination}
              {" · "}
              {formatPrice(selectedOffer)}
            </p>
            <p className="mt-2 break-all text-xs text-slate-500">
              Offer reference: {selectedOffer.offerId}
            </p>
            <p className="mt-3 text-sm text-slate-600">
              Flight selected. Passenger details and booking will be connected
              in the next step.
            </p>
          </section>
        )}
      </div>
    </main>
  );
}
