-- =========================
-- SCHEMA
-- =========================
CREATE SCHEMA IF NOT EXISTS simple_ledger_sch
DEFAULT CHARACTER SET utf8mb4;

USE simple_ledger_sch;

-- =========================
-- TRANSACTION TYPES
-- =========================
CREATE TABLE sld_transaction_types (
  code VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- ACCOUNTS
-- =========================
CREATE TABLE sld_accounts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  normal_balance VARCHAR(10) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT chk_accounts_name CHECK (CHAR_LENGTH(name) > 0),
  CONSTRAINT chk_accounts_normal_balance CHECK (normal_balance IN ('DEBIT','CREDIT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- TRANSACTIONS
-- =========================
CREATE TABLE sld_transactions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  idempotency_key VARCHAR(255) NOT NULL,
  transaction_type_code VARCHAR(50) NOT NULL,
  external_id VARCHAR(255),
  notes TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_transactions_idempotency (idempotency_key),
  KEY idx_transaction_type (transaction_type_code),
  CONSTRAINT fk_transaction_type 
    FOREIGN KEY (transaction_type_code) 
    REFERENCES sld_transaction_types(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- JOURNAL ENTRIES
-- =========================
CREATE TABLE sld_journal_entries (
  id BIGINT NOT NULL AUTO_INCREMENT,
  transaction_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  type VARCHAR(10) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_journal_transaction (transaction_id),
  KEY idx_journal_account (account_id),
  CONSTRAINT fk_journal_transaction 
    FOREIGN KEY (transaction_id) 
    REFERENCES sld_transactions(id),
  CONSTRAINT fk_journal_account 
    FOREIGN KEY (account_id) 
    REFERENCES sld_accounts(id),
  CONSTRAINT chk_journal_amount CHECK (amount > 0),
  CONSTRAINT chk_journal_type CHECK (type IN ('DEBIT','CREDIT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- SEED DATA
-- =========================
INSERT INTO sld_transaction_types (code, description) VALUES
('DEPOSIT', 'External deposit'),
('TRANSFER', 'Internal transfer'),
('CARD', 'Card payment');