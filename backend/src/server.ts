import { app } from "./app";
import { env } from "./config/env";
import { checkDatabaseConnection } from "./db/mysql";

async function bootstrap() {
  try {
    await checkDatabaseConnection();
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
