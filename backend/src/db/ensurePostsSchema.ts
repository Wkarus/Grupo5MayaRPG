import type { RowDataPacket } from "mysql2";
import type { Pool } from "mysql2/promise";

export async function ensurePostsSchema(pool: Pool): Promise<void> {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS post_recipients (
      id INT NOT NULL AUTO_INCREMENT,
      post_id INT NOT NULL,
      patient_id INT NOT NULL,
      PRIMARY KEY (id),
      UNIQUE KEY post_recipients_unique (post_id, patient_id),
      CONSTRAINT post_recipients_post_fk FOREIGN KEY (post_id)
        REFERENCES posts (id) ON DELETE CASCADE ON UPDATE CASCADE,
      CONSTRAINT post_recipients_patient_fk FOREIGN KEY (patient_id)
        REFERENCES patients (id) ON DELETE CASCADE ON UPDATE CASCADE
    ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
  `);

  const columns: Array<{ name: string; ddl: string }> = [
    { name: "tipo", ddl: "ALTER TABLE posts ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'TEXTO'" },
    { name: "media_url", ddl: "ALTER TABLE posts ADD COLUMN media_url VARCHAR(500) NULL" },
    { name: "audience", ddl: "ALTER TABLE posts ADD COLUMN audience VARCHAR(20) NOT NULL DEFAULT 'TODOS'" }
  ];

  for (const col of columns) {
    const [rows] = await pool.query<RowDataPacket[]>(
      `SELECT COUNT(*) as c FROM information_schema.COLUMNS
       WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'posts' AND COLUMN_NAME = ?`,
      [col.name]
    );
    if (Number(rows[0]?.c ?? 0) === 0) {
      await pool.query(col.ddl);
    }
  }

  await pool.query(`
    ALTER TABLE posts MODIFY COLUMN conteudo TEXT NULL
  `).catch(() => undefined);
}
