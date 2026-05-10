import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

// Itens fixos da barra lateral (estrutura simples para facilitar manutenção em grupo).
const NAV_ITEMS = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/posts", label: "Posts" },
  { to: "/exercicios", label: "Exercicios" },
  { to: "/pacientes", label: "Pacientes" },
  { to: "/prontuarios", label: "Prontuarios" },
  { to: "/agenda", label: "Agenda" },
  { to: "/comentarios", label: "Comentarios" }
];

// Layout base do painel: menu lateral + area de conteudo.
export function AdminLayout() {
  const { user, logout } = useAuth();

  return (
    <div style={{ display: "flex", minHeight: "100vh" }}>
      <aside
        style={{
          width: 240,
          background: "linear-gradient(180deg, var(--navy) 0%, var(--navy-2) 100%)",
          color: "white",
          padding: 16
        }}
      >
        <h2 style={{ fontSize: 20, marginBottom: 14 }}>Painel MayaRpg</h2>
        <nav style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              style={({ isActive }) => ({
                color: "white",
                textDecoration: "none",
                borderRadius: 8,
                padding: "8px 10px",
                background: isActive ? "rgba(42,172,191,0.25)" : "transparent",
                border: isActive ? "1px solid rgba(255,255,255,0.15)" : "1px solid transparent"
              })}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        {/* Logout simples para manter o fluxo atual de autenticação. */}
        <button onClick={logout} className="btn btn-ghost" style={{ marginTop: 16, width: "100%" }}>
          Sair
        </button>
      </aside>
      <main style={{ flex: 1, padding: 24 }}>
        <header className="card" style={{ marginBottom: 16 }}>
          <strong>Admin:</strong> {user?.nome ?? user?.email}
        </header>
        <Outlet />
      </main>
    </div>
  );
}
