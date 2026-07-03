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
    INDEX idx_users_user_code_current (user_code, is_current, is_deleted),
    INDEX idx_users_role_current_created (role, is_current, is_deleted, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  TEXT         NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at   DATETIME(3)  NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id  BIGINT       NOT NULL,
    name         VARCHAR(255) NOT NULL,
    description  TEXT         NULL,
    status       ENUM ('DRAFT', 'ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'DRAFT',
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at   DATETIME(3)  NULL,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_products_category_id (category_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product_variants (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id        BIGINT        NOT NULL,
    name              VARCHAR(255)  NOT NULL,
    price             DECIMAL(10,2) NOT NULL DEFAULT 0,
    available_stock   INT           NOT NULL DEFAULT 0,
    status            ENUM ('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at        DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at        DATETIME(3)   NULL,
    CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_product_variants_product_id (product_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cart_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code     VARCHAR(20)  NOT NULL,
    variant_id    BIGINT       NOT NULL,
    quantity      INT          NOT NULL DEFAULT 1,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at    DATETIME(3)  NULL,
    CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    INDEX idx_cart_items_user_code (user_code),
    INDEX idx_cart_items_variant_id (variant_id)
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

-- Temporary UC-23 table. Replace with the official Order schema when available.
CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code       VARCHAR(20)   NOT NULL UNIQUE,
    user_id          BIGINT        NOT NULL,
    recipient_name   VARCHAR(255)  NULL,
    recipient_phone  VARCHAR(20)   NULL,
    delivery_address VARCHAR(512)  NULL,
    shipping_fee     DECIMAL(12,2) NOT NULL DEFAULT 0,
    subtotal         DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount     DECIMAL(12,2) NOT NULL DEFAULT 0,
    order_status     ENUM ('NEW', 'CONFIRMED', 'SHIPPING', 'COMPLETED', 'CANCELLED', 'PENDING_CANCEL') NOT NULL DEFAULT 'NEW',
    payment_status   ENUM ('UNPAID', 'PENDING', 'PAID', 'FAILED', 'TIMEOUT', 'REFUND_PENDING', 'REFUNDED') NOT NULL DEFAULT 'UNPAID',
    payment_method   VARCHAR(50)   NOT NULL DEFAULT 'COD',
    note             TEXT          NULL,
    created_at       DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at       DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at       DATETIME(3)   NULL,
    CONSTRAINT fk_orders_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_orders_user_id_created_at (user_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT        NOT NULL,
    product_name  VARCHAR(255)  NOT NULL,
    variant_name  VARCHAR(255)  NULL,
    unit_price    DECIMAL(12,2) NOT NULL,
    quantity      INT           NOT NULL,
    subtotal      DECIMAL(12,2) NOT NULL,
    product_id    BIGINT        NULL,
    variant_id    BIGINT        NULL,
    created_at    DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at    DATETIME(3)   NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_product_id (product_id),
    INDEX idx_order_items_variant_id (variant_id)
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

INSERT INTO categories (name, description, is_active, created_at, updated_at) VALUES
    ('Thức ăn', 'Thức ăn dinh dưỡng cho mọi loại thú cưng', TRUE, NOW(3), NOW(3)),
    ('Đồ chơi', 'Đồ chơi thú vị để thú cưng vui chơi', TRUE, NOW(3), NOW(3)),
    ('Phụ kiện', 'Phụ kiện chăm sóc và làm đẹp cho thú cưng', TRUE, NOW(3), NOW(3));

INSERT INTO products (category_id, name, description, status, created_at, updated_at) VALUES
    ((SELECT id FROM categories WHERE name = 'Thức ăn' LIMIT 1), 'Royal Canin Chó Trưởng Thành', 'Thức ăn hạt cao cấp cho chó trưởng thành, bổ sung đầy đủ dưỡng chất', 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM categories WHERE name = 'Thức ăn' LIMIT 1), 'Whiskas Mèo Trưởng Thành Cá Ngừ', 'Thức ăn ướt cho mèo trưởng thành, hương vị cá ngừ tươi ngon', 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM categories WHERE name = 'Thức ăn' LIMIT 1), 'Tetra Min Thức Ăn Cá Cảnh', 'Thức ăn dạng vảy đa năng cho cá cảnh nhiệt đới', 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM categories WHERE name = 'Đồ chơi' LIMIT 1), 'Bóng Cao Su Tương Tác Cho Chó', 'Bóng cao su tự nhiên, bền bỉ, kích thích vận động cho chó', 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM categories WHERE name = 'Đồ chơi' LIMIT 1), 'Đũa Lông Vũ Câu Mèo', 'Đồ chơi câu mèo với lông vũ sặc sỡ, kích thích bản năng săn mồi', 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM categories WHERE name = 'Đồ chơi' LIMIT 1), 'Bánh Xe Chạy Cho Hamster', 'Bánh xe im lặng, giúp hamster vận động mỗi ngày', 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM categories WHERE name = 'Phụ kiện' LIMIT 1), 'Vòng Cổ Chó Da Thật', 'Vòng cổ da bò tự nhiên, chắc chắn và thời trang cho chó', 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM categories WHERE name = 'Phụ kiện' LIMIT 1), 'Sữa Tắm Thú Cưng Bio-Pet', 'Sữa tắm thiên nhiên, an toàn cho da nhạy cảm, hương thơm dịu nhẹ', 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM categories WHERE name = 'Phụ kiện' LIMIT 1), 'Túi Vận Chuyển Thú Cưng', 'Túi thoáng khí, tiện lợi cho việc di chuyển cùng thú cưng', 'ACTIVE', NOW(3), NOW(3));

