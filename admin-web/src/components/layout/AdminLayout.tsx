import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const links = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/posts", label: "Posts" },
  { to: "/agenda", label: "Agenda" },
  { to: "/comentarios", label: "Comentarios" }
];

export function AdminLayout() {
  const { user, logout } = useAuth();

  return (
    <div style={{ display: "flex", minHeight: "100vh", fontFamily: "Arial, sans-serif" }}>
      <aside style={{ width: 240, background: "#1f2937", color: "white", padding: 16 }}>
        <h2>Painel MayaRpg</h2>
        <nav style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} style={{ color: "white", textDecoration: "none" }}>
              {link.label}
            </NavLink>
          ))}
        </nav>
        <button onClick={logout} style={{ marginTop: 16 }}>
          Sair
        </button>
      </aside>
      <main style={{ flex: 1, padding: 24 }}>
        <header style={{ marginBottom: 24 }}>
          <strong>Admin:</strong> {user?.nome ?? user?.email}
        </header>
        <Outlet />
      </main>
    </div>
  );
}
