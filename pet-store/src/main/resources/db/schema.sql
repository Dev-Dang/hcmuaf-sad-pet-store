-- Pet Store - Database Schema
-- App     : spring.sql.init.mode=never  -> run manually when needed
-- Test    : spring.sql.init.mode=always -> reset schema for each test context
-- Manual  : mysql -u root -p < src/main/resources/db/schema.sql
--
-- Mock admin:
--   email    : admin@petstore.local
--   password : admin1234

DROP DATABASE IF EXISTS pet_store;
CREATE DATABASE pet_store CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_store;

CREATE TABLE IF NOT EXISTS users (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code         VARCHAR(20)                NOT NULL,
    email             VARCHAR(255)               NOT NULL,
    display_name      VARCHAR(255)               NOT NULL,
    role              ENUM ('CUSTOMER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    effective_from    DATETIME(3)                NOT NULL,
    effective_to      DATETIME(3)                NULL,
    is_current        BOOLEAN                    NOT NULL DEFAULT TRUE,
    is_deleted        BOOLEAN                    NOT NULL DEFAULT FALSE,
    created_at        DATETIME(3)                NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT chk_users_scd_window CHECK (
        (is_current = TRUE AND effective_to IS NULL)
        OR (is_current = FALSE AND effective_to IS NOT NULL)
    ),
    INDEX idx_users_email_current (email, is_current, is_deleted),
    INDEX idx_users_user_code_current (user_code, is_current, is_deleted)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_credential (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code        VARCHAR(20)              NOT NULL,
    provider         ENUM ('EMAIL', 'GOOGLE') NOT NULL,
    provider_user_id VARCHAR(255)             NULL,
    secret_hash      VARCHAR(255)             NULL,
    linked_at        DATETIME(3)              NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT chk_user_credential_provider_payload CHECK (
        (provider = 'EMAIL' AND secret_hash IS NOT NULL AND provider_user_id IS NULL)
        OR (provider = 'GOOGLE' AND secret_hash IS NULL AND provider_user_id IS NOT NULL)
    ),
    UNIQUE KEY uk_user_provider (user_code, provider),
    UNIQUE KEY uk_provider_user_id (provider, provider_user_id),
    INDEX idx_user_credential_user_code (user_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shipping_addresses (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    address_id             VARCHAR(36)    NOT NULL,
    user_code              VARCHAR(20)    NOT NULL,
    recipient_name         VARCHAR(255)   NOT NULL,
    phone                  VARCHAR(20)    NOT NULL,
    place_id               VARCHAR(255)   NOT NULL,
    full_address           VARCHAR(512)   NOT NULL,
    address_detail         VARCHAR(255)   NULL,
    latitude               DECIMAL(10, 8) NOT NULL,
    longitude              DECIMAL(11, 8) NOT NULL,
    is_default             BOOLEAN        NOT NULL DEFAULT FALSE,
    effective_from         DATETIME(3)    NOT NULL,
    effective_to           DATETIME(3)    NULL,
    is_current             BOOLEAN        NOT NULL DEFAULT TRUE,
    is_deleted             BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at             DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT chk_shipping_addresses_scd_window CHECK (
        (is_current = TRUE AND effective_to IS NULL)
        OR (is_current = FALSE AND effective_to IS NOT NULL)
    ),
    INDEX idx_addr_user_code_current (user_code, is_current, is_deleted),
    INDEX idx_addr_address_id_current (address_id, is_current, is_deleted)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS otp_challenges (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    challenge_id           VARCHAR(36)                                           NOT NULL,
    purpose                ENUM ('RESET_PASSWORD')                               NOT NULL,
    target_type            ENUM ('EMAIL')                                        NOT NULL,
    target_value           VARCHAR(255)                                          NOT NULL,
    user_code              VARCHAR(20)                                           NOT NULL,
    status                 ENUM ('PENDING', 'VERIFIED', 'COMPLETED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    resend_count           INT                                                   NOT NULL DEFAULT 0,
    last_sent_at           DATETIME(3)                                           NULL,
    expires_at             DATETIME(3)                                           NOT NULL,
    verified_otp_record_id BIGINT                                                NULL,
    verified_at            DATETIME(3)                                           NULL,
    completed_at           DATETIME(3)                                           NULL,
    created_at             DATETIME(3)                                           NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT chk_otp_challenges_resend_count CHECK (resend_count >= 0),
    CONSTRAINT chk_otp_challenges_verified_state CHECK (
        (status IN ('PENDING', 'EXPIRED') AND verified_otp_record_id IS NULL AND verified_at IS NULL)
        OR (status IN ('VERIFIED', 'COMPLETED') AND verified_otp_record_id IS NOT NULL AND verified_at IS NOT NULL)
    ),
    CONSTRAINT chk_otp_challenges_completed_state CHECK (
        (status = 'COMPLETED' AND completed_at IS NOT NULL)
        OR (status <> 'COMPLETED' AND completed_at IS NULL)
    ),
    UNIQUE KEY uk_otp_challenges_challenge_id (challenge_id),
    INDEX idx_otp_challenges_user_target (user_code, purpose, target_type, target_value, status),
    INDEX idx_otp_challenges_status_expires (status, expires_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS otp_records (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    challenge_id        VARCHAR(36)                            NOT NULL,
    otp_hash            VARCHAR(255)                           NOT NULL,
    attempt_count       INT                                    NOT NULL DEFAULT 0,
    status              ENUM ('ACTIVE', 'USED', 'INVALIDATED') NOT NULL DEFAULT 'ACTIVE',
    sent_at             DATETIME(3)                            NOT NULL,
    expires_at          DATETIME(3)                            NOT NULL,
    used_at             DATETIME(3)                            NULL,
    created_at          DATETIME(3)                            NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT chk_otp_records_attempt_count CHECK (attempt_count >= 0),
    CONSTRAINT chk_otp_records_used_state CHECK (
        (status = 'USED' AND used_at IS NOT NULL)
        OR (status <> 'USED' AND used_at IS NULL)
    ),
    INDEX idx_otp_record_challenge_id (challenge_id),
    INDEX idx_otp_records_status_expires (status, expires_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bk_generator (
    entity_type VARCHAR(50) PRIMARY KEY,
    prefix      VARCHAR(10) NOT NULL,
    last_val    BIGINT      NOT NULL DEFAULT 0,
    pad_length  TINYINT     NOT NULL DEFAULT 7
) ENGINE=InnoDB;

INSERT IGNORE INTO bk_generator (entity_type, prefix, last_val, pad_length) VALUES
    ('CUSTOMER', 'KHG', 999, 7),
    ('ADMIN',    'ADM', 1,   4),
    ('ADDRESS',  'DCH', 0,   7),
    ('ORDER',    'ORD', 0,   7),
    ('PRODUCT',  'SP',  0,   7);

INSERT IGNORE INTO users (
    user_code, email, display_name, role,
    effective_from, effective_to, is_current, is_deleted, created_at
) VALUES (
    'ADM-0001', 'admin@petstore.local', 'Mock Admin', 'ADMIN',
    NOW(3), NULL, TRUE, FALSE, NOW(3)
);

INSERT IGNORE INTO user_credential (
    user_code, provider, provider_user_id, secret_hash, linked_at
) VALUES (
    'ADM-0001',
    'EMAIL',
    NULL,
    '$argon2id$v=19$m=16384,t=2,p=1$2fZhLkK7uZxvf35f08iTIQ$UMfmwUPCPeEQfT8qhsjOpqyxdpETzdBxPZNyzwhq42k',
    NOW(3)
);
