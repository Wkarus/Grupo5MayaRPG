import { useEffect, useState } from "react";
import { api } from "../../services/api";

// Linha que vem do MySQL (mesmo formato do GET /admin/exercises)
type ExerciseRow = {
  id: number;
  titulo: string;
  descricao: string | null;
};

export function ExerciciosPage() {
  const [lista, setLista] = useState<ExerciseRow[]>([]);
  const [titulo, setTitulo] = useState("");
  const [descricao, setDescricao] = useState("");
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState<string | null>(null);

  // Atualiza a lista depois de salvar (GET /admin/exercises)
  async function refreshLista() {
    try {
      const { data } = await api.get<ExerciseRow[]>("/admin/exercises");
      setLista(data);
    } catch {
      setMsg("Nao foi possivel carregar a lista.");
    }
  }

  // Ao abrir a pagina, carrega uma vez
  useEffect(() => {
    let ativo = true;
    void (async () => {
      try {
        const { data } = await api.get<ExerciseRow[]>("/admin/exercises");
        if (ativo) {
          setLista(data);
        }
      } catch {
        if (ativo) {
          setMsg("Nao foi possivel carregar a lista.");
        }
      }
    })();
    return () => {
      ativo = false;
    };
  }, []);

  async function criar(e: React.FormEvent) {
    e.preventDefault();
    setMsg(null);
    setLoading(true);
    try {
      await api.post("/admin/exercises", {
        titulo: titulo.trim(),
        descricao: descricao.trim() || undefined
      });
      setTitulo("");
      setDescricao("");
      await refreshLista();
      setMsg("Salvo.");
    } catch {
      setMsg("Erro ao salvar. Faca login como admin.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h1>Exercicios (app)</h1>
      <p style={{ maxWidth: 560 }}>Cadastro basico para o app Android.</p>

      <form onSubmit={(ev) => void criar(ev)} style={{ marginBottom: 24, maxWidth: 480 }}>
        <div style={{ marginBottom: 12 }}>
          <label>
            <div>Titulo</div>
            <input
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              required
              minLength={2}
              style={{ width: "100%", padding: 8 }}
            />
          </label>
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>
            <div>Descricao (opcional)</div>
            <textarea
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              rows={4}
              style={{ width: "100%", padding: 8 }}
            />
          </label>
        </div>
        <button type="submit" disabled={loading}>
          {loading ? "Salvando..." : "Adicionar"}
        </button>
      </form>

      {msg ? <p>{msg}</p> : null}

      <h2 style={{ fontSize: "1.1rem" }}>Lista</h2>
      <ul style={{ paddingLeft: 18 }}>
        {lista.map((ex) => (
          <li key={ex.id} style={{ marginBottom: 8 }}>
            <strong>{ex.titulo}</strong>
            {ex.descricao ? <div style={{ fontSize: 14 }}>{ex.descricao}</div> : null}
          </li>
        ))}
      </ul>
    </div>
  );
}
