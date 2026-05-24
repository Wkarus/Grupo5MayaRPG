import cors from "cors";
import express from "express";
import helmet from "helmet";
import morgan from "morgan";
import path from "path";
import { authRouter } from "./routes/auth.routes";
import { commentsRouter } from "./routes/comments.routes";
import { exercisesRouter } from "./routes/exercises.routes";
import { publicRouter } from "./routes/public.routes";
import { meRouter } from "./routes/me.routes";
import { postsRouter } from "./routes/posts.routes";
import { adminRouter } from "./routes/admin.routes";
import { errorHandler, notFoundHandler } from "./middlewares/errorHandler";
import { requireAuth, requireRole } from "./middlewares/auth";

export const app = express();

app.use(
  helmet({
    crossOriginResourcePolicy: { policy: "cross-origin" }
  })
);
app.use(cors());
app.use(morgan("dev"));
app.use(express.json());
app.use("/uploads", express.static(path.join(process.cwd(), "uploads")));

app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

// Raiz (browser em localhost:8081/) — evita 404 "Rota nao encontrada" sem caminho.
app.get("/", (_req, res) => {
  res.json({
    service: "Maya Admin API",
    health: "/health",
    login: "POST /auth/login",
    firebaseLogin: "POST /auth/firebase",
    publicados: "GET /posts (auth)",
    agenda: "GET /agenda/disponivel",
    exercicios: "GET /exercises (auth)",
    checkin: "POST /exercises/:id/checkin (auth)",
    historicoExercicios: "GET /me/exercise-checkins (auth)",
    adminExercicios: "GET|POST /admin/exercises (ADMIN)"
  });
});

app.use("/auth", authRouter);
app.use("/", publicRouter);
app.use("/", commentsRouter);
app.use("/", postsRouter);
app.use("/", meRouter);
app.use("/", exercisesRouter);
app.use("/admin", requireAuth, requireRole("ADMIN"), adminRouter);

app.use(notFoundHandler);
app.use(errorHandler);
