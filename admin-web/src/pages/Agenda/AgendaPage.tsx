import { useEffect, useMemo, useState } from "react";
import { DayPicker } from "react-day-picker";
import { addMinutes, format, parse } from "date-fns";
import "react-day-picker/style.css";
import { api } from "../../services/api";

interface AgendaItem {
  data: string;
  horario_inicio: string;
  horario_fim: string;
  bloqueado: number | boolean;
}

const TIME_SLOTS = [
  "09:00",
  "09:30",
  "10:00",
  "10:30",
  "11:00",
  "11:30",
  "14:00",
  "14:30",
  "15:00",
  "15:30",
  "16:00",
  "16:30"
];

export function AgendaPage() {
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [selectedSlot, setSelectedSlot] = useState<string | null>(null);
  const [agenda, setAgenda] = useState<AgendaItem[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const selectedDateIso = useMemo(() => format(selectedDate, "yyyy-MM-dd"), [selectedDate]);

  useEffect(() => {
    async function fetchAgenda() {
      try {
        const response = await api.get<AgendaItem[]>("/agenda/disponivel");
        setAgenda(response.data);
        setError(null);
      } catch {
        setError("Nao foi possivel carregar agenda da API.");
      }
    }
    void fetchAgenda();
  }, []);

  const slotsByTime = useMemo(() => {
    const currentDayRows = agenda.filter((item) => item.data === selectedDateIso);
    const map = new Map<string, AgendaItem>();
    currentDayRows.forEach((row) => {
      map.set(row.horario_inicio.slice(0, 5), row);
    });
    return map;
  }, [agenda, selectedDateIso]);

  function getSlotStatus(slot: string) {
    const row = slotsByTime.get(slot);
    if (!row) return "LIVRE";
    return row.bloqueado ? "OCUPADO" : "LIVRE";
  }

  function formatEndTime(slot: string) {
    const parsed = parse(slot, "HH:mm", new Date());
    return format(addMinutes(parsed, 30), "HH:mm:ss");
  }

  async function refreshAgenda() {
    const response = await api.get<AgendaItem[]>("/agenda/disponivel");
    setAgenda(response.data);
  }

  async function marcarOcupado() {
    if (!selectedSlot) return;
    setLoading(true);
    try {
      await api.post("/admin/agenda/block", {
        data: selectedDateIso,
        horario_inicio: `${selectedSlot}:00`,
        horario_fim: formatEndTime(selectedSlot),
        motivo: "Bloqueio manual do admin"
      });
      await refreshAgenda();
    } catch {
      setError("Falha ao marcar horario como ocupado.");
    } finally {
      setLoading(false);
    }
  }

  async function marcarLivre() {
    if (!selectedSlot) return;
    setLoading(true);
    try {
      await api.post("/admin/agenda/unblock", {
        data: selectedDateIso,
        horario_inicio: `${selectedSlot}:00`,
        horario_fim: formatEndTime(selectedSlot)
      });
      await refreshAgenda();
    } catch {
      setError("Falha ao marcar horario como livre.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h1>Agenda</h1>
      <p>Selecione um dia, veja os horarios e altere status de ocupado/livre.</p>
      {error && <p style={{ color: "#b91c1c", marginTop: 12 }}>{error}</p>}

      <div style={{ display: "flex", gap: 24, alignItems: "flex-start", marginTop: 16 }}>
        <div style={{ background: "#fff", padding: 12, borderRadius: 8 }}>
          <DayPicker mode="single" selected={selectedDate} onSelect={(date) => date && setSelectedDate(date)} />
        </div>

        <div style={{ flex: 1, textAlign: "left" }}>
          <h3 style={{ marginTop: 0 }}>Horarios de {format(selectedDate, "dd/MM/yyyy")}</h3>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, minmax(120px, 1fr))", gap: 8 }}>
            {TIME_SLOTS.map((slot) => {
              const status = getSlotStatus(slot);
              const selected = selectedSlot === slot;
              return (
                <button
                  key={slot}
                  onClick={() => setSelectedSlot(slot)}
                  style={{
                    border: selected ? "2px solid #2563eb" : "1px solid #d1d5db",
                    background: status === "OCUPADO" ? "#fee2e2" : "#dcfce7",
                    padding: 10,
                    borderRadius: 8,
                    cursor: "pointer"
                  }}
                >
                  <strong>{slot}</strong>
                  <div>{status}</div>
                </button>
              );
            })}
          </div>

          <div style={{ marginTop: 12, display: "flex", gap: 8 }}>
            <button disabled={loading || !selectedSlot || getSlotStatus(selectedSlot) === "OCUPADO"} onClick={() => void marcarOcupado()}>
              Marcar ocupado
            </button>
            <button disabled={loading || !selectedSlot || getSlotStatus(selectedSlot) === "LIVRE"} onClick={() => void marcarLivre()}>
              Marcar livre
            </button>
            <button disabled={loading || !selectedSlot} onClick={() => setSelectedSlot(null)}>
              Cancelar selecao
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
