import { useEffect, useRef, useState } from "react";
import { useNavigate, Link, useSearchParams } from "react-router-dom";
import { setToken } from "../auth/token";
import { apiFetch } from "../api/http";

export default function Login() {
  const nav = useNavigate();
  const googleBtnRef = useRef(null);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");
  const [searchParams] = useSearchParams();



  async function submit(e) {
    e.preventDefault();
    setErr("");

    try {
      const data = await apiFetch("/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });

      const token = data.token || data.jwt || data.accessToken;

      if (!token) throw new Error("Token non presente nella risposta");

      setToken(token);
      nav("/");
    } catch (e2) {
      setErr(String(e2.message || e2));
    }
  }

  useEffect(() => {
    const tokenFromGithub = searchParams.get("token");
    if (tokenFromGithub) {
      setToken(tokenFromGithub);
      nav("/");
    }
  }, [searchParams, nav]);

  useEffect(() => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
    if (!clientId) {
      console.error("VITE_GOOGLE_CLIENT_ID mancante");
      return;
    }

    function initGoogle() {
      if (!window.google || !googleBtnRef.current) return;

      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: handleGoogleCredential,
      });

      window.google.accounts.id.renderButton(googleBtnRef.current, {
        theme: "outline",
        size: "large",
        shape: "pill",
        text: "continue_with",
        width: 320,
      });
    }

    const existing = document.getElementById("google-identity-script");
    if (existing) {
      initGoogle();
      return;
    }

    const script = document.createElement("script");
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.id = "google-identity-script";
    script.onload = initGoogle;
    document.body.appendChild(script);
  }, []);

  async function handleGoogleCredential(response) {
    setErr("");

    try {
      const data = await apiFetch("/auth/google", {
        method: "POST",
        body: JSON.stringify({
          idToken: response.credential,
        }),
      });

      const token = data.token || data.jwt || data.accessToken;

      if (!token) throw new Error("Token non presente nella risposta Google");

      setToken(token);
      nav("/");
    } catch (e) {
      setErr(String(e.message || e));
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-950 text-slate-100 p-6">
      <form
        onSubmit={submit}
        className="w-[90%] max-w-sm bg-white/5 border border-white/10 rounded-2xl p-6"
      >
        <h1 className="text-2xl font-bold mb-4">Login artista</h1>

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

        <button className="w-full rounded-xl p-2 bg-white text-black font-semibold">
          Entra
        </button>

        <div className="my-4 flex items-center gap-3">
          <div className="h-px bg-white/10 flex-1" />
          <span className="text-xs text-white/50">oppure</span>
          <div className="h-px bg-white/10 flex-1" />
        </div>

        <div className="flex justify-center">
          <div ref={googleBtnRef}></div>
        </div>
        <div className="mt-3 flex justify-center">
          <button
            type="button"
            onClick={() => window.location.href = "http://localhost:8081/auth/github/login"}
            className="w-[30%] rounded-xl p-2 bg-white/10 border border-white/10 text-black font-semibold hover:bg-white/20"
          >
            Continua con GitHub
          </button>
        </div>

        <div className="mt-4 text-sm text-white/60">
          Non hai un account?{" "}
          <Link to="/register" className="text-white hover:underline">
            Registrati
          </Link>
        </div>
      </form>
    </div>
  );
}