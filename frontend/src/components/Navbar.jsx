
// import { Link, NavLink } from "react-router-dom";
// import { Plane, Menu } from "lucide-react";
// import { useState } from "react";

// export default function Navbar() {
//   const [menuOpen, setMenuOpen] = useState(false);

//   const linkClass = ({ isActive }) =>
//     `text-sm font-medium transition ${
//       isActive ? "text-blue-600" : "text-slate-600 hover:text-blue-600"
//     }`;

//   return (
//     <header className="sticky top-0 z-50 border-b border-slate-200 bg-white/95 backdrop-blur">
//       <div className="container-app flex h-16 items-center justify-between">
//         <Link to="/" className="flex items-center gap-2 text-xl font-extrabold">
//           <span className="grid h-9 w-9 place-items-center rounded-xl bg-blue-600 text-white">
//             <Plane size={19} />
//           </span>
//           <span>Sky<span className="text-blue-600">Go</span></span>
//         </Link>

//         <nav className="hidden items-center gap-7 md:flex">
//           <NavLink to="/" className={linkClass}>Flights</NavLink>
//           <NavLink to="/bookings" className={linkClass}>My Bookings</NavLink>
//           <NavLink to="/profile" className={linkClass}>Profile</NavLink>
//         </nav>

//         <div className="hidden items-center gap-3 md:flex">
//           <Link to="/login" className="rounded-xl px-4 py-2 text-sm font-semibold text-blue-600">
//             Login
//           </Link>
//           <Link to="/register" className="rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700">
//             Sign Up
//           </Link>
//         </div>

//         <button
//           className="rounded-lg p-2 md:hidden"
//           onClick={() => setMenuOpen(!menuOpen)}
//           aria-label="Toggle navigation"
//           aria-expanded={menuOpen}
//         >
//           <Menu />
//         </button>
//       </div>

//       {menuOpen && (
//         <nav className="grid gap-4 border-t border-slate-100 bg-white p-5 md:hidden">
//           <Link to="/" onClick={() => setMenuOpen(false)}>Flights</Link>
//           <Link to="/bookings" onClick={() => setMenuOpen(false)}>My Bookings</Link>
//           <Link to="/profile" onClick={() => setMenuOpen(false)}>Profile</Link>
//           <Link to="/login" onClick={() => setMenuOpen(false)}>Login</Link>
//           <Link to="/register" onClick={() => setMenuOpen(false)}>Sign Up</Link>
//         </nav>
//       )}
//     </header>
//   );
// }



import { useState } from "react";
import { Link, NavLink, useNavigate, useLocation } from "react-router-dom";
import {
  Plane,
  Menu,
  UserRound,
  ChevronDown,
  User,
  Ticket,
  Settings,
  LogOut,
  X,
} from "lucide-react";
import { logout } from "../services/authService";

