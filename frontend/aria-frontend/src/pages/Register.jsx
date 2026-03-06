import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { setToken } from "../auth/token";

export default function Register() {
  const nav = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setErr("");
    setLoading(true);

    try {
      const res = await fetch("http://localhost:8081/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      if (!res.ok) {
        throw new Error(await res.text());
      }

      const data = await res.json();
      const token = data.token || data.jwt ||data.accessToken;

      if (!token) {
        throw new Error("Token non presente nella risposta");
      }

      setToken(token);
      nav("/");
    } catch (e2) {
      setErr(String(e2.message || e2));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-950 text-slate-100 p-6">
      <form
        onSubmit={submit}
        className="w-[90%] max-w-sm bg-white/5 border border-white/10 rounded-2xl p-6"
      >
        <h1 className="text-2xl font-bold mb-4">Registrati</h1>

        <label className="block text-sm mb-1">Username</label>
        <input
          className="w-[99%] mb-3 p-2 rounded bg-black/30 border border-white/10"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />

        <label className="block text-sm mb-1">Password</label>
        <input
          type="password"
          className="w-[99%] mb-4 p-2 rounded bg-black/30 border border-white/10"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        {err && <div className="text-red-400 text-sm mb-3">{err}</div>}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-xl p-2 bg-white text-black font-semibold hover:bg-white/90 transition disabled:opacity-60"
        >
          {loading ? "Registrazione..." : "Registrati"}
        </button>

        <div className="mt-4 text-sm text-white/60">
          Hai già un account?{" "}
          <Link to="/login" className="text-white hover:underline">
            Accedi
          </Link>
        </div>
      </form>
    </div>
  );
}