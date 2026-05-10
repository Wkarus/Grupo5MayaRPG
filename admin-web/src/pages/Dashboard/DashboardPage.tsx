import { useEffect, useState } from "react";
import { api } from "../../services/api";

interface DashboardData {
  consultasDoDia: number;
  horariosBloqueados: number;
  comentariosPendentes: number;
  postsPublicados: number;
}

// Fluxo da página:
// 1) Busca os indicadores no endpoint /admin/dashboard ao abrir.
// 2) Exibe estado de loading/erro para não deixar tela "travada".
// 3) Renderiza cards de visão rápida para decisão do admin.
export function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    /**
     * fetchData()
     * - Entrada: nenhuma (usa token do interceptor e rota fixa).
     * - Saída: atualiza states locais (`data`, `error`, `loading`).
     * - Papel: buscar os indicadores principais do painel ao abrir a página.
     */
    async function fetchData() {
      setLoading(true);
      try {
        const response = await api.get("/admin/dashboard");
        setData(response.data);
        setError(null);
      } catch {
        // Evita loading infinito quando API retorna erro.
        setError("Nao foi possivel carregar o dashboard.");
      } finally {
        setLoading(false);
      }
    }
    void fetchData();
  }, []);

  if (loading) return <p>Carregando dashboard...</p>;
  if (error) return <p>{error}</p>;
  if (!data) return <p>Sem dados.</p>;

  return (
    <div style={{ display: "grid", gap: 20 }}>
      <div
        style={{
          background: "linear-gradient(135deg, #0f2236 0%, #1e3a57 100%)",
          color: "#fff",
          borderRadius: 16,
          padding: "20px 24px"
        }}
      >
        <p style={{ margin: 0, opacity: 0.85 }}>Painel administrativo</p>
        <h1 style={{ margin: "8px 0 0 0", fontSize: 28 }}>Dashboard Maya RPG</h1>
      </div>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(210px, 1fr))",
          gap: 14
        }}
      >
        <Card title="Consultas do dia" value={data.consultasDoDia} color="#2AACBF" icon="📅" />
        <Card title="Horarios bloqueados" value={data.horariosBloqueados} color="#F0A500" icon="⛔" />
        <Card title="Comentarios pendentes" value={data.comentariosPendentes} color="#E5484D" icon="💬" />
        <Card title="Posts publicados" value={data.postsPublicados} color="#17B978" icon="📰" />
      </div>

      <div
        style={{
          background: "#fff",
          border: "1px solid rgba(0,0,0,0.08)",
          borderRadius: 14,
          padding: 16
        }}
      >
        <h2 style={{ marginTop: 0, marginBottom: 8, fontSize: 18 }}>Resumo rapido</h2>
        <p style={{ margin: 0, color: "#3d5066" }}>
          Hoje voce tem <strong>{data.consultasDoDia}</strong> consulta(s) e{" "}
          <strong>{data.comentariosPendentes}</strong> comentario(s) aguardando acao.
        </p>
      </div>
    </div>
  );
}

type CardProps = {
  title: string;
  value: number;
  color: string;
  icon: string;
};

/**
 * Card()
 * - Entrada: título, valor numérico, cor e ícone.
 * - Saída: bloco visual reutilizável para os indicadores do dashboard.
 * - Papel: evitar repetição de markup dos cards.
 */
function Card({ title, value, color, icon }: CardProps) {
  return (
    <div
      style={{
        background: "#fff",
        border: "1px solid rgba(0,0,0,0.08)",
        borderRadius: 14,
        padding: 14
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <span style={{ color: "#3d5066", fontSize: 13 }}>{title}</span>
        <span style={{ fontSize: 18 }}>{icon}</span>
      </div>
      <p style={{ margin: "8px 0 0 0", fontSize: 34, lineHeight: 1, color, fontWeight: 700 }}>{value}</p>
    </div>
  );
}
