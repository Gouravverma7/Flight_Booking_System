
import { useEffect, useRef, useState } from "react";
import { MapPin, LoaderCircle } from "lucide-react";
import api from "../services/api";

export default function LocationAutocomplete({
  label,
  value,
  onSelect,
  placeholder = "Search city or airport",
}) {
  const [query, setQuery] = useState(value?.label || "");
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [open, setOpen] = useState(false);
  const timer = useRef(null);
  const requestId = useRef(0);

  useEffect(() => {
    setQuery(value?.label || "");
  }, [value?.label]);

  useEffect(() => {
    const text = query.trim();

    if (text.length < 2 || value?.label === text) {
      setSuggestions([]);
      setError("");
      setLoading(false);
      return;
    }

    const id = ++requestId.current;

    timer.current = setTimeout(async () => {
      try {
        setLoading(true);
        setError("");

        // Backend endpoint to be added in Flight Service.
        const response = await api.get("/api/flights/locations", {
          params: { query: text },
        });

        if (id !== requestId.current) return;

        // Expected contract: { locations: [{ id, name, city,
        // country, iataCode, type }] }
        const locations = response.data?.locations;

        if (!Array.isArray(locations)) {
          throw new Error("Invalid location response from server.");
        }

        setSuggestions(locations);
        setOpen(true);
      } catch (err) {
        if (id !== requestId.current) return;

        setSuggestions([]);
        setError(
          err.response?.data?.message ||
          "Could not load locations. Please try again."
        );
      } finally {
        if (id === requestId.current) setLoading(false);
      }
    }, 300);

    return () => {
      clearTimeout(timer.current);
    };
  }, [query, value?.label]);

  function handleChange(event) {
    const text = event.target.value;

    setQuery(text);
    setOpen(true);
    setSuggestions([]);

    // Clear the previously selected airport when the user edits.
    onSelect(null);
  }

  function chooseLocation(location) {
    if (!location.iataCode) return;

    const labelText = [
      location.name,
      location.city,
      location.country,
      location.iataCode,
    ]
      .filter(Boolean)
      .filter((part, index, parts) => parts.indexOf(part) === index)
      .join(", ");

    setQuery(labelText);
    setSuggestions([]);
    setOpen(false);
    setError("");

    onSelect({
      ...location,
      label: labelText,
      code: location.iataCode,
    });
  }

  return (
    <div className="relative min-w-0">
      <label className="field">
        <span className="field-label">
          <MapPin size={17} />
          {label}
          {loading && (
            <LoaderCircle className="animate-spin" size={15} />
          )}
        </span>

        <input
          value={query}
          onChange={handleChange}
          onFocus={() => setOpen(true)}
          onBlur={() => {
            setTimeout(() => setOpen(false), 150);
          }}
          placeholder={placeholder}
          autoComplete="off"
          role="combobox"
          aria-expanded={open && suggestions.length > 0}
          aria-autocomplete="list"
        />
      </label>

      {open && suggestions.length > 0 && (
        <div className="location-dropdown">
          {suggestions.map((location) => (
            <button
              type="button"
              key={location.id}
              className="location-option"
              onMouseDown={(event) => event.preventDefault()}
              onClick={() => chooseLocation(location)}
            >
              <MapPin size={18} />

              <span>
                <strong>
                  {location.name || location.city}
                </strong>
                <small>
                  {[location.city, location.country]
                    .filter(Boolean)
                    .join(", ")}
                  {location.iataCode
                    ? ` · ${location.iataCode}`
                    : ""}
                </small>
              </span>
            </button>
          ))}
        </div>
      )}

      {open && error && (
        <p role="alert" className="error-message">
          {error}
        </p>
      )}
    </div>
  );
}
