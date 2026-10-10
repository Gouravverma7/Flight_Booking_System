import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Plane,
  CalendarDays,
  Ticket,
  RefreshCw,
  Download,
  LoaderCircle,
  AlertCircle,
  CheckCircle2,
  Clock3,
  XCircle,
  Search,
  ArrowRight,
} from "lucide-react";
import api from "../services/api";

function rowsOf(data) {
  if (Array.isArray(data)) return data;
  return (
    data?.bookings ||
    data?.content ||
    data?.data?.bookings ||
    data?.data?.content ||
    data?.data ||
    []
  );
}
function value(...items) {
  return items.find((v) => v !== undefined && v !== null && v !== "");
}
function dateText(v) {
  if (!v) return "Date not available";
  const d = new Date(v);
  return Number.isNaN(d.getTime())
    ? String(v)
    : new Intl.DateTimeFormat("en-IN", {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(d);
}
function money(v, currency = "INR") {
  if (v === undefined || v === null || v === "") return "Amount unavailable";
  try {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency,
      maximumFractionDigits: 2,
    }).format(Number(v));
  } catch {
    return `${v} ${currency}`;
  }
}
function statusMeta(status) {
  const s = String(status || "").toUpperCase();
  if (["CONFIRMED", "COMPLETED", "TICKETED", "SUCCESS"].includes(s))
    return { label: s, cls: "bg-green-50 text-green-700", Icon: CheckCircle2 };
  if (
    ["PENDING", "PENDING_PAYMENT", "PAYMENT_PENDING", "PROCESSING"].includes(s)
  )
    return {
      label: s.replaceAll("_", " "),
      cls: "bg-amber-50 text-amber-700",
      Icon: Clock3,
    };
  if (["CANCELLED", "FAILED", "EXPIRED"].includes(s))
    return { label: s, cls: "bg-red-50 text-red-700", Icon: XCircle };
  return {
    label: s || "STATUS UNAVAILABLE",
    cls: "bg-slate-100 text-slate-700",
    Icon: Clock3,
  };
}

