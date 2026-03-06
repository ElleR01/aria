import { Routes, Route } from "react-router-dom";

import RequireAuth from "./auth/RequireAuth";
import ArtistLayout from "./layouts/ArtistLayout";

import Dashboard from "./pages/Dashboard";
import Catalog from "./pages/Catalog";
import Royalties from "./pages/Royalties";
import Upload from "./pages/Upload";
import Register from "./pages/Register";
import Login from "./pages/Login";

export default function App() {
  return (
    <Routes>

      {/* PAGINA PUBBLICA */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      
      {/* AREA PROTETTA */}
      <Route element={<RequireAuth />}>
        <Route element={<ArtistLayout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/catalog" element={<Catalog />} />
          <Route path="/royalties" element={<Royalties />} />
          <Route path="/upload" element={<Upload />} />
        </Route>
      </Route>

    </Routes>
  );
}