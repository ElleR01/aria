import { useEffect, useState } from "react";
import { apiFetch, apiFetchBlob } from "../api/http";
import { getArtistId } from "../auth/jwt.js";
import { Card } from "../ui/Card";
import { Badge } from "../ui/Badge";
import { Button } from "../ui/Button";
import { useOutletContext } from "react-router-dom";

function fmtDuration(sec) {
  if (sec == null) return "-";
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${m}:${String(s).padStart(2, "0")}`;
}

function StatusBadge({ status }) {
  const s = (status || "").toUpperCase();
  let variant = "default";
  if (s === "DRAFT") variant = "ghost";
  if (s === "SUBMITTED") variant = "default";
  if (s === "PUBLISHED") variant = "success"; // se non ce l’hai, torna default
  return <Badge variant={variant}>{s || "?"}</Badge>;
}

export default function Catalog() {
  const [tracks, setTracks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");
  const { nowPlaying, setNowPlaying } = useOutletContext();

  useEffect(() => {
    (async () => {
      try {
        setErr("");
        setLoading(true);
        const artistId = getArtistId();
        const data = await apiFetch(`/tracks?artistId=${artistId}`);
        setTracks(Array.isArray(data) ? data : []);
      } catch (e) {
        setErr(String(e.message || e));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  async function doSubmit(trackId) {
    setErr("");
    try {
      const updated = await apiFetch(`/tracks/${trackId}/submit`, { method: "PUT" });

      // aggiorna la lista in memoria
      setTracks((prev) => prev.map((t) => (t.trackId === trackId ? updated : t)));
    } catch (e) {
      setErr(String(e.message || e));
    }
  }

  async function doPublish(trackId) {
    setErr("");
    try {
      const updated = await apiFetch(`/tracks/${trackId}/publish-with-license`, {
        method: "PUT",
      });

      setTracks((prev) => prev.map((t) => (t.trackId === trackId ? updated : t)));
    } catch (e) {
      setErr(String(e.message || e));
    }
  }

  async function doDelete(trackId) {
    setErr("");
    try {
      await apiFetch(`/tracks/${trackId}`, { method: "DELETE" });
      setTracks((prev) => prev.filter((t) => t.trackId !== trackId));
    } catch (e) {
      setErr(String(e.message || e));
    }
  }

  async function doPlay(trackId) {
    setErr("");
    try {
      await apiFetch(`/tracks/${trackId}/play`, {
        method: "POST",
        headers: {
          "Idempotency-Key": crypto.randomUUID(),
        },
      });

      console.log("Stream registrato");
    } catch (e) {
      setErr(String(e.message || e));
    }
  }
  async function playAndStream(track) {
    setErr("");
    try {
      // registra stream (royalties)
      await apiFetch(`/tracks/${track.trackId}/play`, {
        method: "POST",
        headers: { "Idempotency-Key": crypto.randomUUID() },
      });

      // scarica audio protetto
      const blob = await apiFetchBlob(`/tracks/${track.trackId}/stream`);
      const url = URL.createObjectURL(blob);

      // pulizia URL precedente
      setNowPlaying((prev) => {
        if (prev?.url) URL.revokeObjectURL(prev.url);
        return { trackId: track.trackId, title: track.title, url };
      });
    } catch (e) {
      setErr(String(e.message || e));
    }
  }

  return (
    <>
    <div className="space-y-6 pb-32">
      <div className="flex items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight">My Catalog</h1>
          <p className="mt-1 text-sm text-white/60">Le tue tracce (dal backend).</p>
        </div>
      </div>

      <Card className="w-[99%] max-w-none">
        {loading ? (
          <div className="p-4 text-white/70">Caricamento...</div>
        ) : err ? (
          <div className="p-4 text-red-300">Errore: {err}</div>
        ) : tracks.length === 0 ? (
          <div className="p-4 text-white/70">Nessuna traccia ancora. Crea la tua prima!</div>
        ) : (
          <div className="w-99%">
            <table className="w-[99%] text-sm">
              <thead className="text-white/60">
                <tr className="border-b border-white/10">
                  <th className="text-left font-medium p-3">Titolo</th>
                  <th className="text-left font-medium p-3">Genere</th>
                  <th className="text-left font-medium p-3">Descrizione</th>
                  <th className="text-left font-medium p-3">Durata</th>
                  <th className="text-left font-medium p-3">Stato</th>
                  <th className="text-right font-medium p-3">Azioni</th>
                </tr>
              </thead>

              <tbody>
                {tracks.map((t) => (
                  <tr key={t.trackId} className="border-b border-white/10 hover:bg-white/5">
                    <td className="p-3 font-semibold">{t.title || "-"}</td>
                    <td className="p-3 text-white/80">{t.genre || "-"}</td>
                    <td className="p-3 text-white/70 max-w-[520px] truncate">
                      {t.description || "-"}
                    </td>
                    <td className="p-3 text-white/80">{fmtDuration(t.durationSec)}</td>
                    <td className="p-3">
                      <StatusBadge status={t.status} />
                    </td>
                    <td className="p-3">
                      {/* AZIONI */}
                      <div className="flex items-center justify-end gap-2">
                        {t.status === "DRAFT" && (
                          <>
                            <Button onClick={() => doSubmit(t.trackId)}>
                              Submit
                            </Button>
                            <Button variant="ghost"
                              onClick={() => {
                                if (confirm(`Eliminare "${t.title}"?`)) doDelete(t.trackId);
                              }}
                            >
                              Elimina
                            </Button>
                          </>
                        )}

                        {t.status === "SUBMITTED" && (
                          <Button onClick={() => doPublish(t.trackId)}>Publish</Button>
                        )}

                        {t.status === "PUBLISHED" && (
                          <Button onClick={() => playAndStream(t)}>▶ Play</Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
      </div>
    </>
  );
}