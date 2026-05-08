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
      <h1>Posts</h1>
      <p>Tela inicial com acao de criacao para validar integracao.</p>
      <button onClick={() => void createSamplePost()} disabled={loading}>
        {loading ? "Enviando..." : "Criar post de teste"}
      </button>
    </div>
  );
}
