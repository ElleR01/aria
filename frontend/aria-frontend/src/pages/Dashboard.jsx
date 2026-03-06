import { useEffect, useMemo, useState } from "react";
import { Card, CardTitle, CardValue } from "../ui/Card";
import { Button } from "../ui/Button";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/http";

function euroCents(cents) {
  return (cents / 100).toLocaleString("it-IT", {
    style: "currency",
    currency: "EUR",
  });
}

export default function Dashboard() {
  const navigate = useNavigate();

  const [tracks, setTracks] = useState([]);
  const [royaltiesMap, setRoyaltiesMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  useEffect(() => {
    async function loadDashboard() {
      setLoading(true);
      setErr("");

      try {
        const trackList = await apiFetch("/tracks");
        const safeTracks = Array.isArray(trackList) ? trackList : [];
        setTracks(safeTracks);

        const publishedTracks = safeTracks.filter((t) => t.status === "PUBLISHED");

        const royaltyEntries = await Promise.all(
          publishedTracks.map(async (t) => {
            try {
              const res = await fetch(`http://localhost:8083/royalties/${t.trackId}`);
              if (!res.ok) {
                return [t.trackId, { totalStreams: 0, totalAmountCents: 0 }];
              }
              const data = await res.json();
              return [t.trackId, data];
            } catch {
              return [t.trackId, { totalStreams: 0, totalAmountCents: 0 }];
            }
          })
        );

        setRoyaltiesMap(Object.fromEntries(royaltyEntries));
      } catch (e) {
        setErr(String(e.message || e));
      } finally {
        setLoading(false);
      }
    }

    loadDashboard();
  }, []);

  const totalTracks = tracks.length;

  const totalStreams = useMemo(() => {
    return Object.values(royaltiesMap).reduce(
      (sum, r) => sum + (r.totalStreams || 0),
      0
    );
  }, [royaltiesMap]);

  const totalRoyaltiesCents = useMemo(() => {
    return Object.values(royaltiesMap).reduce(
      (sum, r) => sum + (r.totalAmountCents || 0),
      0
    );
  }, [royaltiesMap]);

  const topTracks = useMemo(() => {
    return [...tracks]
      .map((t) => ({
        ...t,
        totalStreams: royaltiesMap[t.trackId]?.totalStreams || 0,
        totalAmountCents: royaltiesMap[t.trackId]?.totalAmountCents || 0,
      }))
      .sort((a, b) => b.totalStreams - a.totalStreams)
      .slice(0, 5);
  }, [tracks, royaltiesMap]);

  if (loading) {
    return <div className="text-white/70">Caricamento dashboard...</div>;
  }

  if (err) {
    return <div className="text-red-300">Errore: {err}</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight">Dashboard</h1>
          <p className="mt-1 text-sm text-white/60">
            Overview rapido delle performance.
          </p>
        </div>

        <Button onClick={() => navigate("/upload")}>
          ⬆️ Upload new track
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardTitle>Total Plays</CardTitle>
          <CardValue>{totalStreams}</CardValue>
        </Card>

        <Card>
          <CardTitle>Royalties</CardTitle>
          <CardValue>{euroCents(totalRoyaltiesCents)}</CardValue>
        </Card>

        <Card>
          <CardTitle>Tracks</CardTitle>
          <CardValue>{totalTracks}</CardValue>
        </Card>
      </div>

      <Card className="p-0 overflow-hidden">
        <div className="p-4 border-b border-white/10">
          <div className="font-semibold">Top tracks</div>
          <div className="text-sm text-white/60">Classifica basata sugli stream</div>
        </div>

        <div className="divide-y divide-white/10">
          {topTracks.length === 0 ? (
            <div className="p-4 text-white/60">Nessuna traccia disponibile.</div>
          ) : (
            topTracks.map((t) => (
              <div key={t.trackId} className="p-4 flex items-center justify-between">
                <div>
                  <div className="font-medium"><strong>{t.title}</strong></div>
                  <div className="text-sm text-white/50">
                    {t.genre || "-"} • {t.status}
                  </div>
                </div>

                <div className="text-sm text-white/70 flex gap-6">
                  <span>{t.totalStreams} plays</span>
                  <span className="font-semibold">{euroCents(t.totalAmountCents)}</span>
                </div>
              </div>
            ))
          )}
        </div>
      </Card>
    </div>
  );
}