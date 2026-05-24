import type { RowDataPacket } from "mysql2";
import type { Pool } from "mysql2/promise";

export async function ensureCommentsSchema(pool: Pool): Promise<void> {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS comments (
      id INT NOT NULL AUTO_INCREMENT,
      autor VARCHAR(191) NOT NULL,
      texto TEXT NOT NULL,
      status VARCHAR(191) NOT NULL DEFAULT 'PENDENTE',
      resposta TEXT NULL,
      lido TINYINT NOT NULL DEFAULT 0,
      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      INDEX comments_autor_idx (autor),
      INDEX comments_lido_idx (lido)
    ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
  `);

  const [cols] = await pool.query<RowDataPacket[]>(
    `SELECT COUNT(*) as c FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'comments' AND COLUMN_NAME = 'lido'`
  );
  if (Number(cols[0]?.c ?? 0) === 0) {
    await pool.query(`ALTER TABLE comments ADD COLUMN lido TINYINT NOT NULL DEFAULT 0`);
  }

  const [colsPostId] = await pool.query<RowDataPacket[]>(
    `SELECT COUNT(*) as c FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'comments' AND COLUMN_NAME = 'post_id'`
  );
  if (Number(colsPostId[0]?.c ?? 0) === 0) {
    await pool.query(`ALTER TABLE comments ADD COLUMN post_id INT NULL`);
    await pool.query(`ALTER TABLE comments ADD INDEX comments_post_idx (post_id)`);
  }
}
