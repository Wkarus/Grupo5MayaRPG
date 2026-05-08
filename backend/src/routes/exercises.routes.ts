import { Router } from "express";
import type { ResultSetHeader, RowDataPacket } from "mysql2";
import { pool } from "../db/mysql";
import { requireAuth } from "../middlewares/auth";
import { ApiError } from "../utils/ApiError";

export const exercisesRouter = Router();

// Lista exercicios (precisa estar logado no app com JWT)
exercisesRouter.get("/exercises", requireAuth, async (_req, res, next) => {
  try {
    const [rows] = await pool.query<RowDataPacket[]>(
      "SELECT id, titulo, descricao FROM exercises ORDER BY id ASC"
    );
    return res.json(rows);
  } catch (error) {
    next(error);
  }
});

// Registra que o usuario fez o exercicio (guarda no MySQL)
exercisesRouter.post("/exercises/:exerciseId/checkin", requireAuth, async (req, res, next) => {
  try {
    const exerciseId = Number(req.params.exerciseId);
    if (!Number.isFinite(exerciseId) || exerciseId < 1) {
      return next(new ApiError(400, "ID de exercicio invalido."));
    }

    const [found] = await pool.query<RowDataPacket[]>(
      "SELECT id FROM exercises WHERE id = ? LIMIT 1",
      [exerciseId]
    );
    if (!found.length) {
      return next(new ApiError(404, "Exercicio nao encontrado."));
    }

    const userId = req.user!.id;
    await pool.execute<ResultSetHeader>(
      "INSERT INTO exercise_checkins (user_id, exercise_id) VALUES (?, ?)",
      [userId, exerciseId]
    );

    return res.status(201).json({ message: "Check-in registado." });
  } catch (error) {
    next(error);
  }
});

// Historico de check-ins no servidor (opcional para o app mostrar depois)
exercisesRouter.get("/me/exercise-checkins", requireAuth, async (req, res, next) => {
  try {
    const userId = req.user!.id;
    const [rows] = await pool.query<RowDataPacket[]>(
      `SELECT e.titulo as titulo, c.completed_at as completedAt
       FROM exercise_checkins c
       INNER JOIN exercises e ON e.id = c.exercise_id
       WHERE c.user_id = ?
       ORDER BY c.completed_at DESC
       LIMIT 100`,
      [userId]
    );
    return res.json(rows);
  } catch (error) {
    next(error);
  }
});
