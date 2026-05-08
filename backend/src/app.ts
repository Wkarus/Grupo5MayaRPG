import cors from "cors";
import express from "express";
import helmet from "helmet";
import morgan from "morgan";
import { authRouter } from "./routes/auth.routes";
import { publicRouter } from "./routes/public.routes";
import { adminRouter } from "./routes/admin.routes";
import { errorHandler, notFoundHandler } from "./middlewares/errorHandler";
import { requireAuth, requireRole } from "./middlewares/auth";

export const app = express();

app.use(helmet());
app.use(cors());
app.use(morgan("dev"));
app.use(express.json());

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
    publicados: "GET /posts",
    agenda: "GET /agenda/disponivel"
  });
});

app.use("/auth", authRouter);
app.use("/", publicRouter);
app.use("/admin", requireAuth, requireRole("ADMIN"), adminRouter);

app.use(notFoundHandler);
app.use(errorHandler);
