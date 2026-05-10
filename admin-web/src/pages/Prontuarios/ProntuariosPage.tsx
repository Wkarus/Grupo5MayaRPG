import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { api } from "../../services/api";

type Patient = {
  id: number;
  nome: string;
  status: "ATIVO" | "INATIVO";
};

type RecordRow = {
  id: number;
  patientId: number;
  observacao: string;
  createdAt: string;
};

// Fluxo do Prontuário:
// 1) Carrega pacientes ativos para seleção.
// 2) Seleciona paciente e busca histórico dele.
// 3) Nova observação salva no backend e lista atualiza na hora.
export function ProntuariosPage() {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [selectedId, setSelectedId] = useState<number>(0);
  const [records, setRecords] = useState<RecordRow[]>([]);
  const [observacao, setObservacao] = useState("");
  const [msg, setMsg] = useState("");

  // Busca pacientes ativos para seleção no prontuário.
  // Se houver pacientes, já seleciona o primeiro para facilitar uso.
  useEffect(() => {
    async function loadPatients() {
      const response = await api.get("/admin/patients", { params: { status: "ATIVO" } });
      const list: Patient[] = response.data;
      setPatients(list);
      if (list.length && !selectedId) {
        setSelectedId(list[0].id);
      }
    }
    void loadPatients();
  }, []);

  useEffect(() => {
    if (!selectedId) {
      setRecords([]);
      return;
    }
    // Sempre que troca o paciente, recarrega o histórico dele.
    // Isso mantém o painel coerente sem precisar botão "atualizar".
    async function loadRecords() {
      const response = await api.get(`/admin/patients/${selectedId}/records`);
      setRecords(response.data);
    }
    void loadRecords();
  }, [selectedId]);

  async function addRecord(ev: FormEvent) {
    ev.preventDefault();
    if (!selectedId) return;
    await api.post(`/admin/patients/${selectedId}/records`, {
      observacao: observacao.trim()
    });
    setObservacao("");
    setMsg("Prontuário salvo.");
    // Atualiza a lista para já aparecer o novo registro na tela.
    // Evita sensação de "não salvou" para quem está atendendo.
    const response = await api.get(`/admin/patients/${selectedId}/records`);
    setRecords(response.data);
  }

  return (
    <div style={{ display: "grid", gap: 16 }}>
      <h1 className="page-title">Prontuário eletrônico</h1>
      <p className="page-subtitle">Observações clínicas e histórico por paciente.</p>

      <div className="card">
        <label>Paciente</label>
        <br />
        <select value={selectedId} onChange={(e) => setSelectedId(Number(e.target.value))} style={{ minWidth: 320 }}>
          {!patients.length ? <option value={0}>Sem pacientes ativos</option> : null}
          {patients.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nome}
            </option>
          ))}
        </select>
      </div>

      <form className="card" onSubmit={addRecord} style={{ display: "grid", gap: 8 }}>
        <strong>Nova observação clínica</strong>
        <textarea
          value={observacao}
          onChange={(e) => setObservacao(e.target.value)}
          placeholder="Descreva a evolução do paciente..."
          rows={4}
          required
        />
        <button className="btn btn-teal" type="submit" disabled={!selectedId}>
          Salvar observação
        </button>
      </form>

      {msg ? <p>{msg}</p> : null}

      <div className="card">
        <strong>Histórico</strong>
        {/* Histórico em ordem de data (mais recente primeiro vem da API). */}
        <ul style={{ marginTop: 10 }}>
          {records.map((r) => (
            <li key={r.id} style={{ marginBottom: 10 }}>
              <small>{new Date(r.createdAt).toLocaleString("pt-BR")}</small>
              <br />
              {r.observacao}
            </li>
          ))}
          {!records.length ? <li>Sem registros para este paciente.</li> : null}
        </ul>
      </div>
    </div>
  );
}
