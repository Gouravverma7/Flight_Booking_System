
import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { UserRoundPlus, Eye, EyeOff, LoaderCircle, Plane } from "lucide-react";
import { register } from "../services/authService";

export default function Register() {
  const navigate = useNavigate();
  const location = useLocation();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const redirectState = location.state;

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (name.trim().length < 2 || name.trim().length > 100) {
      setError("Name must contain between 2 and 100 characters.");
      return;
    }

    if (password.length < 8 || password.length > 100) {
      setError("Password must contain between 8 and 100 characters.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);

    try {
      await register({ name, email, password });

      navigate("/login", {
        replace: true,
        state: {
          ...(redirectState || {}),
          registrationSuccess: true,
        },
      });
    } catch (err) {
      console.error("Registration failed:", {
        status: err.response?.status,
        data: err.response?.data,
        message: err.message,
      });

      const data = err.response?.data;

      setError(
        data?.message ||
          data?.error ||
          (err.response?.status === 409
            ? "An account with this email may already exist. Please sign in."
            : err.response
              ? "Registration failed. Please check your details and try again."
              : "Unable to connect to the server. Please try again.")
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="flex min-h-[80vh] items-center justify-center bg-slate-50 px-4 py-10">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
        <div className="mb-7 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-100 text-blue-700">
            <Plane size={27} />
          </div>

          <h1 className="text-2xl font-bold text-slate-900">
            Create your SkyGo account
          </h1>

          <p className="mt-2 text-sm text-slate-600">
            Register to continue with your flight booking.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <label className="block text-sm font-medium text-slate-700">
            Full name
            <input
              type="text"
              name="name"
              autoComplete="name"
              minLength={2}
              maxLength={100}
              required
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Enter your full name"
              className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
          </label>

          <label className="block text-sm font-medium text-slate-700">
            Email address
            <input
              type="email"
              name="email"
              autoComplete="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
          </label>

          <label className="block text-sm font-medium text-slate-700">
            Password
            <div className="relative mt-2">
              <input
                type={showPassword ? "text" : "password"}
                name="password"
                autoComplete="new-password"
                minLength={8}
                maxLength={100}
                required
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Create a password"
                className="w-full rounded-xl border border-slate-300 px-4 py-3 pr-12 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              />

              <button
                type="button"
                onClick={() => setShowPassword((current) => !current)}
                aria-label={showPassword ? "Hide password" : "Show password"}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500"
              >
                {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
              </button>
            </div>
            <span className="mt-1 block text-xs font-normal text-slate-500">
              Use between 8 and 100 characters.
            </span>
          </label>

          <label className="block text-sm font-medium text-slate-700">
            Confirm password
            <input
              type="password"
              name="confirmPassword"
              autoComplete="new-password"
              required
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              placeholder="Re-enter your password"
              className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
          </label>

          {error && (
            <div
              role="alert"
              className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700"
            >
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 px-4 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loading ? (
              <LoaderCircle size={18} className="animate-spin" />
            ) : (
              <UserRoundPlus size={18} />
            )}
            {loading ? "Creating account..." : "Create account"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          Already have an account?{" "}
          <Link
            to="/login"
            state={redirectState}
            className="font-semibold text-blue-700 hover:underline"
          >
            Sign in
          </Link>
        </p>
      </div>
    </main>
  );
}
