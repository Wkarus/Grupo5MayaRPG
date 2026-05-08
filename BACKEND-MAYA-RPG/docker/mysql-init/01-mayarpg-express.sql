-- Banco para o backend Express (`backend/`), usado pelo admin-web (`/admin/*`, `/agenda/*`).
-- Executado na primeira subida do MySQL quando o volume `mysql_data` ainda esta vazio.

CREATE DATABASE IF NOT EXISTS mayarpg
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE mayarpg;

CREATE TABLE IF NOT EXISTS users (
  id INT NOT NULL AUTO_INCREMENT,
  email VARCHAR(191) NOT NULL,
  nome VARCHAR(191) NOT NULL,
  senha VARCHAR(191) NOT NULL,
  role VARCHAR(191) NOT NULL,
  UNIQUE INDEX users_email_key (email),
  PRIMARY KEY (id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS posts (
  id INT NOT NULL AUTO_INCREMENT,
  titulo VARCHAR(500) NOT NULL,
  conteudo TEXT NOT NULL,
  categoria VARCHAR(191) NOT NULL,
  status VARCHAR(191) NOT NULL,
  data_publicacao DATETIME NULL,
  PRIMARY KEY (id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agenda (
  id INT NOT NULL AUTO_INCREMENT,
  data DATE NOT NULL,
  horario_inicio TIME NOT NULL,
  horario_fim TIME NOT NULL,
  bloqueado TINYINT NOT NULL DEFAULT 0,
  motivo VARCHAR(500) NULL,
  PRIMARY KEY (id),
  INDEX agenda_data_horario_idx (data, horario_inicio, horario_fim)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comments (
  id INT NOT NULL AUTO_INCREMENT,
  autor VARCHAR(191) NOT NULL,
  texto TEXT NOT NULL,
  status VARCHAR(191) NOT NULL DEFAULT 'PENDENTE',
  resposta TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agendamentos (
  id INT NOT NULL AUTO_INCREMENT,
  data DATE NOT NULL,
  horario VARCHAR(191) NOT NULL,
  nome VARCHAR(191) NOT NULL,
  observacao VARCHAR(500) NULL,
  user_id INT NULL,
  PRIMARY KEY (id),
  INDEX agendamentos_data_horario_idx (data, horario),
  CONSTRAINT agendamentos_user_id_fkey FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT IGNORE INTO users (email, nome, senha, role)
VALUES ('mayarpg@gmail.com', 'Admin Demo', '1234', 'ADMIN');