export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const isLoggedIn = Boolean(sessionStorage.getItem("accessToken"));
  const userName = sessionStorage.getItem("userName") || "My Account";
  const userEmail = sessionStorage.getItem("userEmail") || "";

  const linkClass = ({ isActive }) =>
    `text-sm font-medium transition ${
      isActive
        ? "text-blue-600"
        : "text-slate-600 hover:text-blue-600"
    }`;

  function handleLogout() {
    logout();
    setProfileOpen(false);
    setMenuOpen(false);
    navigate("/", { replace: true });
  }

  function goTo(path) {
    setProfileOpen(false);
    setMenuOpen(false);
    navigate(path);
  }

  return (
    <header className="sticky top-0 z-50 border-b border-slate-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
        <Link
          to="/"
          className="flex items-center gap-2 text-xl font-extrabold"
          onClick={() => setMenuOpen(false)}
        >
          <span className="grid h-9 w-9 place-items-center rounded-xl bg-blue-600 text-white">
            <Plane size={19} />
          </span>
          <span>
            Sky<span className="text-blue-600">Go</span>
          </span>
        </Link>

        <nav className="hidden items-center gap-7 md:flex">
          <NavLink to="/" className={linkClass}>
            Flights
          </NavLink>

          {isLoggedIn && (
            <NavLink to="/bookings" className={linkClass}>
              My Bookings
            </NavLink>
          )}
        </nav>

        <div className="hidden items-center gap-3 md:flex">
          {!isLoggedIn ? (
            <>
              <Link
                to="/login"
                className="rounded-xl px-4 py-2 text-sm font-semibold text-blue-600 hover:bg-blue-50"
              >
                Login
              </Link>
              <Link
                to="/register"
                className="rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
              >
                Sign Up
              </Link>
            </>
          ) : (
            <div className="relative">
              <button
                type="button"
                onClick={() => setProfileOpen((open) => !open)}
                aria-expanded={profileOpen}
                className="flex items-center gap-2 rounded-full border border-slate-200 p-1.5 pr-3 transition hover:bg-slate-50"
              >
                <span className="grid h-9 w-9 place-items-center rounded-full bg-blue-100 text-blue-700">
                  <UserRound size={20} />
                </span>
                <span className="max-w-32 truncate text-sm font-semibold text-slate-700">
                  {userName}
                </span>
                <ChevronDown size={16} className="text-slate-500" />
              </button>

              {profileOpen && (
                <div className="absolute right-0 top-full mt-3 w-64 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl">
                  <div className="border-b border-slate-100 px-4 py-4">
                    <p className="truncate font-semibold text-slate-900">
                      {userName}
                    </p>
                    <p className="mt-1 truncate text-sm text-slate-500">
                      {userEmail}
                    </p>
                  </div>

                  <div className="p-2">
                    <button
                      onClick={() => goTo("/profile")}
                      className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm text-slate-700 hover:bg-slate-50"
                    >
                      <User size={18} />
                      My Profile
                    </button>

                    <button
                      onClick={() => goTo("/bookings")}
                      className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm text-slate-700 hover:bg-slate-50"
                    >
                      <Ticket size={18} />
                      My Bookings
                    </button>

                    <button
                      onClick={() => goTo("/profile")}
                      className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm text-slate-700 hover:bg-slate-50"
                    >
                      <Settings size={18} />
                      Account Settings
                    </button>
                  </div>

                  <div className="border-t border-slate-100 p-2">
                    <button
                      onClick={handleLogout}
                      className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-medium text-red-600 hover:bg-red-50"
                    >
                      <LogOut size={18} />
                      Logout
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        <button
          type="button"
          className="rounded-lg p-2 text-slate-700 md:hidden"
          onClick={() => setMenuOpen((open) => !open)}
          aria-label={menuOpen ? "Close navigation" : "Open navigation"}
          aria-expanded={menuOpen}
        >
          {menuOpen ? <X /> : <Menu />}
        </button>
      </div>

      {menuOpen && (
        <nav className="grid gap-1 border-t border-slate-100 bg-white p-4 md:hidden">
          <Link
            to="/"
            onClick={() => setMenuOpen(false)}
            className="rounded-lg px-3 py-2.5 text-sm text-slate-700 hover:bg-slate-50"
          >
            Flights
          </Link>

          {isLoggedIn ? (
            <>
              <button
                onClick={() => goTo("/profile")}
                className="flex items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm text-slate-700 hover:bg-slate-50"
              >
                <User size={18} /> My Profile
              </button>
              <button
                onClick={() => goTo("/bookings")}
                className="flex items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm text-slate-700 hover:bg-slate-50"
              >
                <Ticket size={18} /> My Bookings
              </button>
              <button
                onClick={() => goTo("/profile")}
                className="flex items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm text-slate-700 hover:bg-slate-50"
              >
                <Settings size={18} /> Account Settings
              </button>
              <button
                onClick={handleLogout}
                className="flex items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm text-red-600 hover:bg-red-50"
              >
                <LogOut size={18} /> Logout
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                onClick={() => setMenuOpen(false)}
                className="rounded-lg px-3 py-2.5 text-sm text-blue-700 hover:bg-blue-50"
              >
                Login
              </Link>
              <Link
                to="/register"
                onClick={() => setMenuOpen(false)}
                className="rounded-lg bg-blue-600 px-3 py-2.5 text-sm font-semibold text-white"
              >
                Sign Up
              </Link>
            </>
          )}
        </nav>
      )}
    </header>
  );
}
