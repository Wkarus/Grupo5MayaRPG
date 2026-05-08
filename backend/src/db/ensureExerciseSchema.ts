import type { Pool } from "mysql2/promise";
import type { RowDataPacket } from "mysql2";

// Cria tabelas se nao existirem e coloca 3 exercicios de exemplo se a tabela estiver vazia
export async function ensureExerciseSchema(pool: Pool): Promise<void> {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS exercises (
      id INT NOT NULL AUTO_INCREMENT,
      titulo VARCHAR(255) NOT NULL,
      descricao VARCHAR(1000) NULL,
      PRIMARY KEY (id)
    ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS exercise_checkins (
      id INT NOT NULL AUTO_INCREMENT,
      user_id INT NOT NULL,
      exercise_id INT NOT NULL,
      completed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      INDEX exercise_checkins_user_idx (user_id),
      CONSTRAINT exercise_checkins_user_fk FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
      CONSTRAINT exercise_checkins_exercise_fk FOREIGN KEY (exercise_id)
        REFERENCES exercises(id) ON DELETE CASCADE ON UPDATE CASCADE
    ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
  `);

  const [countRows] = await pool.query<RowDataPacket[]>("SELECT COUNT(*) as c FROM exercises");
  const n = Number(countRows[0]?.c ?? 0);
  if (n === 0) {
    await pool.query(
      `INSERT INTO exercises (titulo, descricao) VALUES
       (?, ?), (?, ?), (?, ?)`,
      [
        "1. Fortalecimento das Pernas",
        "Sequencia guiada para fortalecer membros inferiores. Faca com calma e pare se sentir dor.",
        "2. Fortalecimento do Quadril",
        "Exercicios de estabilidade do quadril. Respire de forma regular.",
        "3. Fortalecimento Articular",
        "Mobilidade suave das articulacoes. Nao force o amplitude."
      ]
    );
  }
}