export default function MyBookings() {
  const navigate = useNavigate();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [downloading, setDownloading] = useState("");

  const loadBookings = useCallback(async () => {
    setLoading(true);
    setError("");
    setNotice("");
    try {
      const response = await api.get("/api/bookings/my");
      const list = rowsOf(response.data);

      if (!Array.isArray(list)) {
        setBookings([]);
        return;
      }

      // /api/bookings/my returns summary fields. Enrich cards from the
      // booking-details endpoint when it provides additional flight fields.
      const enriched = await Promise.all(
        list.map(async (booking) => {
          const reference = value(
            booking.bookingReference,
            booking.bookingRef,
            booking.reference,
          );

          if (!reference) return booking;

          try {
            const detailResponse = await api.get(
              `/api/bookings/${encodeURIComponent(reference)}`,
            );
            const details =
              detailResponse?.data?.data ?? detailResponse?.data;

            return details && typeof details === "object"
              ? { ...booking, ...details }
              : booking;
          } catch {
            // Preserve the summary if this booking's details cannot be loaded.
            return booking;
          }
        }),
      );

      setBookings(enriched);
    } catch (e) {
      setError(
        e.response?.data?.message ||
          e.response?.data?.error ||
          (e.response?.status === 401
            ? "Your session expired. Please sign in again."
            : "Couldn't load your bookings. Please try again."),
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!sessionStorage.getItem("accessToken")) {
      navigate("/login", { replace: true, state: { from: "/bookings" } });
      return;
    }
    loadBookings();
  }, [navigate, loadBookings]);

  const filtered = useMemo(
    () =>
      bookings.filter((b) => {
        const status = String(
          value(b.status, b.bookingStatus, ""),
        ).toUpperCase();
        const ref = String(
          value(b.bookingReference, b.bookingRef, b.reference, b.bookingId, ""),
        );
        const route = `${value(b.origin, b.originIata, b.source, "")} ${value(b.destination, b.destinationIata, b.destinationAirport, "")}`;
        return (
          (filter === "ALL" || status === filter) &&
          `${ref} ${route} ${status}`
            .toLowerCase()
            .includes(search.trim().toLowerCase())
        );
      }),
    [bookings, filter, search],
  );

  async function downloadTicket(reference) {
    if (!reference) return;
    setDownloading(String(reference));
    setError("");
    setNotice("");
    try {
      const response = await api.get(
        `/api/tickets/${encodeURIComponent(reference)}/download`,
        { responseType: "blob" },
      );
      const url = URL.createObjectURL(
        new Blob([response.data], { type: "application/pdf" }),
      );
      const link = document.createElement("a");
      link.href = url;
      link.download = `ticket-${reference}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
      setNotice(`Ticket download started for ${reference}.`);
    } catch (e) {
      setError(
        e.response?.status === 404
          ? "Ticket PDF is not available yet for this booking."
          : "Unable to download the ticket. Please try again.",
      );
    } finally {
      setDownloading("");
    }
  }

  const confirmed = bookings.filter(
    (b) =>
      String(value(b.status, b.bookingStatus, "")).toUpperCase() ===
      "CONFIRMED",
  ).length;
  const pending = bookings.filter((b) =>
    ["PENDING", "PENDING_PAYMENT", "PAYMENT_PENDING", "PROCESSING"].includes(
      String(value(b.status, b.bookingStatus, "")).toUpperCase(),
    ),
  ).length;

  return (
    <main className="min-h-screen bg-slate-50 px-4 py-8 sm:px-6 lg:py-12">
      <div className="mx-auto max-w-6xl">
        <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-wider text-blue-600">
              Your trips
            </p>
            <h1 className="mt-2 text-3xl font-extrabold text-slate-900 sm:text-4xl">
              My Bookings
            </h1>
            <p className="mt-2 text-slate-600">
              View your flight reservations and access your tickets.
            </p>
          </div>
          <Link
            to="/"
            className="inline-flex w-fit items-center gap-2 rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white hover:bg-blue-700"
          >
            Search flights <ArrowRight size={17} />
          </Link>
        </div>

        {error && (
          <div
            role="alert"
            className="mb-5 flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
          >
            <AlertCircle size={19} />
            <span className="flex-1">{error}</span>
            {error.toLowerCase().includes("sign in") && (
              <button
                onClick={() =>
                  navigate("/login", { state: { from: "/bookings" } })
                }
                className="font-semibold underline"
              >
                Sign in
              </button>
            )}
          </div>
        )}
        {notice && (
          <div
            role="status"
            className="mb-5 flex items-center gap-3 rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800"
          >
            <CheckCircle2 size={19} />
            {notice}
          </div>
        )}

        <div className="mb-7 grid gap-4 sm:grid-cols-3">
          <Stat
            label="Total bookings"
            count={loading ? "—" : bookings.length}
          />
          <Stat label="Confirmed" count={loading ? "—" : confirmed} green />
          <Stat label="Pending" count={loading ? "—" : pending} amber />
        </div>

        <section className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm sm:p-5">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="relative w-full sm:max-w-sm">
              <Search
                size={18}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
              />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search by reference or route"
                className="w-full rounded-xl border border-slate-300 py-3 pl-10 pr-3 text-sm outline-none focus:border-blue-500"
              />
            </div>
            <div className="flex gap-3">
              <select
                value={filter}
                onChange={(e) => setFilter(e.target.value)}
                aria-label="Filter by booking status"
                className="min-w-0 rounded-xl border border-slate-300 bg-white px-3 py-3 text-sm"
              >
                <option value="ALL">All bookings</option>
                <option value="CONFIRMED">Confirmed</option>
                <option value="PENDING_PAYMENT">Pending payment</option>
                <option value="CANCELLED">Cancelled</option>
                <option value="FAILED">Failed</option>
              </select>
              <button
                onClick={loadBookings}
                disabled={loading}
                className="inline-flex items-center gap-2 rounded-xl border border-slate-300 px-4 py-3 text-sm font-semibold hover:bg-slate-50 disabled:opacity-60"
              >
                {loading ? (
                  <LoaderCircle className="animate-spin" size={17} />
                ) : (
                  <RefreshCw size={17} />
                )}
                Refresh
              </button>
            </div>
          </div>
        </section>

        <section className="mt-5 space-y-4">
          {loading ? (
            <div className="flex min-h-56 items-center justify-center rounded-2xl border bg-white">
              <LoaderCircle size={28} className="animate-spin text-blue-600" />
              <span className="ml-3 text-sm text-slate-600">
                Loading your bookings...
              </span>
            </div>
          ) : !error && bookings.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center">
              <Plane size={32} className="mx-auto text-blue-600" />
              <h2 className="mt-5 text-xl font-bold text-slate-900">
                No bookings yet
              </h2>
              <p className="mt-2 text-sm text-slate-600">
                Your reservations and tickets will appear here after you book a
                flight.
              </p>
              <Link
                to="/"
                className="mt-6 inline-flex items-center gap-2 rounded-xl bg-blue-600 px-5 py-3 text-sm font-semibold text-white"
              >
                Find a flight <ArrowRight size={17} />
              </Link>
            </div>
          ) : !error && filtered.length === 0 ? (
            <div className="rounded-2xl border bg-white p-10 text-center">
              <p className="font-semibold text-slate-800">
                No matching bookings
              </p>
              <button
                onClick={() => {
                  setSearch("");
                  setFilter("ALL");
                }}
                className="mt-3 text-sm font-semibold text-blue-700 underline"
              >
                Clear filters
              </button>
            </div>
          ) : (
            filtered.map((b, i) => (
              <BookingCard
                key={value(b.id, b.bookingReference, b.bookingRef, i)}
                booking={b}
                downloading={downloading}
                onDownload={downloadTicket}
              />
            ))
          )}
        </section>
      </div>
    </main>
  );
}

function Stat({ label, count, green, amber }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <p className="text-sm text-slate-500">{label}</p>
      <p
        className={`mt-2 text-3xl font-extrabold ${green ? "text-green-600" : amber ? "text-amber-600" : "text-slate-900"}`}
      >
        {count}
      </p>
    </div>
  );
}

function BookingCard({ booking: b, downloading, onDownload }) {
  const reference = value(
    b.bookingReference,
    b.bookingRef,
    b.reference,
    b.bookingId,
  );
  const origin = value(
    b.origin,
    b.originIata,
    b.departureAirport,
    b.source,
    "—",
  );
  const destination = value(
    b.destination,
    b.destinationIata,
    b.arrivalAirport,
    b.destinationAirport,
    "—",
  );
  const departure = value(
    b.departureDateTime,
    b.departureDate,
    b.departureTime,
    b.departure,
  );
  const status = String(value(b.status, b.bookingStatus, "")).toUpperCase();
  const amount = value(b.amount, b.totalAmount, b.price);
  const currency = value(b.currency, b.paymentCurrency, "INR");
  const airlineRef = value(b.airlineBookingReference, b.airlineReference);
  const meta = statusMeta(status);
  const StatusIcon = meta.Icon;
  const canDownload =
    status === "CONFIRMED" ||
    Boolean(b.ticketGenerated) ||
    String(b.ticketStatus || "").toUpperCase() === "GENERATED";
  const busy = String(downloading) === String(reference);

  return (
    <article className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:shadow-md">
      <div className="flex flex-col gap-4 border-b border-slate-100 p-5 sm:flex-row sm:items-start sm:justify-between sm:p-6">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
            Booking reference
          </p>
          <p className="mt-1 font-mono text-sm font-bold text-slate-800">
            {reference || "Not available"}
          </p>
          <div className="mt-4 flex items-center gap-3">
            <div className="grid h-11 w-11 place-items-center rounded-xl bg-blue-50 text-blue-700">
              <Plane size={21} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-900">
                {origin} <span className="mx-1 text-blue-600">→</span>{" "}
                {destination}
              </h2>
              <p className="mt-1 text-sm text-slate-500">
                {departure
                  ? dateText(departure)
                  : "Departure date not available"}
              </p>
            </div>
          </div>
        </div>
        <span
          className={`inline-flex w-fit items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-bold ${meta.cls}`}
        >
          <StatusIcon size={14} />
          {meta.label}
        </span>
      </div>
      <div className="grid gap-4 p-5 sm:grid-cols-2 sm:p-6 lg:grid-cols-3">
        <Info
          icon={<CalendarDays size={19} />}
          label="Departure"
          value={departure ? dateText(departure) : "Not available"}
        />
        <Info
          icon={<Ticket size={19} />}
          label="Airline reference"
          value={airlineRef || "Available after confirmation"}
        />
        <Info label="Total amount" value={money(amount, currency)} />
      </div>
      <div className="flex flex-col gap-3 border-t border-slate-100 bg-slate-50/70 px-5 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <p className="text-xs text-slate-500">
          {status === "CONFIRMED"
            ? "Your booking is confirmed."
            : "Booking details reflect the latest available status."}
        </p>
        <button
          onClick={() => onDownload(reference)}
          disabled={!reference || !canDownload || busy}
          title={
            !canDownload
              ? "Ticket is available after confirmation and generation."
              : "Download ticket PDF"
          }
          className="inline-flex items-center justify-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {busy ? (
            <LoaderCircle size={17} className="animate-spin" />
          ) : (
            <Download size={17} />
          )}
          {busy ? "Preparing ticket..." : "Download ticket"}
        </button>
        <Link
          to={`/bookings/${encodeURIComponent(reference)}`}
          className="inline-flex items-center justify-center gap-2 rounded-xl border border-blue-600 bg-white px-4 py-2.5 text-sm font-semibold text-blue-700 hover:bg-blue-50"
        >
          View Details
          <ArrowRight size={17} />
        </Link>
      </div>
    </article>
  );
}
function Info({ icon, label, value }) {
  return (
    <div className="flex items-start gap-3">
      <span className="mt-0.5 text-slate-400">
        {icon || <Ticket size={19} />}
      </span>
      <div>
        <p className="text-xs text-slate-500">{label}</p>
        <p className="mt-1 break-words text-sm font-semibold text-slate-800">
          {value}
        </p>
      </div>
    </div>
  );
}
