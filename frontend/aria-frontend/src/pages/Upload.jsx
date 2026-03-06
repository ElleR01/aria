import { useState } from "react";
import { Card } from "../ui/Card";
import { Button } from "../ui/Button";
import { apiFetch } from "../api/http";
import { useNavigate } from "react-router-dom";

export default function Upload() {
  const [title, setTitle] = useState("");
  const [genre, setGenre] = useState("");
  const [description, setDescription] = useState("");
  const [durationSec, setDurationSec] = useState("");
  const [file, setFile] = useState(null);

  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState("");
  const [err, setErr] = useState("");
  const nav = useNavigate();

  async function saveDraft() {
    setErr("");
    setMsg("");
    setLoading(true);

    try {
      // 1) crea traccia (DRAFT)
      const created = await apiFetch("/tracks", {
        method: "POST",
        body: JSON.stringify({
          // artistId lo ricavi dal token lato backend (se lo fai così),
          // oppure lo mandi esplicitamente. Qui assumo che backend usi artistId dal JWT.
          title,
          description,
          genre,
          durationSec: durationSec ? Number(durationSec) : null,
        }),
      });

      const trackId = created.trackId;

      // 2) se ho il file, upload audio
      if (file) {
        const fd = new FormData();
        fd.append("file", file); // <<< deve chiamarsi "file" come nel controller

        await apiFetch(`/tracks/${trackId}/audio`, {
          method: "POST",
          body: fd,
          // IMPORTANTISSIMO: non settare Content-Type qui
        });

        setMsg(`Creato draft + audio caricato ✅ (trackId: ${trackId})`);
      } else {
        setMsg(`Creato draft ✅ (trackId: ${trackId}). Ora puoi caricare l’audio.`);
        
      }
      setTimeout(() => nav("/catalog"), 1000);
      
    } catch (e) {
      setErr(String(e.message || e));
    } finally {
      setLoading(false);
    }
  }

  function clear() {
    setTitle("");
    setGenre("");
    setDescription("");
    setDurationSec("");
    setFile(null);
    setMsg("");
    setErr("");
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-extrabold tracking-tight">Upload Track</h1>
        <p className="mt-1 text-sm text-white/60">
          Crea una traccia e (opzionale) carica subito l’audio.
        </p>
      </div>

      <Card>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="text-xs text-white/60">Title</label>
            <input
              className="mt-1 w-full rounded-xl bg-white/5 border border-white/10 px-3 py-2 outline-none"
              placeholder="Es. Leggendario"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>

          <div>
            <label className="text-xs text-white/60">Genre</label>
            <input
              className="mt-1 w-full rounded-xl bg-white/5 border border-white/10 px-3 py-2 outline-none"
              placeholder="Es. Trap"
              value={genre}
              onChange={(e) => setGenre(e.target.value)}
            />
          </div>

          <div className="md:col-span-2">
            <label className="text-xs text-white/60">Description</label>
            <input
              className="mt-1 w-full rounded-xl bg-white/5 border border-white/10 px-3 py-2 outline-none"
              placeholder="Es. singolo"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div>
            <label className="text-xs text-white/60">Duration (sec)</label>
            <input
              type="number"
              className="mt-1 w-full rounded-xl bg-white/5 border border-white/10 px-3 py-2 outline-none"
              placeholder="192"
              value={durationSec}
              onChange={(e) => setDurationSec(e.target.value)}
            />
          </div>

          <div>
            <label className="text-xs text-white/60">Audio file</label>
            <input
              type="file"
              className="mt-1 w-full rounded-xl bg-white/5 border border-white/10 px-3 py-2"
              accept="audio/*"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
            />
            {file && (
              <div className="mt-1 text-xs text-white/60">
                Selezionato: {file.name}
              </div>
            )}
          </div>
        </div>

        <div className="mt-4 flex gap-2">
          <Button onClick={saveDraft} disabled={loading || !title.trim()}>
            {loading ? "Salvataggio..." : "Save draft"}
          </Button>
          <Button variant="ghost" onClick={clear} disabled={loading}>
            Clear
          </Button>
        </div>

        {msg && <div className="mt-3 text-sm text-emerald-300">{msg}</div>}
        {err && <div className="mt-3 text-sm text-red-300">{err}</div>}
      </Card>
    </div>
  );
}