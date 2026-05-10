import type { RowDataPacket } from "mysql2";
import type { Pool } from "mysql2/promise";

// Tabelas do modulo admin: pacientes + prontuario.
export async function ensureClinicalSchema(pool: Pool): Promise<void> {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS patients (
      id INT NOT NULL AUTO_INCREMENT,
      nome VARCHAR(191) NOT NULL,
      telefone VARCHAR(50) NULL,
      email VARCHAR(191) NULL,
      status ENUM('ATIVO', 'INATIVO') NOT NULL DEFAULT 'ATIVO',
      observacoes TEXT NULL,
      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      INDEX patients_nome_idx (nome),
      INDEX patients_status_idx (status)
    ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS patient_records (
      id INT NOT NULL AUTO_INCREMENT,
      patient_id INT NOT NULL,
      observacao TEXT NOT NULL,
      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      INDEX patient_records_patient_idx (patient_id),
      CONSTRAINT patient_records_patient_fk FOREIGN KEY (patient_id)
        REFERENCES patients(id) ON DELETE CASCADE ON UPDATE CASCADE
    ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
  `);

  const [rows] = await pool.query<RowDataPacket[]>("SELECT COUNT(*) as c FROM patients");
  const total = Number(rows[0]?.c ?? 0);
  if (total === 0) {
    await pool.query(
      `INSERT INTO patients (nome, telefone, email, status, observacoes) VALUES
       (?, ?, ?, 'ATIVO', ?),
       (?, ?, ?, 'ATIVO', ?),
       (?, ?, ?, 'INATIVO', ?)`,
      [
        "Ana Paula Santos",
        "(11) 98765-4321",
        "ana@example.com",
        "Dor lombar cronica e postura.",
        "Rodrigo Mendes",
        "(11) 91234-5678",
        "rodrigo@example.com",
        "Reabilitacao pos-cirurgia no ombro.",
        "Carla Ferreira",
        "(11) 99876-5432",
        "carla@example.com",
        "Paciente pausou tratamento neste mes."
      ]
    );
  }
}
