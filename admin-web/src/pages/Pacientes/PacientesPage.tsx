import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { api } from "../../services/api";

type Patient = {
  id: number;
  nome: string;
  telefone: string | null;
  email: string | null;
  status: "ATIVO" | "INATIVO";
  observacoes: string | null;
};

const initialForm = {
  nome: "",
  telefone: "",
  email: "",
  status: "ATIVO" as "ATIVO" | "INATIVO",
  observacoes: ""
};

// Fluxo de Pacientes:
// - topo: busca + filtro por status.
// - meio: formulário único para criar/editar.
// - base: tabela com ações de editar/remover.
export function PacientesPage() {
  const [items, setItems] = useState<Patient[]>([]);
  const [q, setQ] = useState("");
  const [status, setStatus] = useState("");
  const [form, setForm] = useState(initialForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [msg, setMsg] = useState("");

  // Carrega a lista respeitando busca textual e status.
  // O backend já filtra, então o front só envia os parâmetros.
  /**
   * load()
   * - Entrada: usa estados `q` e `status`.
   * - Saída: atualiza `items` com a lista filtrada.
   * - Papel: manter tabela sincronizada com filtros e operações CRUD.
   */
  async function load() {
    const response = await api.get("/admin/patients", {
      params: { q: q || undefined, status: status || undefined }
    });
    setItems(response.data);
  }

  useEffect(() => {
    void load();
  }, [q, status]);

  // Preenche o formulário com dados da linha selecionada.
  /**
   * startEdit(p)
   * - Entrada: paciente clicado na tabela.
   * - Saída: ativa modo edição (`editingId`) e preenche `form`.
   * - Papel: permitir editar no mesmo formulário de criação.
   */
  function startEdit(p: Patient) {
    setEditingId(p.id);
    setForm({
      nome: p.nome,
      telefone: p.telefone || "",
      email: p.email || "",
      status: p.status,
      observacoes: p.observacoes || ""
    });
  }

  /**
   * resetForm()
   * - Entrada: nenhuma.
   * - Saída: limpa formulário e desativa modo edição.
   * - Papel: voltar para estado neutro após salvar/cancelar.
   */
  function resetForm() {
    setEditingId(null);
    setForm(initialForm);
  }

  // Mesmo formulário serve para criar e editar.
  // A decisão acontece pelo editingId (null = criação).
  /**
   * save(ev)
   * - Entrada: submit do formulário.
   * - Saída: cria ou atualiza paciente no backend.
   * - Papel: centralizar lógica de persistência (POST/PUT) em uma função.
   */
  async function save(ev: FormEvent) {
    ev.preventDefault();
    const payload = {
      nome: form.nome.trim(),
      telefone: form.telefone.trim() || undefined,
      email: form.email.trim() || undefined,
      status: form.status,
      observacoes: form.observacoes.trim() || undefined
    };
    if (editingId) {
      await api.put(`/admin/patients/${editingId}`, payload);
      setMsg("Paciente atualizado.");
    } else {
      await api.post("/admin/patients", payload);
      setMsg("Paciente criado.");
    }
    resetForm();
    await load();
  }

  /**
   * removePatient(id)
   * - Entrada: id da linha selecionada.
   * - Saída: remove no backend e recarrega tabela.
   * - Papel: exclusão com confirmação para evitar erro humano.
   */
  async function removePatient(id: number) {
    // Remoção com confirmação para evitar clique acidental.
    if (!window.confirm("Remover paciente?")) return;
    await api.delete(`/admin/patients/${id}`);
    setMsg("Paciente removido.");
    if (editingId === id) resetForm();
    await load();
  }

  return (
    <div style={{ display: "grid", gap: 16 }}>
      <h1 className="page-title">Pacientes</h1>
      <p className="page-subtitle">CRUD com busca, filtro e status ativo/inativo.</p>

      <div className="card" style={{ display: "flex", gap: 8 }}>
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Buscar por nome, telefone ou e-mail"
          style={{ flex: 1, padding: 8 }}
        />
        <select value={status} onChange={(e) => setStatus(e.target.value)} style={{ padding: 8 }}>
          <option value="">Todos</option>
          <option value="ATIVO">Ativo</option>
          <option value="INATIVO">Inativo</option>
        </select>
      </div>

      <form className="card" onSubmit={save} style={{ display: "grid", gap: 8 }}>
        {/* Quando editingId existe, usuário está no modo edição. */}
        <strong>{editingId ? "Editar paciente" : "Novo paciente"}</strong>
        <input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} placeholder="Nome" required />
        <input value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} placeholder="Telefone" />
        <input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="E-mail" type="email" />
        <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value as "ATIVO" | "INATIVO" })}>
          <option value="ATIVO">Ativo</option>
          <option value="INATIVO">Inativo</option>
        </select>
        <textarea
          value={form.observacoes}
          onChange={(e) => setForm({ ...form, observacoes: e.target.value })}
          placeholder="Observações clínicas iniciais"
          rows={3}
        />
        <div style={{ display: "flex", gap: 8 }}>
          <button className="btn btn-teal" type="submit">{editingId ? "Salvar alterações" : "Criar paciente"}</button>
          {editingId ? (
            <button className="btn btn-ghost" type="button" onClick={resetForm}>
              Cancelar edição
            </button>
          ) : null}
        </div>
      </form>

      {msg ? <p>{msg}</p> : null}

      <div className="card">
      {/* Tabela simples: foco em leitura rápida para secretaria/atendimento. */}
      <table>
        <thead>
          <tr>
            <th style={{ textAlign: "left" }}>Nome</th>
            <th style={{ textAlign: "left" }}>Telefone</th>
            <th style={{ textAlign: "left" }}>Status</th>
            <th style={{ textAlign: "left" }}>Ações</th>
          </tr>
        </thead>
        <tbody>
          {items.map((p) => (
            <tr key={p.id} style={{ borderTop: "1px solid #eee" }}>
              <td>{p.nome}</td>
              <td>{p.telefone || "-"}</td>
              <td>{p.status}</td>
              <td style={{ display: "flex", gap: 8 }}>
                <button className="btn btn-ghost" type="button" onClick={() => startEdit(p)}>
                  Editar
                </button>
                <button className="btn btn-danger" type="button" onClick={() => removePatient(p.id)}>
                  Remover
                </button>
              </td>
            </tr>
          ))}
          {!items.length ? (
            <tr>
              <td colSpan={4} style={{ paddingTop: 12 }}>
                Nenhum paciente encontrado.
              </td>
            </tr>
          ) : null}
        </tbody>
      </table>
      </div>
    </div>
  );
}
