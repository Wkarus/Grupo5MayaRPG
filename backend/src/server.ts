import { app } from "./app";
import { env } from "./config/env";
import { ensureClinicalSchema } from "./db/ensureClinicalSchema";
import { ensureCommentsSchema } from "./db/ensureCommentsSchema";
import { ensureExerciseSchema } from "./db/ensureExerciseSchema";
import { ensurePostsSchema } from "./db/ensurePostsSchema";
import { checkDatabaseConnection, pool } from "./db/mysql";

async function bootstrap() {
  try {
    await checkDatabaseConnection();
    await ensureExerciseSchema(pool); // tabelas exercises + checkins
    await ensureClinicalSchema(pool); // tabelas patients + patient_records
    await ensureCommentsSchema(pool); // comments + coluna lido
    await ensurePostsSchema(pool); // posts midia + destinatarios
  } catch (error) {
    if (!env.ALLOW_START_WITHOUT_DB) {
      console.error("Falha ao iniciar backend:", error);
      process.exit(1);
    }
    console.warn("Banco indisponivel no startup. Subindo backend em modo degradado para testes.");
  }

  app.listen(env.PORT, () => {
    console.log(`Backend rodando em http://localhost:${env.PORT}`);
  });
}

void bootstrap();
