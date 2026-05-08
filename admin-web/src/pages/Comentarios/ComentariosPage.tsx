import { useEffect, useState } from "react";
import { api } from "../../services/api";

interface Comment {
  id: number;
  autor: string;
  texto: string;
  status: string;
}

export function ComentariosPage() {
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function fetchComments() {
      setLoading(true);
      const response = await api.get("/admin/comments");
      setComments(response.data);
      setLoading(false);
    }
    void fetchComments();
  }, []);

  async function moderar(id: number, status: "APROVADO" | "REPROVADO") {
    await api.patch(`/admin/comments/${id}/moderar`, { status });
    setComments((prev) => prev.map((c) => (c.id === id ? { ...c, status } : c)));
  }

  return (
    <div>
      <h1>Comentarios</h1>
      {loading && <p>Carregando...</p>}
      {comments.map((comment) => (
        <div key={comment.id} style={{ borderBottom: "1px solid #ddd", padding: 8 }}>
          <strong>{comment.autor}</strong> - {comment.status}
          <p>{comment.texto}</p>
          <button onClick={() => void moderar(comment.id, "APROVADO")}>Aprovar</button>
          <button onClick={() => void moderar(comment.id, "REPROVADO")} style={{ marginLeft: 8 }}>
            Reprovar
          </button>
        </div>
      ))}
    </div>
  );
}