INSERT INTO product_variants (product_id, name, price, available_stock, status, created_at, updated_at) VALUES
    ((SELECT id FROM products WHERE name = 'Royal Canin Chó Trưởng Thành' LIMIT 1), 'Gói 1kg', 185000, 50, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Royal Canin Chó Trưởng Thành' LIMIT 1), 'Gói 3kg', 490000, 30, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Royal Canin Chó Trưởng Thành' LIMIT 1), 'Gói 10kg', 1350000, 10, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Whiskas Mèo Trưởng Thành Cá Ngừ' LIMIT 1), 'Hộp 85g', 22000, 100, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Whiskas Mèo Trưởng Thành Cá Ngừ' LIMIT 1), 'Thùng 24 hộp', 480000, 20, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Tetra Min Thức Ăn Cá Cảnh' LIMIT 1), 'Hũ 52g', 75000, 80, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Tetra Min Thức Ăn Cá Cảnh' LIMIT 1), 'Hũ 200g', 245000, 40, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Bóng Cao Su Tương Tác Cho Chó' LIMIT 1), 'Size S (đường kính 5cm)', 45000, 60, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Bóng Cao Su Tương Tác Cho Chó' LIMIT 1), 'Size M (đường kính 7cm)', 65000, 60, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Bóng Cao Su Tương Tác Cho Chó' LIMIT 1), 'Size L (đường kính 10cm)', 85000, 40, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Đũa Lông Vũ Câu Mèo' LIMIT 1), 'Bộ 1 cái', 35000, 120, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Đũa Lông Vũ Câu Mèo' LIMIT 1), 'Bộ 3 cái', 89000, 50, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Bánh Xe Chạy Cho Hamster' LIMIT 1), 'Đường kính 15cm - Trắng', 89000, 35, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Bánh Xe Chạy Cho Hamster' LIMIT 1), 'Đường kính 21cm - Hồng', 125000, 25, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Vòng Cổ Chó Da Thật' LIMIT 1), 'Size S (cổ 25-35cm)', 165000, 30, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Vòng Cổ Chó Da Thật' LIMIT 1), 'Size M (cổ 35-50cm)', 195000, 30, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Vòng Cổ Chó Da Thật' LIMIT 1), 'Size L (cổ 50-65cm)', 225000, 20, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Sữa Tắm Thú Cưng Bio-Pet' LIMIT 1), 'Chai 300ml - Hương Lavender', 135000, 45, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Sữa Tắm Thú Cưng Bio-Pet' LIMIT 1), 'Chai 500ml - Hương Chamomile', 195000, 35, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Túi Vận Chuyển Thú Cưng' LIMIT 1), 'Size S (cho chó/mèo < 5kg)', 345000, 20, 'ACTIVE', NOW(3), NOW(3)),
    ((SELECT id FROM products WHERE name = 'Túi Vận Chuyển Thú Cưng' LIMIT 1), 'Size M (cho chó/mèo < 10kg)', 495000, 15, 'ACTIVE', NOW(3), NOW(3));

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

