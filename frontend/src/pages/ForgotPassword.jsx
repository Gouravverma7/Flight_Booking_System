
import { useState } from "react";
import { Link } from "react-router-dom";
import { Mail, Plane, LoaderCircle, CheckCircle2 } from "lucide-react";
import { requestPasswordReset } from "../services/authService";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSent(false);

    try {
      await requestPasswordReset(email);
      setSent(true);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "We couldn't process your request. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="flex min-h-[80vh] items-center justify-center bg-slate-50 px-4 py-10">
      <section className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
        <div className="mb-6 text-center">
          <div className="mx-auto mb-4 grid h-14 w-14 place-items-center rounded-2xl bg-blue-100 text-blue-700">
            <Plane size={27} />
          </div>
          <h1 className="text-2xl font-bold text-slate-900">
            Forgot password?
          </h1>
          <p className="mt-2 text-sm text-slate-600">
            Enter your registered email and we'll send a reset link if an account exists.
          </p>
        </div>

        {sent ? (
          <div role="status" className="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800">
            <CheckCircle2 className="mb-2" size={22} />
            If an account exists for this email, a password reset link will be sent. Check your inbox.
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-5">
            <label className="block text-sm font-medium text-slate-700">
              Email address
              <div className="relative mt-2">
                <Mail size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                <input
                  type="email"
                  autoComplete="email"
                  required
                  maxLength={254}
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  placeholder="you@example.com"
                  className="w-full rounded-xl border border-slate-300 py-3 pl-10 pr-3 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                />
              </div>
            </label>

            {error && (
              <p role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
                {error}
              </p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:opacity-60"
            >
              {loading && <LoaderCircle size={18} className="animate-spin" />}
              {loading ? "Sending..." : "Send reset link"}
            </button>
          </form>
        )}

        <p className="mt-6 text-center text-sm text-slate-600">
          Remember your password?{" "}
          <Link to="/login" className="font-semibold text-blue-700 hover:underline">
            Back to login
          </Link>
        </p>
      </section>
    </main>
  );
}
