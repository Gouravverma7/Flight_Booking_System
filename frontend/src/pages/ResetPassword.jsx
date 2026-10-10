
import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { LockKeyhole, LoaderCircle, Plane } from "lucide-react";
import { resetPassword } from "../services/authService";

export default function ResetPassword() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const token = params.get("token") || "";

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (!token) {
      setError("This reset link is missing its token. Request a new link.");
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
      await resetPassword(token, password);
      navigate("/login", {
        replace: true,
        state: { passwordResetSuccess: true },
      });
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "This reset link may be invalid or expired. Request a new link."
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
          <h1 className="text-2xl font-bold text-slate-900">Set new password</h1>
          <p className="mt-2 text-sm text-slate-600">
            Choose a new password for your SkyGo account.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <label className="block text-sm font-medium text-slate-700">
            New password
            <input
              type="password"
              autoComplete="new-password"
              minLength={8}
              maxLength={100}
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
          </label>

          <label className="block text-sm font-medium text-slate-700">
            Confirm new password
            <input
              type="password"
              autoComplete="new-password"
              required
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
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
            {loading ? <LoaderCircle size={18} className="animate-spin" /> : <LockKeyhole size={18} />}
            {loading ? "Updating password..." : "Reset password"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          <Link to="/forgot-password" className="font-semibold text-blue-700 hover:underline">
            Request a new reset link
          </Link>
        </p>
      </section>
    </main>
  );
}
