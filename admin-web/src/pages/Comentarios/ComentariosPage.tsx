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
      <h1 className="page-title">Comentarios</h1>
      <p className="page-subtitle">Modere os comentários enviados pelos pacientes.</p>
      {loading && <p>Carregando...</p>}
      <div className="card">
        {comments.map((comment) => (
          <div key={comment.id} style={{ borderBottom: "1px solid #edf0f2", padding: 10 }}>
            <strong>{comment.autor}</strong>{" "}
            <span style={{ color: "#3d5066", fontSize: 13 }}>({comment.status})</span>
            <p style={{ marginTop: 6, marginBottom: 8 }}>{comment.texto}</p>
            <button className="btn btn-teal" onClick={() => void moderar(comment.id, "APROVADO")}>
              Aprovar
            </button>
            <button className="btn btn-danger" onClick={() => void moderar(comment.id, "REPROVADO")} style={{ marginLeft: 8 }}>
              Reprovar
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