INSERT IGNORE INTO user_credential (
    user_code, provider, provider_user_id, secret_hash, linked_at
) VALUES
    ('KHG-0000001', 'EMAIL', NULL, '$argon2id$v=19$m=16384,t=2,p=1$G8X+svcKeV32Rmj+QRr3GA$N2ChxqNlfSKkCmuzvVmDMQQjJQKduLrUVZtsldtsPv0', NOW(3)),
    ('KHG-0000002', 'EMAIL', NULL, '$argon2id$v=19$m=16384,t=2,p=1$CruJrCTykjh+PlSdjkOzGg$04Xofqs90lnZhyj1asy29UH/IbP6Ud/oGN7tRYuXkDc', NOW(3)),
    ('KHG-0000003', 'EMAIL', NULL, '$argon2id$v=19$m=16384,t=2,p=1$G8X+svcKeV32Rmj+QRr3GA$N2ChxqNlfSKkCmuzvVmDMQQjJQKduLrUVZtsldtsPv0', NOW(3));

INSERT IGNORE INTO users (
    user_code, email, display_name, role,
    effective_from, effective_to, is_current, is_deleted, created_at
) VALUES
    ('KHG-0000001', 'an.nguyen@example.com', 'Nguyen Van An', 'CUSTOMER', NOW(3), NULL, TRUE, FALSE, '2026-05-01 09:00:00.000'),
    ('KHG-0000002', 'binh.tran@example.com', 'Tran Thi Binh', 'CUSTOMER', NOW(3), NULL, TRUE, FALSE, '2026-05-03 10:00:00.000'),
    ('KHG-0000003', 'chi.le@example.com', 'Le Minh Chi', 'CUSTOMER', NOW(3), NULL, TRUE, FALSE, '2026-05-05 11:00:00.000');

INSERT IGNORE INTO shipping_addresses (
    address_id, user_code, recipient_name, phone, place_id, full_address,
    address_detail, latitude, longitude, is_default,
    effective_from, effective_to, is_current, is_deleted, created_at
) VALUES
    ('DCH-0000001', 'KHG-0000001', 'Nguyen Van An', '0900000001', 'place-khg-1',
     'Linh Trung, Thu Duc, TP HCM', 'So 1', 10.87000000, 106.80000000, TRUE,
     NOW(3), NULL, TRUE, FALSE, NOW(3)),
    ('DCH-0000002', 'KHG-0000002', 'Tran Thi Binh', '0900000002', 'place-khg-2',
     'Ben Nghe, Quan 1, TP HCM', 'Tang 2', 10.77500000, 106.70000000, TRUE,
     NOW(3), NULL, TRUE, FALSE, NOW(3)),
    ('DCH-0000003', 'KHG-0000003', 'Le Minh Chi', '0900000003', 'place-khg-3',
     'Da Kao, Quan 1, TP HCM', NULL, 10.78600000, 106.69700000, TRUE,
     NOW(3), NULL, TRUE, FALSE, NOW(3));

