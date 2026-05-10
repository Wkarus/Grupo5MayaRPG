import bcrypt from "bcryptjs";
import * as admin from "firebase-admin";
import { Router } from "express";
import jwt from "jsonwebtoken";
import { z } from "zod";
import type { RowDataPacket, ResultSetHeader } from "mysql2";
import { env } from "../config/env";
import { pool } from "../db/mysql";
import { resolveFirebaseAdmin } from "../services/firebaseAdmin";
import { ApiError } from "../utils/ApiError";

const loginSchema = z.object({
  email: z.string().email(),
  senha: z.string().min(4)
});

const firebaseLoginSchema = z.object({
  idToken: z.string().min(10)
});

type UserRow = {
  id: number;
  email: string;
  nome: string;
  role: "ADMIN" | "USER";
};

export const authRouter = Router();

async function persistFirebaseUser(email: string, nome: string, uid: string): Promise<UserRow> {
  const [existing] = await pool.query<RowDataPacket[]>(
    "SELECT id, email, nome, role FROM users WHERE email = ? LIMIT 1",
    [email.toLowerCase()]
  );
  const found = existing as UserRow[];
  if (found.length) {
    return found[0]!;
  }
  const senhaPlaceholder = bcrypt.hashSync(`__firebase__:${uid}:${env.JWT_SECRET}`, 10);
  const [execRes] = await pool.execute<ResultSetHeader>(
    "INSERT INTO users (email, nome, senha, role) VALUES (?, ?, ?, ?)",
    [email.toLowerCase(), nome.trim() || email.split("@")[0]!, senhaPlaceholder, "USER"]
  );
  const newId = execRes.insertId;
  const userPayload: UserRow = {
    id: Number(newId),
    email: email.toLowerCase(),
    nome,
    role: "USER"
  };
  return userPayload;
}

/**
 * Ligação simples Firebase -> patients:
 * - se já existir paciente com esse e-mail: mantém e força status ATIVO
 * - se não existir: cria um paciente básico
 */
async function syncPatientFromFirebase(email: string, nome: string): Promise<void> {
  const emailNorm = email.trim().toLowerCase();
  const [rows] = await pool.query<RowDataPacket[]>(
    "SELECT id FROM patients WHERE email = ? LIMIT 1",
    [emailNorm]
  );
  if (rows.length > 0) {
    await pool.query("UPDATE patients SET status = 'ATIVO' WHERE id = ?", [rows[0]!.id]);
    return;
  }
  await pool.query(
    "INSERT INTO patients (nome, email, status, observacoes) VALUES (?, ?, 'ATIVO', ?)",
    [nome.trim() || emailNorm.split("@")[0]!, emailNorm, "Criado automaticamente no login Firebase."]
  );
}

authRouter.post("/firebase", async (req, res, next) => {
  try {
    const { idToken } = firebaseLoginSchema.parse(req.body);
    const resolved = resolveFirebaseAdmin();
    if (!resolved.ok) {
      return next(
        new ApiError(
          503,
          "Firebase Admin nao configurado no servidor.",
          resolved.reason === "missing_env" ? "FIREBASE_DISABLED" : "FIREBASE_BAD_CONFIG"
        )
      );
    }

    let decoded: admin.auth.DecodedIdToken;
    try {
      decoded = await admin.auth().verifyIdToken(idToken);
    } catch {
      return next(new ApiError(401, "Token Firebase invalido."));
    }

    const emailRaw = decoded.email;
    if (!emailRaw || typeof emailRaw !== "string") {
      return next(new ApiError(401, "Token sem email."));
    }
    if (decoded.email_verified !== true) {
      return next(new ApiError(403, "Email ainda nao verificado no Firebase."));
    }

    const email = emailRaw.toLowerCase();
    const nome =
      typeof decoded.name === "string" && decoded.name.trim().length > 0
        ? decoded.name.trim()
        : email.split("@")[0]!;

    const user = await persistFirebaseUser(email, nome, decoded.uid);
    await syncPatientFromFirebase(email, nome);
    const token = jwt.sign(user, env.JWT_SECRET, { expiresIn: env.JWT_EXPIRES_IN as jwt.SignOptions["expiresIn"] });
    return res.json({ token, user });
  } catch (error) {
    next(error);
  }
});

authRouter.post("/login", async (req, res, next) => {
  try {
    const { email: emailNorm, senha } = loginSchema.parse(req.body);
    const email = emailNorm.trim().toLowerCase();
    // Login de demonstracao para testar o painel sem dependencia imediata de banco.
    if (email === env.DEMO_ADMIN_EMAIL && senha === env.DEMO_ADMIN_PASSWORD) {
      const demoUser = {
        id: 1,
        email: env.DEMO_ADMIN_EMAIL,
        nome: "Admin Demo",
        role: "ADMIN" as const
      };
      const demoToken = jwt.sign(demoUser, env.JWT_SECRET, {
        expiresIn: env.JWT_EXPIRES_IN as jwt.SignOptions["expiresIn"]
      });
      return res.json({ token: demoToken, user: demoUser });
    }

    const [rows] = await pool.query(
      "SELECT id, email, nome, role FROM users WHERE email = ? AND senha = ? LIMIT 1",
      [email, senha]
    );
    const users = rows as Array<{ id: number; email: string; nome: string; role: "ADMIN" | "USER" }>;
    if (!users.length) {
      throw new ApiError(401, "Credenciais invalidas.");
    }
    const user = users[0]!;
    const token = jwt.sign(user, env.JWT_SECRET, { expiresIn: env.JWT_EXPIRES_IN as jwt.SignOptions["expiresIn"] });
    return res.json({ token, user });
  } catch (error) {
    next(error);
  }
});
