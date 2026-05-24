import { useEffect, useMemo, useState } from "react";
import { api } from "../../services/api";

interface Comment {
  id: number;
  autor: string;
  texto: string;
  status: string;
  lido: number;
  createdAt: string;
}

interface UserGroup {
  autor: string;
  comments: Comment[];
  unread: number;
}

function formatDate(value: string) {
  try {
    return new Date(value).toLocaleString("pt-BR", {
      day: "2-digit",
      month: "2-digit",
      hour: "2-digit",
      minute: "2-digit"
    });
  } catch {
    return value;
  }
}

export function ComentariosPage() {
  const [comments, setComments] = useState<Comment[]>([]);
  const [selectedAutor, setSelectedAutor] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function fetchComments() {
    setLoading(true);
    try {
      const response = await api.get<Comment[]>("/admin/comments");
      setComments(response.data);
      if (response.data.length > 0) {
        const autores = [...new Set(response.data.map((c) => c.autor))];
        setSelectedAutor((current) =>
          current && autores.includes(current) ? current : (autores[0] ?? null)
        );
      }
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void fetchComments();
  }, []);

  const userGroups = useMemo<UserGroup[]>(() => {
    const map = new Map<string, Comment[]>();
    for (const comment of comments) {
      const list = map.get(comment.autor) ?? [];
      list.push(comment);
      map.set(comment.autor, list);
    }
    return Array.from(map.entries())
      .map(([autor, list]) => ({
        autor,
        comments: list.sort(
          (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        ),
        unread: list.filter((c) => !c.lido).length
      }))
      .sort((a, b) => {
        if (a.unread !== b.unread) return b.unread - a.unread;
        return a.autor.localeCompare(b.autor, "pt-BR");
      });
  }, [comments]);

  const selectedComments =
    userGroups.find((g) => g.autor === selectedAutor)?.comments ?? [];

  async function selectUser(autor: string) {
    setSelectedAutor(autor);
    const hasUnread = comments.some((c) => c.autor === autor && !c.lido);
    if (!hasUnread) return;

    await api.post("/admin/comments/mark-read", { autor });
    setComments((prev) =>
      prev.map((c) => (c.autor === autor ? { ...c, lido: 1 } : c))
    );
    window.dispatchEvent(new Event("comments-read"));
  }

  async function moderar(id: number, status: "APROVADO" | "REPROVADO") {
    await api.patch(`/admin/comments/${id}/moderar`, { status });
    setComments((prev) => prev.map((c) => (c.id === id ? { ...c, status } : c)));
  }

  return (
    <div>
      <h1 className="page-title">Comentarios</h1>
      <p className="page-subtitle">
        Clique no nome do paciente para ver as mensagens. Bolinha vermelha = mensagem nova.
      </p>

      {loading && <p>Carregando...</p>}

      {!loading && userGroups.length === 0 && (
        <div className="card">
          <p style={{ color: "var(--muted)" }}>Nenhum comentario recebido ainda.</p>
        </div>
      )}

      {userGroups.length > 0 && (
        <div className="comments-layout">
          <div className="comments-users card">
            {userGroups.map((group) => (
              <button
                key={group.autor}
                type="button"
                className={`comment-user-box${selectedAutor === group.autor ? " active" : ""}`}
                onClick={() => void selectUser(group.autor)}
              >
                <span className="comment-user-name">{group.autor}</span>
                {group.unread > 0 && <span className="unread-dot" title="Novas mensagens" />}
              </button>
            ))}
          </div>

          <div className="comments-thread card">
            {selectedAutor ? (
              <>
                <h2 style={{ fontSize: 18, marginBottom: 12 }}>{selectedAutor}</h2>
                {selectedComments.map((comment) => (
                  <div key={comment.id} className="comment-bubble">
                    <p style={{ marginBottom: 6 }}>{comment.texto}</p>
                    <div className="row" style={{ justifyContent: "space-between", flexWrap: "wrap" }}>
                      <span style={{ color: "var(--muted)", fontSize: 12 }}>
                        {formatDate(comment.createdAt)} · {comment.status}
                      </span>
                      <div className="row">
                        <button
                          className="btn btn-teal"
                          type="button"
                          onClick={() => void moderar(comment.id, "APROVADO")}
                        >
                          Aprovar
                        </button>
                        <button
                          className="btn btn-danger"
                          type="button"
                          onClick={() => void moderar(comment.id, "REPROVADO")}
                        >
                          Reprovar
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </>
            ) : (
              <p style={{ color: "var(--muted)" }}>Selecione um paciente.</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
