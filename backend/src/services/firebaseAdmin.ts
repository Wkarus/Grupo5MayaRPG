import * as fs from "fs";
import * as admin from "firebase-admin";
import { env } from "../config/env";

type FirebaseResolution =
  | { ok: true }
  | { ok: false; reason: "missing_env" | "bad_credentials" };

function trimPath(value: string | undefined): string | undefined {
  if (value == null) return undefined;
  const s = value.trim();
  return s === "" ? undefined : s;
}

/**
 * Liga o Firebase Admin a partir da JSON de conta de serviço (descarregada no Console Firebase).
 */
export function resolveFirebaseAdmin(): FirebaseResolution {
  const pathAuth = trimPath(env.FIREBASE_SERVICE_ACCOUNT_PATH);
  if (!pathAuth) return { ok: false, reason: "missing_env" };
  try {
    if (!fs.existsSync(pathAuth)) {
      console.error("[firebase] Credenciais: ficheiro nao existe:", pathAuth);
      return { ok: false, reason: "bad_credentials" };
    }
    const json = fs.readFileSync(pathAuth, "utf8");
    const key = JSON.parse(json);
    if (!admin.apps.length) {
      admin.initializeApp({ credential: admin.credential.cert(key as admin.ServiceAccount) });
    }
    return { ok: true };
  } catch (e) {
    console.error("[firebase] Falha ao iniciar Firebase Admin:", e);
    return { ok: false, reason: "bad_credentials" };
  }
}
