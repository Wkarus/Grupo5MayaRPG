import dotenv from "dotenv";
import { z } from "zod";

dotenv.config();

const envSchema = z.object({
  NODE_ENV: z.string().default("development"),
  PORT: z.coerce.number().default(8080),
  MYSQL_HOST: z.string(),
  MYSQL_PORT: z.coerce.number().default(3306),
  MYSQL_USER: z.string(),
  MYSQL_PASSWORD: z.string(),
  MYSQL_DATABASE: z.string(),
  JWT_SECRET: z.string().min(8),
  JWT_EXPIRES_IN: z.string().default("1d"),
  DEMO_ADMIN_EMAIL: z.string().email().default("mayarpg@gmail.com"),
  DEMO_ADMIN_PASSWORD: z.string().default("1234"),
  ALLOW_START_WITHOUT_DB: z
    .enum(["true", "false"])
    .default("true")
    .transform((value) => value === "true"),
  /** Caminho absoluto ou relativo ao cwd para a JSON da conta de serviço Firebase (opcional). */
  FIREBASE_SERVICE_ACCOUNT_PATH: z.preprocess((v) => {
    if (v === undefined || v === "") return undefined;
    return String(v).trim();
  }, z.string().min(1).optional())
});

const parsed = envSchema.safeParse(process.env);
if (!parsed.success) {
  console.error("Variaveis de ambiente invalidas:", parsed.error.flatten().fieldErrors);
  process.exit(1);
}

export const env = parsed.data;
