import { useState } from "react";
import { api } from "../../services/api";

export function PostsPage() {
  const [loading, setLoading] = useState(false);

  async function createSamplePost() {
    setLoading(true);
    await api.post("/admin/posts", {
      titulo: "Novo post MVP",
      conteudo: "Conteudo inicial do painel admin.",
      categoria: "geral",
      status: "PUBLICADO"
    });
    alert("Post criado com sucesso.");
    setLoading(false);
  }

  return (
    <div>
      <h1 className="page-title">Posts</h1>
      <p className="page-subtitle">Publicação simples para validar integração com o backend.</p>
      <div className="card" style={{ maxWidth: 520 }}>
        <button className="btn btn-primary" onClick={() => void createSamplePost()} disabled={loading}>
          {loading ? "Enviando..." : "Criar post de teste"}
        </button>
      </div>
    </div>
  );
}
