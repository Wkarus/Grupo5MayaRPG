import { Router } from "express";
import type { ResultSetHeader, RowDataPacket } from "mysql2";
import { z } from "zod";
import { pool } from "../db/mysql";
import { requireAuth } from "../middlewares/auth";
import { ApiError } from "../utils/ApiError";

const createCommentSchema = z.object({
  texto: z.string().min(1).max(2000),
  postId: z.number().int().positive().nullable().optional()
});

export const commentsRouter = Router();

commentsRouter.post("/comments", requireAuth, async (req, res, next) => {
  try {
    const body = createCommentSchema.parse(req.body);
    const autor =
      (req.user?.nome && req.user.nome.trim()) ||
      (req.user?.email ? req.user.email.split("@")[0] : "Paciente");

    const [result] = await pool.execute<ResultSetHeader>(
      "INSERT INTO comments (autor, texto, status, lido, post_id) VALUES (?, ?, 'PENDENTE', 0, ?)",
      [autor.trim(), body.texto.trim(), body.postId ?? null]
    );

    return res.status(201).json({
      message: "Comentario enviado.",
      id: result.insertId
    });
  } catch (error) {
    next(error);
  }
});

commentsRouter.get("/comments/mine", requireAuth, async (req, res, next) => {
  try {
    const autor =
      (req.user?.nome && req.user.nome.trim()) ||
      (req.user?.email ? req.user.email.split("@")[0] : "");
    if (!autor) {
      return next(new ApiError(400, "Usuario sem nome."));
    }
    const [rows] = await pool.query(
      "SELECT id, texto, status, resposta, post_id as postId, created_at as createdAt FROM comments WHERE autor = ? ORDER BY created_at DESC LIMIT 50",
      [autor]
    );
    return res.json(rows);
  } catch (error) {
    next(error);
  }
});

commentsRouter.get("/posts/:id/comments", requireAuth, async (req, res, next) => {
  try {
    const postId = Number(req.params.id);
    if (!Number.isFinite(postId) || postId < 1) {
      return next(new ApiError(400, "ID invalido."));
    }
    const autor =
      (req.user?.nome && req.user.nome.trim()) ||
      (req.user?.email ? req.user.email.split("@")[0] : "");
    if (!autor) {
      return next(new ApiError(400, "Usuario sem nome."));
    }
    const [rows] = await pool.query<RowDataPacket[]>(
      `SELECT id, texto, resposta, created_at as createdAt
       FROM comments
       WHERE post_id = ? AND autor = ?
       ORDER BY created_at ASC
       LIMIT 50`,
      [postId, autor]
    );
    return res.json(rows);
  } catch (error) {
    next(error);
  }
});
