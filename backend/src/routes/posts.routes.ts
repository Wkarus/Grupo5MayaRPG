import { Router } from "express";
import type { RowDataPacket } from "mysql2";
import { pool } from "../db/mysql";
import { requireAuth } from "../middlewares/auth";

export const postsRouter = Router();

type PostRow = RowDataPacket & {
  id: number;
  titulo: string;
  conteudo: string | null;
  categoria: string;
  tipo: string;
  media_url: string | null;
  audience: string;
  data_publicacao: Date | string | null;
};

postsRouter.get("/posts", requireAuth, async (req, res, next) => {
  try {
    const email = req.user?.email?.trim().toLowerCase() ?? "";
    const [rows] = await pool.query<PostRow[]>(
      `SELECT DISTINCT p.id, p.titulo, p.conteudo, p.categoria, p.tipo, p.media_url as mediaUrl,
              p.audience, p.data_publicacao as dataPublicacao
       FROM posts p
       LEFT JOIN post_recipients pr ON pr.post_id = p.id
       LEFT JOIN patients pt ON pt.id = pr.patient_id
       WHERE p.status = 'PUBLICADO'
         AND p.titulo NOT LIKE 'Novo post MVP%'
         AND (
           p.audience = 'TODOS'
           OR (p.audience = 'SELECIONADOS' AND LOWER(COALESCE(pt.email, '')) = ?)
         )
       ORDER BY COALESCE(p.data_publicacao, p.id) DESC
       LIMIT 1`,
      [email]
    );
    return res.json(rows);
  } catch (error) {
    next(error);
  }
});
