import { useEffect, useMemo, useState } from "react";
import { apiFetch } from "../api/http";
import { getArtistId } from "../auth/jwt";
import { Card } from "../ui/Card";
import { Badge } from "../ui/Badge";
import { Button } from "../ui/Button";

function euroCents(cents) {
  if (cents == null) return "-";
  return (cents / 100).toLocaleString("it-IT", { style: "currency", currency: "EUR" });
}

function StatusBadge({ status }) {
  const s = (status || "").toUpperCase();
  let variant = "default";
  if (s === "DRAFT") variant = "ghost";
  if (s === "SUBMITTED") variant = "default";
  if (s === "PUBLISHED") variant = "success";
  return <Badge variant={variant}>{s || "?"}</Badge>;
}

export default function Royalties() {
  const [tracks, setTracks] = useState([]);
  const [rows, setRows] = useState({}); // trackId -> { totalStreams, totalAmountCents, ... }
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  const published = useMemo(
    () => tracks.filter((t) => (t.status || "").toUpperCase() === "PUBLISHED"),
    [tracks]
  );

  async function loadAll() {
    setErr("");
    setLoading(true);
    try {
      const artistId = getArtistId();
      if (!artistId) throw new Error("artistId mancante nel token. Rifai login.");

      // 1) tracce dell’artista (Catalog, protetto)
      const t = await apiFetch(`/tracks`); // nel backend ora list() usa artistId dal token
      const list = Array.isArray(t) ? t : [];
      setTracks(list);

      // 2) royalties per tracce pubblicate (Royalties NON protetto -> fetch diretto)
      const pub = list.filter((x) => (x.status || "").toUpperCase() === "PUBLISHED");
      const results = await Promise.all(
        pub.map(async (tr) => {
          const res = await fetch(`http://localhost:8083/royalties/${tr.trackId}`);
          if (!res.ok) throw new Error(await res.text());
          const data = await res.json();
          return [tr.trackId, data];
        })
      );

      const map = {};
      for (const [id, data] of results) map[id] = data;
      setRows(map);
    } catch (e) {
      setErr(String(e.message || e));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight">My Royalties</h1>
          <p className="mt-1 text-sm text-white/60">Entrate (demo) dalle tracce pubblicate.</p>
        </div>

        <Button variant="ghost" onClick={loadAll}>Refresh</Button>
      </div>

      <Card>
        {loading ? (
          <div className="p-4 text-white/70">Caricamento...</div>
        ) : err ? (
          <div className="p-4 text-red-300">Errore: {err}</div>
        ) : published.length === 0 ? (
          <div className="p-4 text-white/70">
            Nessuna traccia <b>PUBLISHED</b> ancora. Pubblica un brano per vedere le royalties.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead className="text-white/60">
                <tr className="border-b border-white/10">
                  <th className="text-left font-medium p-3">Titolo</th>
                  <th className="text-left font-medium p-3">Stato</th>
                  <th className="text-left font-medium p-3">Streams</th>
                  <th className="text-left font-medium p-3">Amount</th>
                </tr>
              </thead>

              <tbody>
                {published.map((t) => {
                  const r = rows[t.trackId];
                  return (
                    <tr key={t.trackId} className="border-b border-white/10 hover:bg-white/5">
                      <td className="p-3 font-semibold">{t.title || "-"}</td>
                      <td className="p-3"><StatusBadge status={t.status} /></td>
                      <td className="p-3 text-white/80">{r?.totalStreams ?? 0}</td>
                      <td className="p-3 text-white/80">{euroCents(r?.totalAmountCents ?? 0)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}