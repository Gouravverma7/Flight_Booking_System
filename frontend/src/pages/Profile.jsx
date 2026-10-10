import { useEffect, useState } from "react";
import {
  UserRound,
  Mail,
  ShieldCheck,
  CalendarDays,
  Pencil,
  X,
  Save,
  LockKeyhole,
  LoaderCircle,
  CheckCircle2,
  AlertCircle,
} from "lucide-react";
import {
  getMyProfile,
  updateMyProfile,
  changeMyPassword,
} from "../services/authService";

const initialPasswordForm = {
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
};

function formatDate(value) {
  if (!value) return "Not available";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Not available";

  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(date);
}

function getErrorMessage(error, fallback) {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    fallback
  );
}

function Profile() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ name: "", email: "" });
  const [passwordForm, setPasswordForm] = useState(initialPasswordForm);
  const [editing, setEditing] = useState(false);
  const [activeTab, setActiveTab] = useState("profile");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadProfile() {
    setLoading(true);
    setError("");

    try {
      const data = await getMyProfile();
      setProfile(data);
      setForm({
        name: data?.name || "",
        email: data?.email || "",
      });
    } catch (err) {
      setError(
        getErrorMessage(
          err,
          "Unable to load your profile. Please sign in again if your session has expired."
        )
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadProfile();
  }, []);

  function handleProfileFieldChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  async function handleSaveProfile(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (form.name.trim().length < 2) {
      setError("Name must contain at least 2 characters.");
      return;
    }

    setSaving(true);

    try {
      const updated = await updateMyProfile(form);
      setProfile(updated);
      setForm({
        name: updated?.name || form.name.trim(),
        email: updated?.email || form.email.trim(),
      });
      setEditing(false);
      setSuccess("Profile updated successfully.");

      // The JWT may contain the old email. Require re-login after email changes.
      if (
        profile?.email &&
        updated?.email &&
        profile.email.toLowerCase() !== updated.email.toLowerCase()
      ) {
        setSuccess(
          "Profile updated. Your email changed, so sign out and sign in again to refresh your session."
        );
        sessionStorage.setItem("userEmail", updated.email);
        if (updated.name) sessionStorage.setItem("userName", updated.name);
      } else {
        if (updated?.email) sessionStorage.setItem("userEmail", updated.email);
        if (updated?.name) sessionStorage.setItem("userName", updated.name);
      }
    } catch (err) {
      setError(getErrorMessage(err, "Unable to save profile changes."));
    } finally {
      setSaving(false);
    }
  }

  function cancelEditing() {
    setForm({
      name: profile?.name || "",
      email: profile?.email || "",
    });
    setEditing(false);
    setError("");
    setSuccess("");
  }

  function handlePasswordFieldChange(event) {
    const { name, value } = event.target;
    setPasswordForm((current) => ({ ...current, [name]: value }));
  }

  async function handleChangePassword(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (passwordForm.newPassword.length < 8) {
      setError("New password must contain at least 8 characters.");
      return;
    }

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError("New password and confirmation do not match.");
      return;
    }

    if (passwordForm.currentPassword === passwordForm.newPassword) {
      setError("New password must be different from your current password.");
      return;
    }

    setChangingPassword(true);

    try {
      await changeMyPassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordForm(initialPasswordForm);
      setSuccess(
        "Password changed successfully. Sign out and sign in again to refresh your session."
      );
    } catch (err) {
      setError(getErrorMessage(err, "Unable to change password."));
    } finally {
      setChangingPassword(false);
    }
  }

  const initials = (profile?.name || "U")
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");

  return (
    <main className="min-h-screen bg-slate-50 px-4 py-8 sm:px-6 lg:py-12">
      <div className="mx-auto max-w-5xl">
        <div className="mb-8">
          <p className="text-sm font-semibold uppercase tracking-wider text-blue-600">
            Account
          </p>
          <h1 className="mt-2 text-3xl font-extrabold tracking-tight text-slate-900 sm:text-4xl">
            My Profile
          </h1>
          <p className="mt-2 text-slate-600">
            Manage your personal information and account security.
          </p>
        </div>

        {error && (
          <div
            role="alert"
            className="mb-5 flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
          >
            <AlertCircle size={19} className="mt-0.5 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div
            role="status"
            className="mb-5 flex items-start gap-3 rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800"
          >
            <CheckCircle2 size={19} className="mt-0.5 shrink-0" />
            <span>{success}</span>
          </div>
        )}

        {loading ? (
          <div className="flex min-h-64 items-center justify-center rounded-2xl border border-slate-200 bg-white">
            <LoaderCircle className="animate-spin text-blue-600" size={30} />
            <span className="ml-3 text-sm text-slate-600">
              Loading profile...
            </span>
          </div>
        ) : !profile ? (
          <section className="rounded-2xl border border-slate-200 bg-white p-6 text-center sm:p-10">
            <UserRound className="mx-auto text-slate-400" size={38} />
            <h2 className="mt-4 text-xl font-bold text-slate-900">
              Profile unavailable
            </h2>
            <p className="mt-2 text-sm text-slate-600">
              We couldn't load your account information.
            </p>
            <button
              type="button"
              onClick={loadProfile}
              className="mt-5 rounded-xl bg-blue-600 px-5 py-3 font-semibold text-white hover:bg-blue-700"
            >
              Try again
            </button>
          </section>
        ) : (
          <>
            <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <div className="bg-gradient-to-r from-blue-700 to-blue-500 px-6 py-7 text-white sm:px-8">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
                  <div className="grid h-20 w-20 shrink-0 place-items-center rounded-full border-4 border-white/30 bg-white/20 text-2xl font-bold">
                    {initials || <UserRound size={30} />}
                  </div>
                  <div className="min-w-0 flex-1">
                    <h2 className="truncate text-2xl font-bold">
                      {profile.name}
                    </h2>
                    <p className="mt-1 truncate text-sm text-blue-100">
                      {profile.email}
                    </p>
                    <span className="mt-3 inline-flex items-center gap-1.5 rounded-full bg-white/15 px-3 py-1 text-xs font-semibold">
                      <ShieldCheck size={14} />
                      {profile.role || "User"}
                    </span>
                  </div>
                  {!editing && activeTab === "profile" && (
                    <button
                      type="button"
                      onClick={() => {
                        setEditing(true);
                        setError("");
                        setSuccess("");
                      }}
                      className="inline-flex items-center justify-center gap-2 rounded-xl bg-white px-4 py-2.5 text-sm font-semibold text-blue-700 transition hover:bg-blue-50"
                    >
                      <Pencil size={16} />
                      Edit profile
                    </button>
                  )}
                </div>
              </div>

              <div className="flex gap-2 border-b border-slate-200 px-4 pt-4 sm:px-8">
                <button
                  type="button"
                  onClick={() => {
                    setActiveTab("profile");
                    setError("");
                    setSuccess("");
                  }}
                  className={`rounded-t-lg border-b-2 px-4 py-3 text-sm font-semibold ${
                    activeTab === "profile"
                      ? "border-blue-600 text-blue-700"
                      : "border-transparent text-slate-500 hover:text-slate-800"
                  }`}
                >
                  Personal information
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setActiveTab("security");
                    setEditing(false);
                    setError("");
                    setSuccess("");
                  }}
                  className={`rounded-t-lg border-b-2 px-4 py-3 text-sm font-semibold ${
                    activeTab === "security"
                      ? "border-blue-600 text-blue-700"
                      : "border-transparent text-slate-500 hover:text-slate-800"
                  }`}
                >
                  Security
                </button>
              </div>

              {activeTab === "profile" ? (
                <form onSubmit={handleSaveProfile} className="p-6 sm:p-8">
                  <div className="mb-6 flex items-center justify-between gap-3">
                    <div>
                      <h3 className="text-lg font-bold text-slate-900">
                        Personal information
                      </h3>
                      <p className="mt-1 text-sm text-slate-500">
                        Keep your account details up to date.
                      </p>
                    </div>
                    {editing && (
                      <button
                        type="button"
                        onClick={cancelEditing}
                        className="inline-flex items-center gap-1 rounded-lg px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
                      >
                        <X size={16} />
                        Cancel
                      </button>
                    )}
                  </div>

                  <div className="grid gap-5 sm:grid-cols-2">
                    <label className="block text-sm font-medium text-slate-700">
                      Full name
                      <div className="relative mt-2">
                        <UserRound
                          size={18}
                          className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                        />
                        <input
                          name="name"
                          value={form.name}
                          onChange={handleProfileFieldChange}
                          readOnly={!editing}
                          required
                          minLength={2}
                          maxLength={100}
                          className={`w-full rounded-xl border py-3 pl-10 pr-3 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100 ${
                            editing
                              ? "border-slate-300 bg-white"
                              : "border-slate-200 bg-slate-50 text-slate-600"
                          }`}
                        />
                      </div>
                    </label>

                    <label className="block text-sm font-medium text-slate-700">
                      Email address
                      <div className="relative mt-2">
                        <Mail
                          size={18}
                          className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                        />
                        <input
                          type="email"
                          name="email"
                          value={form.email}
                          onChange={handleProfileFieldChange}
                          readOnly={!editing}
                          required
                          maxLength={254}
                          className={`w-full rounded-xl border py-3 pl-10 pr-3 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100 ${
                            editing
                              ? "border-slate-300 bg-white"
                              : "border-slate-200 bg-slate-50 text-slate-600"
                          }`}
                        />
                      </div>
                    </label>

                    <div className="text-sm font-medium text-slate-700">
                      Account role
                      <div className="mt-2 flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 px-3 py-3 text-slate-600">
                        <ShieldCheck size={18} className="text-slate-400" />
                        {profile.role || "User"}
                      </div>
                    </div>

                    <div className="text-sm font-medium text-slate-700">
                      Member since
                      <div className="mt-2 flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 px-3 py-3 text-slate-600">
                        <CalendarDays size={18} className="text-slate-400" />
                        {formatDate(profile.createdAt)}
                      </div>
                    </div>
                  </div>

                  {editing && (
                    <div className="mt-7 flex justify-end">
                      <button
                        type="submit"
                        disabled={saving}
                        className="inline-flex items-center justify-center gap-2 rounded-xl bg-blue-600 px-5 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        {saving ? (
                          <LoaderCircle size={18} className="animate-spin" />
                        ) : (
                          <Save size={18} />
                        )}
                        {saving ? "Saving changes..." : "Save changes"}
                      </button>
                    </div>
                  )}
                </form>
              ) : (
                <form onSubmit={handleChangePassword} className="p-6 sm:p-8">
                  <div className="mb-6">
                    <h3 className="text-lg font-bold text-slate-900">
                      Change password
                    </h3>
                    <p className="mt-1 text-sm text-slate-500">
                      Choose a strong password you don't use elsewhere.
                    </p>
                  </div>

                  <div className="max-w-xl space-y-5">
                    <label className="block text-sm font-medium text-slate-700">
                      Current password
                      <input
                        type="password"
                        name="currentPassword"
                        autoComplete="current-password"
                        required
                        value={passwordForm.currentPassword}
                        onChange={handlePasswordFieldChange}
                        className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                      />
                    </label>

                    <label className="block text-sm font-medium text-slate-700">
                      New password
                      <input
                        type="password"
                        name="newPassword"
                        autoComplete="new-password"
                        minLength={8}
                        maxLength={100}
                        required
                        value={passwordForm.newPassword}
                        onChange={handlePasswordFieldChange}
                        className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                      />
                    </label>

                    <label className="block text-sm font-medium text-slate-700">
                      Confirm new password
                      <input
                        type="password"
                        name="confirmPassword"
                        autoComplete="new-password"
                        required
                        value={passwordForm.confirmPassword}
                        onChange={handlePasswordFieldChange}
                        className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                      />
                    </label>

                    <button
                      type="submit"
                      disabled={changingPassword}
                      className="inline-flex items-center justify-center gap-2 rounded-xl bg-blue-600 px-5 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {changingPassword ? (
                        <LoaderCircle size={18} className="animate-spin" />
                      ) : (
                        <LockKeyhole size={18} />
                      )}
                      {changingPassword
                        ? "Updating password..."
                        : "Update password"}
                    </button>
                  </div>
                </form>
              )}
            </section>
          </>
        )}
      </div>
    </main>
  );
}

export default Profile;
