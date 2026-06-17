-- Pet Store — Database Schema
-- App     : spring.sql.init.mode=never  → chạy thủ công khi cần
-- Test    : spring.sql.init.mode=always → tự reset mỗi lần test
-- Thủ công: mysql -u root -p < src/main/resources/db/schema.sql

DROP DATABASE IF EXISTS pet_store;
CREATE DATABASE pet_store CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_store;

-- ─── SCD Type 2 ────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code    VARCHAR(36)                        NOT NULL,
    email        VARCHAR(255)                       NOT NULL,
    display_name VARCHAR(255)                       NOT NULL,
    role         ENUM ('CUSTOMER', 'ADMIN')         NOT NULL DEFAULT 'CUSTOMER',
    effective_from DATETIME(3)                      NOT NULL,
    effective_to   DATETIME(3)                      NULL,
    is_current   BOOLEAN                            NOT NULL DEFAULT TRUE,
    is_deleted   BOOLEAN                            NOT NULL DEFAULT FALSE,
    created_at   DATETIME(3)                        NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_users_email_current (email, is_current),
    INDEX idx_users_user_code_current (user_code, is_current)
);

-- ─── Fact Table ─────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS user_credential (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code        VARCHAR(36)                    NOT NULL,
    provider         ENUM ('EMAIL', 'GOOGLE')       NOT NULL,
    provider_user_id VARCHAR(255)                   NULL,
    secret_hash      VARCHAR(255)                   NULL,
    linked_at        DATETIME(3)                    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_user_provider (user_code, provider),
    UNIQUE KEY uk_provider_user_id (provider, provider_user_id)
);

-- ─── SCD Type 2 — TODO: UC-8 ────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS shipping_addresses (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    address_id     VARCHAR(36)   NOT NULL,
    user_code      VARCHAR(20)   NOT NULL,
    recipient_name VARCHAR(255)  NOT NULL,
    phone          VARCHAR(20)   NOT NULL,
    place_id       VARCHAR(255)  NOT NULL,
    full_address   VARCHAR(512)  NOT NULL,
    address_detail VARCHAR(255)  NULL,
    latitude       DECIMAL(10, 8) NOT NULL,
    longitude      DECIMAL(11, 8) NOT NULL,
    is_default     BOOLEAN       NOT NULL DEFAULT FALSE,
    effective_from DATETIME(3)   NOT NULL,
    effective_to   DATETIME(3)   NULL,
    is_current     BOOLEAN       NOT NULL DEFAULT TRUE,
    is_deleted     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_addr_user_code_current (user_code, is_current)
);

-- ─── Fact Tables — TODO: UC-4/6 ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS otp_challenges (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    challenge_id          VARCHAR(36)                                              NOT NULL UNIQUE,
    purpose               ENUM ('RESET_PASSWORD')                                 NOT NULL,
    target_type           ENUM ('EMAIL')                                           NOT NULL,
    target_value          VARCHAR(255)                                             NOT NULL,
    user_code             VARCHAR(20)                                              NOT NULL,
    status                ENUM ('PENDING', 'VERIFIED', 'COMPLETED', 'EXPIRED')    NOT NULL DEFAULT 'PENDING',
    resend_count          INT                                                      NOT NULL DEFAULT 0,
    last_sent_at          DATETIME(3)                                              NULL,
    expires_at            DATETIME(3)                                              NOT NULL,
    verified_otp_record_id BIGINT                                                  NULL,
    verified_at           DATETIME(3)                                              NULL,
    completed_at          DATETIME(3)                                              NULL,
    created_at            DATETIME(3)                                              NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_otp_challenge_id (challenge_id)
);

CREATE TABLE IF NOT EXISTS otp_records (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    challenge_id  VARCHAR(36)                              NOT NULL,
    otp_hash      VARCHAR(255)                             NOT NULL,
    attempt_count INT                                      NOT NULL DEFAULT 0,
    status        ENUM ('ACTIVE', 'USED', 'INVALIDATED')  NOT NULL DEFAULT 'ACTIVE',
    sent_at       DATETIME(3)                              NOT NULL,
    expires_at    DATETIME(3)                              NOT NULL,
    used_at       DATETIME(3)                              NULL,
    created_at    DATETIME(3)                              NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_otp_record_challenge_id (challenge_id)
);

-- ─── Business Key Generator ──────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS bk_generator (
    entity_type VARCHAR(50) PRIMARY KEY,
    prefix      VARCHAR(10) NOT NULL,
    last_val    BIGINT      NOT NULL DEFAULT 0,
    pad_length  TINYINT     NOT NULL DEFAULT 7
) ENGINE=InnoDB;

INSERT IGNORE INTO bk_generator (entity_type, prefix, last_val, pad_length) VALUES
    ('CUSTOMER', 'KHG', 999, 7),
    ('ADMIN',    'ADM', 0,   4);
