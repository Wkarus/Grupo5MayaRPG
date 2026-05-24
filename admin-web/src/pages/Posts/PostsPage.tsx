import { useEffect, useRef, useState } from "react";
import { api } from "../../services/api";

interface Patient {
  id: number;
  nome: string;
  email: string | null;
  status: string;
}

interface PostItem {
  id: number;
  titulo: string;
  conteudo: string | null;
  tipo: string;
  mediaUrl: string | null;
  audience: string;
  destinatarios: string | null;
  dataPublicacao: string | null;
}

const apiBase = import.meta.env.VITE_API_URL || "http://localhost:8081";

function tipoLabel(tipo: string) {
  if (tipo === "IMAGEM") return "Foto";
  if (tipo === "VIDEO") return "Video";
  return "Texto";
}

export function PostsPage() {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [titulo, setTitulo] = useState("");
  const [conteudo, setConteudo] = useState("");
  const [audience, setAudience] = useState<"TODOS" | "SELECIONADOS">("TODOS");
  const [selectedPatients, setSelectedPatients] = useState<number[]>([]);
  const [mediaFile, setMediaFile] = useState<File | null>(null);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [posts, setPosts] = useState<PostItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [messageOk, setMessageOk] = useState(true);

  async function loadData() {
    const [patientsRes, postsRes] = await Promise.all([
      api.get<Patient[]>("/admin/patients"),
      api.get<PostItem[]>("/admin/posts")
    ]);
    setPatients(patientsRes.data.filter((p) => p.status === "ATIVO"));
    setPosts(postsRes.data);
  }

  useEffect(() => {
    void loadData();
  }, []);

  function togglePatient(id: number) {
    setSelectedPatients((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  }

  async function publishPost() {
    if (!titulo.trim()) {
      setMessageOk(false);
      setMessage("Informe um titulo.");
      return;
    }
    if (!conteudo.trim() && !mediaFile) {
      setMessageOk(false);
      setMessage("Escreva um comentario ou envie foto/video.");
      return;
    }
    if (audience === "SELECIONADOS" && selectedPatients.length === 0) {
      setMessageOk(false);
      setMessage("Selecione ao menos um paciente.");
      return;
    }

    setLoading(true);
    setMessage("");
    try {
      const form = new FormData();
      form.append("titulo", titulo.trim());
      form.append("conteudo", conteudo.trim());
      form.append("categoria", "maya");
      form.append("status", "PUBLICADO");
      form.append("audience", audience);
      form.append("patientIds", JSON.stringify(selectedPatients));
      if (mediaFile) {
        form.append("media", mediaFile);
      }

      await api.post("/admin/posts", form);

      setTitulo("");
      setConteudo("");
      setMediaFile(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
      setSelectedPatients([]);
      setAudience("TODOS");
      setMessageOk(true);
      setMessage("Post publicado com sucesso.");
      await loadData();
    } catch {
      setMessageOk(false);
      setMessage("Nao foi possivel publicar. Verifique os dados e tente de novo.");
    } finally {
      setLoading(false);
    }
  }

  async function deletePost(id: number) {
    if (!window.confirm("Apagar este post?")) return;
    await api.delete(`/admin/posts/${id}`);
    setPosts((prev) => prev.filter((p) => p.id !== id));
  }

  return (
    <div className="posts-page">
      <header className="posts-hero">
        <div>
          <p className="posts-hero-kicker">Conteudo para pacientes</p>
          <h1 className="page-title">Posts da Maya</h1>
          <p className="page-subtitle">
            Publique texto, foto ou video na pagina inicial do app — para todos ou para quem
            escolher.
          </p>
        </div>
        <div className="posts-hero-stat">
          <span className="posts-hero-stat-num">{posts.length}</span>
          <span className="posts-hero-stat-label">publicados</span>
        </div>
      </header>

      <div className="posts-grid">
        <section className="card posts-form-card">
          <h2 className="posts-section-title">Nova publicacao</h2>

          <div className="field">
            <label className="field-label">Titulo</label>
            <input
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              placeholder="Ex.: Dica de postura"
              className="field-input"
            />
          </div>

          <div className="field">
            <label className="field-label">Comentario</label>
            <span className="field-hint">Opcional se enviar foto ou video</span>
            <textarea
              value={conteudo}
              onChange={(e) => setConteudo(e.target.value)}
              rows={4}
              placeholder="Mensagem para o paciente..."
              className="field-input field-textarea"
            />
          </div>

          <div className="field">
            <label className="field-label">Foto ou video</label>
            <button
              type="button"
              className="file-drop"
              onClick={() => fileInputRef.current?.click()}
            >
              <span className="file-drop-icon" aria-hidden>
                📎
              </span>
              <span className="file-drop-text">
                {mediaFile ? mediaFile.name : "Clique para escolher imagem ou video"}
              </span>
              <span className="file-drop-hint">PNG, JPG, MP4 ate 80 MB</span>
            </button>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*,video/*"
              className="file-input-hidden"
              onChange={(e) => setMediaFile(e.target.files?.[0] ?? null)}
            />
          </div>

          <div className="field">
            <label className="field-label">Enviar para</label>
            <div className="audience-pills">
              <button
                type="button"
                className={`audience-pill${audience === "TODOS" ? " active" : ""}`}
                onClick={() => setAudience("TODOS")}
              >
                Todos os pacientes
              </button>
              <button
                type="button"
                className={`audience-pill${audience === "SELECIONADOS" ? " active" : ""}`}
                onClick={() => setAudience("SELECIONADOS")}
              >
                Escolher pacientes
              </button>
            </div>
          </div>

          {audience === "SELECIONADOS" && (
            <div className="patients-picker">
              {patients.length === 0 && (
                <p className="field-hint">Nenhum paciente ativo cadastrado.</p>
              )}
              {patients.map((p) => (
                <label
                  key={p.id}
                  className={`patient-chip${selectedPatients.includes(p.id) ? " selected" : ""}`}
                >
                  <input
                    type="checkbox"
                    checked={selectedPatients.includes(p.id)}
                    onChange={() => togglePatient(p.id)}
                  />
                  <span className="patient-chip-name">{p.nome}</span>
                  {p.email && <span className="patient-chip-email">{p.email}</span>}
                </label>
              ))}
            </div>
          )}

          <button
            className="btn btn-teal posts-submit"
            disabled={loading}
            onClick={() => void publishPost()}
          >
            {loading ? "Publicando..." : "Publicar na pagina inicial"}
          </button>

          {message && (
            <p className={`posts-feedback${messageOk ? " ok" : " err"}`} role="status">
              {message}
            </p>
          )}
        </section>

        <section className="posts-list-section">
          <h2 className="posts-section-title">Publicados</h2>
          <div className="posts-list">
            {posts.length === 0 && (
              <div className="card posts-empty">
                <span className="posts-empty-icon" aria-hidden>
                  📭
                </span>
                <p>Nenhum post publicado ainda.</p>
              </div>
            )}
            {posts.map((post) => (
              <article key={post.id} className="card post-card">
                <div className="post-card-head">
                  <h3 className="post-card-title">{post.titulo}</h3>
                  <div className="post-badges">
                    <span className="post-badge post-badge-type">{tipoLabel(post.tipo)}</span>
                    <span className="post-badge post-badge-audience">
                      {post.audience === "TODOS" ? "Todos" : "Selecionados"}
                    </span>
                  </div>
                </div>
                {post.conteudo && <p className="post-card-body">{post.conteudo}</p>}
                {post.audience === "SELECIONADOS" && post.destinatarios && (
                  <p className="post-card-meta">Para: {post.destinatarios}</p>
                )}
                  {post.mediaUrl && (
                  <a
                    className="post-media-link"
                    href={`${apiBase}${post.mediaUrl}`}
                    target="_blank"
                    rel="noreferrer"
                  >
                    Ver midia →
                  </a>
                )}
                <button
                  className="btn btn-danger"
                  type="button"
                  style={{ marginTop: 10, fontSize: 13, padding: "6px 12px" }}
                  onClick={() => void deletePost(post.id)}
                >
                  Apagar
                </button>
              </article>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
