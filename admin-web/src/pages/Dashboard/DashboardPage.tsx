import { useEffect, useState } from "react";
import { api } from "../../services/api";

interface DashboardData {
  consultasDoDia: number;
  horariosBloqueados: number;
  comentariosPendentes: number;
  postsPublicados: number;
}

export function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
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
    <div>
      <h1>Dashboard</h1>
      <p>Consultas do dia: {data.consultasDoDia}</p>
      <p>Horarios bloqueados: {data.horariosBloqueados}</p>
      <p>Comentarios pendentes: {data.comentariosPendentes}</p>
      <p>Posts publicados: {data.postsPublicados}</p>
    </div>
  );
}