INSERT IGNORE INTO cart_items (user_code, variant_id, quantity, created_at, updated_at) VALUES
    (
        'KHG-0000001',
        (SELECT id FROM product_variants WHERE name = 'Hộp 85g' LIMIT 1),
        2,
        NOW(3),
        NOW(3)
    ),
    (
        'KHG-0000001',
        (SELECT id FROM product_variants WHERE name = 'Gói 1kg' LIMIT 1),
        1,
        NOW(3),
        NOW(3)
    ),
    (
        'KHG-0000002',
        (SELECT id FROM product_variants WHERE name = 'Size M (đường kính 7cm)' LIMIT 1),
        3,
        NOW(3),
        NOW(3)
    );

INSERT IGNORE INTO orders (
    order_code, user_id, recipient_name, recipient_phone, delivery_address,
    shipping_fee, subtotal, total_amount, order_status, payment_status, payment_method, note, created_at
) SELECT
    'ORD-0000001', id, 'Nguyen Van An', '0900000001', 'Linh Trung, Thu Duc, TP HCM',
    30000.00, 220000.00, 250000.00, 'NEW', 'UNPAID', 'COD', 'Giao giờ hành chính', '2026-05-01 09:15:00.000'
FROM users
WHERE user_code = 'KHG-0000001';

INSERT IGNORE INTO orders (
    order_code, user_id, recipient_name, recipient_phone, delivery_address,
    shipping_fee, subtotal, total_amount, order_status, payment_status, payment_method, note, created_at
) SELECT
    'ORD-0000002', id, 'Nguyen Van An', '0900000001', 'Khu Công Nghệ Cao, Thủ Đức, TP HCM',
    22000.00, 185000.00, 207000.00, 'SHIPPING', 'PAID', 'VNPAY', NULL, '2026-06-10 14:02:00.000'
FROM users
WHERE user_code = 'KHG-0000001';

INSERT IGNORE INTO orders (
    order_code, user_id, recipient_name, recipient_phone, delivery_address,
    shipping_fee, subtotal, total_amount, order_status, payment_status, payment_method, note, created_at
) SELECT
    'ORD-0000003', id, 'Tran Thi Binh', '0900000002', 'Ben Nghe, Quan 1, TP HCM',
    25000.00, 865000.00, 890000.00, 'CANCELLED', 'REFUNDED', 'COD', NULL, '2026-04-22 11:47:00.000'
FROM users
WHERE user_code = 'KHG-0000002';

INSERT IGNORE INTO order_items (
    order_id, product_name, variant_name, unit_price, quantity, subtotal, product_id, variant_id, created_at
) SELECT
    o.id, 'Whiskas Mèo Trưởng Thành Cá Ngừ', 'Hộp 85g', 22000.00, 10, 220000.00, NULL, NULL, '2026-05-01 09:15:00.000'
FROM orders o
WHERE o.order_code = 'ORD-0000001';

INSERT IGNORE INTO order_items (
    order_id, product_name, variant_name, unit_price, quantity, subtotal, product_id, variant_id, created_at
) SELECT
    o.id, 'Royal Canin Chó Trưởng Thành', 'Gói 1kg', 185000.00, 1, 185000.00, NULL, NULL, '2026-06-10 14:02:00.000'
FROM orders o
WHERE o.order_code = 'ORD-0000002';

INSERT IGNORE INTO order_items (
    order_id, product_name, variant_name, unit_price, quantity, subtotal, product_id, variant_id, created_at
) SELECT
    o.id, 'Bánh Xe Chạy Cho Hamster', 'Đường kính 21cm - Hồng', 125000.00, 4, 500000.00, NULL, NULL, '2026-04-22 11:47:00.000'
FROM orders o
WHERE o.order_code = 'ORD-0000003';

INSERT IGNORE INTO order_items (
    order_id, product_name, variant_name, unit_price, quantity, subtotal, product_id, variant_id, created_at
) SELECT
    o.id, 'Túi Vận Chuyển Thú Cưng', 'Size S (cho chó/mèo < 5kg)', 345000.00, 1, 345000.00, NULL, NULL, '2026-04-22 11:47:00.000'
FROM orders o
WHERE o.order_code = 'ORD-0000003';
