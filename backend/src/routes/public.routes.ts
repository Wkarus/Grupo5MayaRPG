import { Router } from "express";
import { z } from "zod";
import { pool } from "../db/mysql";
import { requireAuth } from "../middlewares/auth";

const createAgendamentoSchema = z.object({
  data: z.string(),
  horario: z.string(),
  nome: z.string().min(2),
  observacao: z.string().optional()
});

export const publicRouter = Router();

publicRouter.get("/posts", async (_req, res, next) => {
  try {
    const [rows] = await pool.query(
      "SELECT id, titulo, conteudo, categoria, status, data_publicacao FROM posts WHERE status = 'PUBLICADO' ORDER BY data_publicacao DESC"
    );
    return res.json(rows);
  } catch (error) {
    next(error);
  }
});

publicRouter.get("/agenda/disponivel", async (_req, res, next) => {
  try {
    const [rows] = await pool.query(
      "SELECT data, horario_inicio, horario_fim, bloqueado FROM agenda ORDER BY data, horario_inicio"
    );
    return res.json(rows);
  } catch (error) {
    // Fallback demo para permitir desenvolvimento da agenda sem banco ativo.
    return res.json([
      { data: "2026-05-08", horario_inicio: "09:00:00", horario_fim: "09:30:00", bloqueado: 1 },
      { data: "2026-05-08", horario_inicio: "10:00:00", horario_fim: "10:30:00", bloqueado: 0 },
      { data: "2026-05-09", horario_inicio: "14:00:00", horario_fim: "14:30:00", bloqueado: 1 }
    ]);
  }
});

publicRouter.post("/agendamentos", requireAuth, async (req, res, next) => {
  try {
    const payload = createAgendamentoSchema.parse(req.body);
    const [conflicts] = await pool.query(
      "SELECT id FROM agendamentos WHERE data = ? AND horario = ? LIMIT 1",
      [payload.data, payload.horario]
    );
    if ((conflicts as unknown[]).length > 0) {
      return res.status(409).json({ message: "Horario ja ocupado." });
    }
    await pool.query(
      "INSERT INTO agendamentos (data, horario, nome, observacao, user_id) VALUES (?, ?, ?, ?, ?)",
      [payload.data, payload.horario, payload.nome, payload.observacao ?? null, req.user?.id ?? null]
    );
    return res.status(201).json({ message: "Agendamento criado com sucesso." });
  } catch (error) {
    next(error);
  }
});
