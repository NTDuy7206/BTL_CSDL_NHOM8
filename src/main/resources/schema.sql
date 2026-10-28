CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT NOT NULL,
    display_name VARCHAR(120) NULL,
    email VARCHAR(190) NULL,
    phone VARCHAR(30) NULL,
    avatar_object_key VARCHAR(255) NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES `user` (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contract_requests (
    request_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    room_id VARCHAR(20) NOT NULL,
    requested_start_date DATE NOT NULL,
    message VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    PRIMARY KEY (request_id),
    KEY ix_contract_requests_status_created (status, created_at),
    KEY ix_contract_requests_user (user_id),
    CONSTRAINT fk_contract_requests_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_contract_requests_room FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS payment_reports (
    payment_report_id BIGINT NOT NULL AUTO_INCREMENT,
    payer_user_id BIGINT NOT NULL,
    invoice_id VARCHAR(20) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    transaction_reference VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    PRIMARY KEY (payment_report_id),
    KEY ix_payment_reports_payer (payer_user_id, created_at),
    KEY ix_payment_reports_invoice (invoice_id, status),
    CONSTRAINT fk_payment_reports_user FOREIGN KEY (payer_user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_reports_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (invoice_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Preserve USER=0 and ADMIN=1 while consolidating legacy roles.
CREATE TABLE IF NOT EXISTS demo_seed_history (
    seed_key VARCHAR(100) NOT NULL PRIMARY KEY,
    completed BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

UPDATE user SET role=1 WHERE role=4;
UPDATE user SET role=0 WHERE role IN (2,3);
CREATE TABLE IF NOT EXISTS tenant_accounts (
    user_id BIGINT NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT fk_ta_user FOREIGN KEY(user_id) REFERENCES user(id),
    CONSTRAINT fk_ta_tenant FOREIGN KEY(tenant_id) REFERENCES tenants(tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS rental_request_details (
    request_id BIGINT NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL,
    requested_end_date DATE NOT NULL,
    contract_id VARCHAR(20) NULL,
    CONSTRAINT fk_rr_request FOREIGN KEY(request_id) REFERENCES contract_requests(request_id) ON DELETE CASCADE,
    CONSTRAINT fk_rr_tenant FOREIGN KEY(tenant_id) REFERENCES tenants(tenant_id),
    CONSTRAINT fk_rr_contract FOREIGN KEY(contract_id) REFERENCES contracts(contract_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
