import { NavLink, Outlet } from "react-router-dom";
import { useState } from "react";
import { clearToken } from "../auth/token";
import { useNavigate } from "react-router-dom";
import { getUsername } from "../auth/token";

const linkClass = ({ isActive }) =>
  `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition
   ${isActive ? "bg-white/10 text-white" : "text-white/70 hover:text-white hover:bg-white/10"}`;

export default function ArtistLayout() {
  const [nowPlaying, setNowPlaying] = useState(null);
  const username = getUsername();
  const navigate = useNavigate();

  function logout() {
    clearToken();
    navigate("/login");
  }

  return (
    <>
      <div className="flex flex-col min-h-[93vh] bg-slate-950 text-slate-100 pb-28">
        <div className="flex">
          {/* Sidebar */}
          <aside className="w-64 min-h-[93vh] border-r border-white/10 bg-slate-900/40">
            <div className="px-4 py-4 border-b border-white/10">
              <div className="text-xl font-extrabold tracking-tight">Aria</div>
              <div className="text-xs text-white/60"><strong>Artist Dashboard</strong></div>
            </div>

            <nav className="p-3 space-y-1">
              <NavLink to="/" end className={linkClass}>
                <span>📊</span> Dashboard
              </NavLink>
              <NavLink to="/catalog" className={linkClass}>
                <span>🎵</span> My Catalog
              </NavLink>
              <NavLink to="/royalties" className={linkClass}>
                <span>💸</span> My Royalties
              </NavLink>
              <NavLink to="/upload" className={linkClass}>
                <span>⬆️</span> Upload Track
              </NavLink>
              
            </nav>

            <div className="p-3 mt-auto">
              <div className="rounded-xl border border-white/10 bg-white/5 p-3 text-xs text-white/70">
                <div className="font-semibold text-white">Solo mode</div>
                <div className="mt-1">You are running the prototype locally.</div>
              </div>
            </div>
          </aside>

          {/* Main */}
          <main className="flex-1">
            {/* Topbar */}
            <header className="h-14 border-b border-white/10 bg-slate-900/20 flex items-center justify-between px-6">
              <div className="text-sm text-white/70">Welcome back 👋</div>
              <div className="flex items-center gap-3">
                <div className="text-xs text-white/60 uppercase"><strong>{username}</strong>&nbsp;&nbsp;&nbsp;</div>

                <div className="flex items-center gap-3">
                  <button
                    onClick={logout}
                    className="rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-sm hover:bg-white/10"
                    title="Logout"
                  >
                    Logout
                  </button>
                </div>
              </div>
            </header>

            {/* Content */}
            <div className="p-6 pb-28">
              <Outlet context={{ nowPlaying, setNowPlaying }} />
            </div>

          </main>
        </div>

      </div>

      {nowPlaying?.url && (
        <div className="fixed bottom-0 left-0 right-0 z-50 bg-black/90 border-t border-white/10 w-full">
          <div className="px-6 py-3 flex items-center gap-6">
            <div className="text-sm text-white/70 min-w-[220px]">
              In riproduzione
              <div className="font-bold text-white truncate">
                <strong>{nowPlaying.title}</strong>
              </div>
            </div>

            <audio
              controls
              autoPlay
              src={nowPlaying.url}
              className="flex-1"
            />

          </div>
        </div>
      )}
    </>
  );
}