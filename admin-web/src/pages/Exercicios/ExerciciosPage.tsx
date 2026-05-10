import { useEffect, useState } from "react";
import { api } from "../../services/api";

// Linha que vem do MySQL (mesmo formato do GET /admin/exercises)
type ExerciseRow = {
  id: number;
  titulo: string;
  descricao: string | null;
};

// Fluxo da página:
// 1) Lista atual vem de /admin/exercises.
// 2) Formulário cria novo exercício no backend.
// 3) Após salvar, recarrega a lista para refletir imediatamente.
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

  // Ao abrir a página, carrega uma vez.
  // Usamos flag "ativo" para evitar setState se o componente desmontar.
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
      // payload mínimo do requisito (título + descrição opcional)
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
      <h1 className="page-title">Exercicios (app)</h1>
      <p className="page-subtitle" style={{ maxWidth: 560 }}>
        Cadastro basico para o app Android.
      </p>

      <form className="card" onSubmit={(ev) => void criar(ev)} style={{ marginBottom: 24, maxWidth: 560 }}>
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
        <button className="btn btn-teal" type="submit" disabled={loading}>
          {loading ? "Salvando..." : "Adicionar"}
        </button>
      </form>

      {msg ? <p>{msg}</p> : null}

      <div className="card">
      <h2 style={{ fontSize: "1.1rem", marginBottom: 8 }}>Lista</h2>
      <ul style={{ paddingLeft: 18, margin: 0 }}>
        {lista.map((ex) => (
          <li key={ex.id} style={{ marginBottom: 8 }}>
            <strong>{ex.titulo}</strong>
            {ex.descricao ? <div style={{ fontSize: 14 }}>{ex.descricao}</div> : null}
          </li>
        ))}
      </ul>
      </div>
    </div>
  );
}
