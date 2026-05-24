import { Router } from "express";
import type { RowDataPacket } from "mysql2";
import { pool } from "../db/mysql";
import { requireAuth } from "../middlewares/auth";

export const meRouter = Router();

type AgendamentoRow = RowDataPacket & {
  data: string;
  horario: string;
};

/** Proxima consulta do usuario logado (MySQL). */
meRouter.get("/me/proxima-consulta", requireAuth, async (req, res, next) => {
  try {
    const userId = req.user?.id;
    if (!userId) {
      return res.json(null);
    }

    const [rows] = await pool.query<AgendamentoRow[]>(
      `SELECT data, horario
       FROM agendamentos
       WHERE user_id = ?
         AND (
           data > CURDATE()
           OR (data = CURDATE() AND horario >= DATE_FORMAT(NOW(), '%H:%i'))
         )
       ORDER BY data ASC, horario ASC
       LIMIT 1`,
      [userId]
    );

    if (!rows.length) {
      return res.json(null);
    }

    const row = rows[0]!;
    return res.json({
      data: row.data,
      horario: row.horario
    });
  } catch (error) {
    next(error);
  }
});
